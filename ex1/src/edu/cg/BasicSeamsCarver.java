package edu.cg;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;


public class BasicSeamsCarver extends ImageProcessor {

	// An enum describing the carving scheme used by the seams carver.
	// VERTICAL_HORIZONTAL means vertical seams are removed first.
	// HORIZONTAL_VERTICAL means horizontal seams are removed first.
	// INTERMITTENT means seams are removed intermittently : vertical, horizontal, vertical, horizontal etc.
	public enum CarvingScheme {
		VERTICAL_HORIZONTAL("Vertical seams first"),
		HORIZONTAL_VERTICAL("Horizontal seams first"),
		INTERMITTENT("Intermittent carving");

		public final String description;

		CarvingScheme(String description) {
			this.description = description;
		}
	}

	// A simple coordinate class which assists the implementation.
	protected class Coordinate{
		public int X;
		public int Y;
		public Coordinate(int X, int Y) {
			this.X = X;
			this.Y = Y;
		}
	}

	// A simple seam class which assists the implementation.
	protected class Seam {
		private List<Coordinate> pixels;

		public Seam(){
			pixels = new ArrayList<>();
		}

		public void AddPixel(Coordinate pixel){
			pixels.add(pixel);
		}

		public Coordinate[] GetSeamArray(){
			Coordinate[] array = new Coordinate[pixels.size()];
			pixels.toArray(array);

			return array;
		}
	}

	// TODO :  Decide on the fields your BasicSeamsCarver should include. Refer to the recitation and homework
	BufferedImage greyImage;
	List<Seam> seams;
	double[][] M_Energy;
	int actualWidth;
	int actualHeight;
	public Coordinate[][] coordinates;

	public BasicSeamsCarver(Logger logger, BufferedImage workingImage,
							int outWidth, int outHeight, RGBWeights rgbWeights) {
		super((s) -> logger.log("Seam carving: " + s), workingImage, rgbWeights, outWidth, outHeight);

		greyImage = greyscale();
		seams = new ArrayList<>();
		actualWidth = workingImage.getWidth();
		actualHeight = workingImage.getHeight();
		M_Energy = new double[actualHeight][actualWidth];
		coordinates = new Coordinate[actualHeight][actualWidth];

		InitializeCoordinates();
		InitializeEnergy();
	}

	public BufferedImage carveImage(CarvingScheme carvingScheme) {
		int numberOfVerticalSeamsToCarve = Math.abs(this.outWidth - this.inWidth);
		int numberOfHorizontalSeamsToCarve = Math.abs(this.outHeight - this.inHeight);

		logger.log("Preparing to carve image...");

		setForEachInputParameters();

		switch (carvingScheme) {
			case VERTICAL_HORIZONTAL:
				for (int i = 0; i < numberOfVerticalSeamsToCarve; i ++){
					FindAndRemoveOneVerticalSeam();
				}

				for (int i = 0; i < numberOfHorizontalSeamsToCarve; i ++){
					FindAndRemoveOneHorizontalSeam();
				}
				break;
			case HORIZONTAL_VERTICAL:
				for (int i = 0; i < numberOfHorizontalSeamsToCarve; i ++){
					FindAndRemoveOneHorizontalSeam();
				}

				for (int i = 0; i < numberOfVerticalSeamsToCarve; i ++){
					FindAndRemoveOneVerticalSeam();
				}
				break;

			case INTERMITTENT:
				while(numberOfHorizontalSeamsToCarve > 0 && numberOfVerticalSeamsToCarve > 0){
					FindAndRemoveOneVerticalSeam();
					numberOfVerticalSeamsToCarve--;

					FindAndRemoveOneHorizontalSeam();
					numberOfHorizontalSeamsToCarve--;

				}

				while(numberOfHorizontalSeamsToCarve > 0){
					FindAndRemoveOneHorizontalSeam();
					numberOfHorizontalSeamsToCarve--;
				}

				while(numberOfVerticalSeamsToCarve > 0){
					FindAndRemoveOneVerticalSeam();
					numberOfVerticalSeamsToCarve--;
				}
				break;
		}

		logger.log("Build new Image ...");
		BufferedImage ans = newEmptyOutputSizedImage();
		for (int y = 0; y < ans.getHeight(); y ++){
			for (int x = 0; x < ans.getWidth(); x ++){
				ans.setRGB(x, y, workingImage.getRGB(coordinates[y][x].X, coordinates[y][x].Y));
			}
		}
		logger.log("Build new Image done ...");

		logger.log("Done carve image.");

		return ans;
	}

	private void FindAndRemoveOneVerticalSeam(){
		Seam seam = verticalSeam();
		seams.add(seam);
		removeVerticalSeam(seam);
	}

	private void FindAndRemoveOneHorizontalSeam() {
		Seam seam = horizontalSeam();
		seams.add(seam);
		removeHorizontalSeam(seam);
	}

	//Initiate the coordinate matrix
	private void InitializeCoordinates() {
		forEach((y, x) -> {
			coordinates[y][x] = new Coordinate(x,y);
		});
	}

	//set the calculated energy for each pixel in the image
	private void InitializeEnergy(){
		//Initialize Energy Matrix Value
		forEach((y, x) -> {
			M_Energy[y][x] = calcPixelEnergy(x, y);
		});
	}

	//calculate the cost of each pixel in vertical seams (energy + new edge cost)
	private double[][] CalcCostVertical(){
		double[][] M_CostVertical = new double[actualHeight][actualWidth];

		logger.log("Initialize vertical seam...");

		//Initialize Cost Matrix Value
		for (int y = 0; y < actualHeight; y ++ ){
			for (int x = 0; x < actualWidth; x ++){
				double currentEnergy = getPixelEnergy(x, y);

				if (y == 0){
					M_CostVertical[y][x] = (x == 0 || x == actualWidth - 1) ? currentEnergy : currentEnergy + Math.abs(getPixelEnergy(x + 1, y) - getPixelEnergy(x - 1, y));
				}else {
					M_CostVertical[y][x] = currentEnergy + getMinCostValueVertical(M_CostVertical, x, y);
				}
			}
		}

		logger.log("Done vertical seam...");

		return M_CostVertical;
	}

	//calculate the cost of each pixel in horizontal seams (energy + new edge cost)
	private double[][] CalcCostHorizontal(){
		double[][] M_CostHorizontal = new double[actualHeight][actualWidth];

		logger.log("Initialize horizontal seam...");

		//Initialize Cost Matrix Value
		for (int x = 0; x < actualWidth; x ++ ){
			for (int y = 0; y < actualHeight; y ++){
				double currentEnergy = getPixelEnergy(x, y);

				if (x == 0){
					M_CostHorizontal[y][x] = (y == 0 || y == actualHeight - 1) ? currentEnergy : currentEnergy + Math.abs(getPixelEnergy(x, y + 1) - getPixelEnergy(x, y - 1));
				}else {
					M_CostHorizontal[y][x] = currentEnergy + getMinCostValueHorizontal(M_CostHorizontal, x, y);
				}
			}
		}

		logger.log("Done vertical seam...");

		return M_CostHorizontal;
	}

	//0 - R, 1 - V, 2 - L (The cost of the new edge created by removing the specific pixel vertically)
	private double[] calcEdgeCostVertical (int x, int y){
		double cR, cL, cV;

		if (x == 0) {
			cL = 0;
			cV = 255;
			cR = Math.abs(getRGBCoordinate(x + 1, y) - getRGBCoordinate(x, y - 1));
		} else if (x == actualWidth - 1) {
			cL = Math.abs(getRGBCoordinate(x, y - 1) - getRGBCoordinate(x - 1, y));
			cV = 255;
			cR = 0;
		} else {
			cL = Math.abs(getRGBCoordinate(x + 1, y) - getRGBCoordinate(x - 1, y)) +
					Math.abs(getRGBCoordinate(x, y - 1) - getRGBCoordinate(x - 1, y));
			cV = Math.abs(getRGBCoordinate(x + 1, y) - getRGBCoordinate(x - 1, y));
			cR = Math.abs(getRGBCoordinate(x + 1, y) - getRGBCoordinate(x - 1, y)) +
					Math.abs(getRGBCoordinate(x + 1, y) - getRGBCoordinate(x, y - 1));
		}

		return new double[] { cR, cV, cL };
	}

	//0 - U, 1 - M, 2 - B (The cost of the new edge created by removing the specific pixel horizontally)
	private double[] calcEdgeCostHorizontal (int x, int y){
		double cU, cB, cM;

		if (y == 0) {
			cB = Math.abs(getRGBCoordinate(x - 1, y) - getRGBCoordinate(y + 1, y));
			cM = 255;
			cU = 0;
		} else if (y == actualHeight - 1) {
			cB = 0;
			cM = 255;
			cU = Math.abs(getRGBCoordinate(x, y - 1) - getRGBCoordinate(x - 1, y));
		} else {
			cB = Math.abs(getRGBCoordinate(x, y + 1) - getRGBCoordinate(x - 1, y)) +
					Math.abs(getRGBCoordinate(x, y - 1) - getRGBCoordinate(x, y + 1));
			cM = Math.abs(getRGBCoordinate(x, y + 1) - getRGBCoordinate(x, y - 1));
			cU = Math.abs(getRGBCoordinate(x, y - 1) - getRGBCoordinate(x - 1, y)) +
					Math.abs(getRGBCoordinate(x, y - 1) - getRGBCoordinate(x, y + 1));
		}

		return new double[] { cU, cM, cB };
	}

	//calculate the minimal edge value added by removing the pixel vertically
	private double getMinCostValueVertical (double[][] M_Vertical, int x, int y){
		double[] edgesCost = calcEdgeCostVertical(x , y);

		double costR = edgesCost[0];
		double costV = edgesCost[1] + M_Vertical[y - 1][x];
		double costL = edgesCost[2];

		double minCostValue;

		if (x == 0){
			costR = M_Vertical[y - 1][x + 1] + costR;

			minCostValue = Math.min(costV, costR);
		} else if (x == actualWidth - 1){
			costL = M_Vertical[y - 1][x - 1] + costL;

			minCostValue = Math.min(costV, costL);
		} else {
			costL = M_Vertical[y - 1][x - 1] + costL;
			costR = M_Vertical[y - 1][x + 1] + costR;

			minCostValue = Math.min(costR, Math.min(costV, costL));
		}

		return minCostValue;
	}

	//calculate the minimal edge value added by removing the pixel horizontally
	private double getMinCostValueHorizontal (double[][] M_Vertical, int x, int y){
		double[] edgesCost = calcEdgeCostHorizontal(x , y);


		double costU = edgesCost[0];
		double costM = edgesCost[1] + M_Vertical[y][x - 1];
		double costB = edgesCost[2];

		double minCostValue;

		if (y == 0){
			costB = M_Vertical[y + 1][x - 1] + costB;

			minCostValue = Math.min(costM, costB);
		} else if (y == actualHeight - 1){
			costU = M_Vertical[y - 1][x - 1] + costU;

			minCostValue = Math.min(costM, costU);
		} else {
			costB = M_Vertical[y + 1][x - 1] + costB;
			costU = M_Vertical[y - 1][x - 1] + costU;

			minCostValue = Math.min(costU, Math.min(costM, costB));
		}

		return minCostValue;
	}

	//get the RGB in the corresponding coordinates in the original image
	private int getRGBCoordinate(int x, int y){
		Coordinate coordinate = getSuitableCoordinate(x, y);
		return greyImage.getRGB(coordinate.X, coordinate.Y) & 0xFF;
	}

	//find the optimal vertical seam to remove
	private Seam verticalSeam(){

		double[][] M_Vertical = CalcCostVertical();

		logger.log("Find vertical seam...");

		Seam currentSeam = new Seam();

		//Find Optimal Seam
		int minX = 0;
		float minVal = Float.MAX_VALUE;

		for (int x = 0; x < actualWidth; x ++){
			if (minVal > M_Vertical[actualHeight - 1][x]){
				minVal = (float)M_Vertical[actualHeight - 1][x];
				minX = x;
			}
		}

		Coordinate currentCoordinate = new Coordinate(minX, actualHeight - 1);
		currentSeam.AddPixel(currentCoordinate);

		for (int y = actualHeight - 1; y >= 1; y --){
			double currentEnergy = getPixelEnergy(minX, y);
			double[] costEdges = calcEdgeCostVertical(minX, y);
			double cR = costEdges[0], cV = costEdges[1], cL = costEdges[2];

			if (minX < actualWidth - 1 && minVal == (float)(currentEnergy + M_Vertical[y - 1][minX + 1] + cR)){
				minX ++;
			}
			else if (minX > 0 && minVal == (float)(currentEnergy + M_Vertical[y - 1][minX - 1] + cL)){
				minX --;
			}
			else if (minVal != (float)(currentEnergy + M_Vertical[y - 1][minX] + cV)) {
				logger.log(minX + ", " + y);
			}

			minVal = (float)M_Vertical[y - 1][minX];

			currentCoordinate = new Coordinate(minX, y - 1);
			currentSeam.AddPixel(currentCoordinate);
		}

		logger.log("Found vertical seam...");

		return currentSeam;
	}

	//find the optimal horizontal seam to remove
	private Seam horizontalSeam(){

		double[][] M_Horizontal = CalcCostHorizontal();

		logger.log("Find horizontal seam...");

		Seam currentSeam = new Seam();

		//Find Optimal Seam
		int minY = 0;
		float minVal = Float.MAX_VALUE;

		for (int y = 0; y < actualHeight; y ++){
			if (minVal > M_Horizontal[y][actualWidth - 1]){
				minVal = (float)M_Horizontal[y][actualWidth - 1];
				minY = y;
			}
		}

		Coordinate currentCoordinate = new Coordinate(actualWidth - 1, minY);
		currentSeam.AddPixel(currentCoordinate);

		for (int x = actualWidth - 1; x >= 1; x --){
			double currentEnergy = getPixelEnergy(x, minY);
			double[] costEdges = calcEdgeCostHorizontal(x, minY);
			double cU = costEdges[0], cM = costEdges[1], cB = costEdges[2];

			if (minY < actualHeight - 1 && minVal == (float)(currentEnergy + M_Horizontal[minY + 1][x - 1] + cB)){
				minY ++;
			}
			else if (minY > 0 && minVal == (float)(currentEnergy + M_Horizontal[minY - 1][x - 1] + cU)){
				minY --;
			}

			minVal = (float)M_Horizontal[minY][x - 1];

			currentCoordinate = new Coordinate(x - 1, minY);
			currentSeam.AddPixel(currentCoordinate);
		}

		logger.log("Found horizontal seam...");

		return currentSeam;
	}

	//remove the given horizontal seam
	private void removeHorizontalSeam(Seam seam){

		logger.log("Removing horizontal seam...");

		actualHeight --;

		for (Coordinate c : seam.pixels) {
			for (int y = c.Y; y < actualHeight; y++) {
				coordinates[y][c.X] = coordinates[y + 1][c.X];
			}
		}

		logger.log("Finished removing vertical seam...");
	}

	//remove the given vertical seam
	private void removeVerticalSeam(Seam seam){

		logger.log("Removing vertical seam...");

		actualWidth --;

		for (Coordinate c : seam.pixels)
			for (int x = c.X; x < actualWidth; x++)
				coordinates[c.Y][x] = coordinates[c.Y][x + 1];

		logger.log("Finished removing vertical seam...");
	}

	//returns the energy of the given pixel
	private double getPixelEnergy(int x, int y){
		Coordinate coordinate = getSuitableCoordinate(x, y);
		return M_Energy[coordinate.Y][coordinate.X];
	}

	//calculate the energy of the given pixel (gradient)
	private double calcPixelEnergy(int x, int y){
		double Ex = Math.abs(getRGBCoordinate(x, y) - (x < actualWidth - 1 ? getRGBCoordinate(x + 1, y) : getRGBCoordinate(x - 1, y)));
		double Ey = Math.abs(getRGBCoordinate(x, y) - (y < actualHeight - 1 ? getRGBCoordinate(x, y + 1) : getRGBCoordinate(x, y - 1)));

		Ex = Math.pow(Ex, 2);
		Ey = Math.pow(Ey, 2);

		return Math.sqrt(Ex + Ey);
	}

	//returns the coordinates of the corresponding pixel in the image after resizing
	private Coordinate getSuitableCoordinate(int x, int y){
		return coordinates[y][x];
	}

	//show all seams to be removed
	public BufferedImage showSeams(boolean showVerticalSeams, int seamColorRGB) {
		int numberOfVerticalSeamsToCarve = Math.abs(this.outWidth - this.inWidth);
		int numberOfHorizontalSeamsToCarve = Math.abs(this.outHeight - this.inHeight);

		BufferedImage ans = duplicateWorkingImage();

		if(showVerticalSeams){
			for (int i = 0; i < numberOfVerticalSeamsToCarve; i ++){

				Seam seam = verticalSeam();
				seams.add(seam);

				ColorizePixels(seam, ans, seamColorRGB);

				removeVerticalSeam(seam);
			}
		} else {
			for (int i = 0; i < numberOfHorizontalSeamsToCarve; i ++){
				Seam seam = horizontalSeam();
				seams.add(seam);

				ColorizePixels(seam, ans, seamColorRGB);

				removeHorizontalSeam(seam);
			}
		}

		return ans;
	}

	//color the specify seam
	private void ColorizePixels(Seam seam, BufferedImage ans, int seamColorRGB){
		for (Coordinate coordinate : seam.pixels) {
			Coordinate realCoordinate = getSuitableCoordinate(coordinate.X, coordinate.Y);
			ans.setRGB(realCoordinate.X, realCoordinate.Y, seamColorRGB);
		}

	}
}