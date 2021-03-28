package edu.cg;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;


public class BasicSeamsCarver extends ImageProcessor {
	
	// An enum describing the carving scheme used by the seams carver.
	// VERTICAL_HORIZONTAL means vertical seams are removed first.
	// HORIZONTAL_VERTICAL means horizontal seams are removed first.
	// INTERMITTENT means seams are removed intermittently : vertical, horizontal, vertical, horizontal etc.
	public static enum CarvingScheme {
		VERTICAL_HORIZONTAL("Vertical seams first"),
		HORIZONTAL_VERTICAL("Horizontal seams first"),
		INTERMITTENT("Intermittent carving");
		
		public final String description;
		
		private CarvingScheme(String description) {
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
	double M_Vertical[][];
	double M_Horizontal[][];
	int actualWidth;
	int actualHeight;
	Coordinate coordinates[][];

	public BasicSeamsCarver(Logger logger, BufferedImage workingImage,
			int outWidth, int outHeight, RGBWeights rgbWeights) {
		super((s) -> logger.log("Seam carving: " + s), workingImage, rgbWeights, outWidth, outHeight);

		greyImage = greyscale();
		seams = new ArrayList<>();
		actualWidth = workingImage.getWidth();
		actualHeight = workingImage.getHeight();
		M_Vertical = new double[actualHeight][actualWidth];
		M_Horizontal = new double[actualHeight][actualWidth];
		coordinates = new Coordinate[actualHeight][actualWidth];
	}

	public BufferedImage carveImage(CarvingScheme carvingScheme) {
		int numberOfVerticalSeamsToCarve = Math.abs(this.outWidth - this.inWidth);
		int numberOfHorizontalSeamsToCarve = Math.abs(this.outHeight - this.inHeight);
		// TODO :  Perform Seam Carving. Overall you need to remove 'numberOfVerticalSeamsToCarve' vertical seams
		// and 'numberOfHorizontalSeamsToCarve' horizontal seams from the image.
		// Note you must consider the 'carvingScheme' parameter in your procedure.
		// Return the resulting image.

		logger.log("Preparing to carve image...");

		setForEachInputParameters();

		InitializeCoordinates();
		InitializeVerticalSeam();
		InitializeHorizontalSeam();

		if(carvingScheme.description.equals("Vertical seams first")){

			for (int i = 0; i < numberOfVerticalSeamsToCarve; i ++){
				Seam seam = verticalSeam();
				seams.add(seam);
				removeVerticalSeam(seam);
			}

			for (int i = 0; i < numberOfHorizontalSeamsToCarve; i ++){
				Seam seam = horizontalSeam();
				seams.add(seam);
				removeHorizontalSeam(seam);
			}
		}else if(carvingScheme.description.equals("Horizontal seams first")){

			for (int i = 0; i < numberOfHorizontalSeamsToCarve; i ++){
				Seam seam = horizontalSeam();
				seams.add(seam);
				removeHorizontalSeam(seam);
			}

			for (int i = 0; i < numberOfVerticalSeamsToCarve; i ++){
				Seam seam = verticalSeam();
				seams.add(seam);
				removeVerticalSeam(seam);
			}
		}else{
			while(numberOfHorizontalSeamsToCarve > 0 && numberOfVerticalSeamsToCarve > 0){
				Seam seam = verticalSeam();
				seams.add(seam);
				removeVerticalSeam(seam);
				numberOfVerticalSeamsToCarve--;

				seam = horizontalSeam();
				seams.add(seam);
				removeHorizontalSeam(seam);
				numberOfHorizontalSeamsToCarve--;

			}

			while(numberOfHorizontalSeamsToCarve > 0){
				Seam seam = horizontalSeam();
				seams.add(seam);
				removeHorizontalSeam(seam);
				numberOfHorizontalSeamsToCarve--;
			}

			while(numberOfVerticalSeamsToCarve > 0){
				Seam seam = verticalSeam();
				seams.add(seam);
				removeVerticalSeam(seam);
				numberOfVerticalSeamsToCarve--;

			}
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

	private void InitializeCoordinates() {
		forEach((y, x) -> {
			coordinates[y][x] = new Coordinate(x,y);
		});
	}

	private void InitializeHorizontalSeam(){

		logger.log("Initialize horizontal seam...");

		//Initialize Matrix Value
		forEach((y, x) -> {
			updateEnergyHorizontal(x, y);
		});

		logger.log("Done horizontal seam...");
	}

	private void InitializeVerticalSeam(){

		logger.log("Initialize vertical seam...");

		//Initialize Cost Matrix Value
		for (int y = 0; y < actualHeight; y ++ ){
			for (int x = 0; x < actualWidth; x ++){
				calcCostVertical(x, y);
			}
		}

		logger.log("Done vertical seam...");
	}

	private void calcCostVertical(int x, int y){
		double currentEnergy = getPixelEnergy(x, y);

		if (y == 0){
			M_Vertical[y][x] = (x == 0 || x == actualWidth - 1) ? currentEnergy : currentEnergy + Math.abs(getPixelEnergy(x + 1, y) - getPixelEnergy(x - 1, y));
		}else {
			double L, R, V;

			if (x == 0) {
				L = Integer.MAX_VALUE;
				V = M_Vertical[y - 1][x];
				R = M_Vertical[y - 1][x + 1] + Math.abs(getRGBCoordinate(x + 1, y) - getRGBCoordinate(x, y - 1));
			} else if (x == actualWidth - 1) {
				L = M_Vertical[y - 1][x - 1] + Math.abs(getRGBCoordinate(x, y - 1) - getRGBCoordinate(x - 1, y));
				V = M_Vertical[y - 1][x];
				R = Integer.MAX_VALUE;
			} else {
				L = M_Vertical[y - 1][x - 1] + Math.abs(getRGBCoordinate(x + 1, y) - getRGBCoordinate(x - 1, y)) + Math.abs(getRGBCoordinate(x, y - 1) - getRGBCoordinate(x - 1, y));
				V = M_Vertical[y - 1][x] + Math.abs(getRGBCoordinate(x + 1, y) - getRGBCoordinate(x - 1, y));
				R = M_Vertical[y - 1][x + 1] + Math.abs(getRGBCoordinate(x + 1, y) - getRGBCoordinate(x - 1, y)) + Math.abs(getRGBCoordinate(x + 1, y) - getRGBCoordinate(x, y - 1));
			}

			M_Vertical[y][x] = currentEnergy + Math.min(L, Math.min(R, V));
		}
	}

	private int getRGBCoordinate(int x, int y){
		Coordinate coordinate = getSuitableCoordinate(x, y);
		return greyImage.getRGB(coordinate.X, coordinate.Y);
	}

	private void updateEnergyHorizontal(int x, int y) {
		double currentEnergy = getPixelEnergy(x, y);

		if (x == 0){
			M_Horizontal[y][x] = currentEnergy;
		}else {
			double T, B, V;

			if (y == 0) {
				T = Integer.MAX_VALUE;
				V = M_Horizontal[y][x - 1];
				B = M_Horizontal[y + 1][x - 1] + Math.abs(greyImage.getRGB(x - 1, y) - greyImage.getRGB(x, y + 1));
			} else if (y >= actualHeight - 1) {
				T = M_Horizontal[y - 1][x - 1] + Math.abs(greyImage.getRGB(x, y - 1) - greyImage.getRGB(x - 1, y));
				V = M_Horizontal[y][x - 1];
				B = Integer.MAX_VALUE;
			} else {
				T = M_Horizontal[y - 1][x - 1] + Math.abs(greyImage.getRGB(x, y - 1) - greyImage.getRGB(x - 1, y)) + Math.abs(greyImage.getRGB(x, y - 1) - greyImage.getRGB(x, y + 1));
				V = M_Horizontal[y][x - 1] + Math.abs(greyImage.getRGB(x, y - 1) - greyImage.getRGB(x, y + 1));
				B = M_Horizontal[y + 1][x - 1] + Math.abs(greyImage.getRGB(x , y - 1) - greyImage.getRGB(x, y + 1)) + Math.abs(greyImage.getRGB(x - 1, y) - greyImage.getRGB(x, y + 1));
			}

			M_Horizontal[y][x] = getPixelEnergy(x, y) + Math.min(T, Math.min(B, V));
		}
	}

	private Seam verticalSeam(){

		InitializeVerticalSeam();

		logger.log("Find vertical seam...");

		Seam currentSeam = new Seam();

		//Find Optimal Seam
		int minX = 0;
		double minVal = Integer.MAX_VALUE;

		for (int x = 0; x < actualWidth; x ++){
			if (minVal > M_Vertical[actualHeight - 1][x]){
				minVal = M_Vertical[actualHeight - 1][x];
				minX = x;
			}
		}

		Coordinate currentCoordinate = new Coordinate(minX, actualHeight - 1);
		currentSeam.AddPixel(currentCoordinate);

		for (int y = actualHeight - 1; y >= 1; y --){
			double cL = 0,cV = 0;

			if (minX == actualWidth - 1) {
				cL = Math.abs(getRGBCoordinate(minX, y - 1) - getRGBCoordinate(minX - 1, y));
			} else if (minX > 0) {
				cL = Math.abs(getRGBCoordinate(minX + 1, y) - getRGBCoordinate(minX - 1, y)) + Math.abs(getRGBCoordinate(minX, y - 1) - getRGBCoordinate(minX - 1, y));
				cV = Math.abs(getRGBCoordinate(minX + 1, y) - getRGBCoordinate(minX - 1, y));
			}

			if (minVal != (getPixelEnergy(minX, y) + M_Vertical[y - 1][minX] + cV)){
				if (minX > 0 && minVal == (getPixelEnergy(minX, y) + M_Vertical[y - 1][minX - 1] + cL)){
					minX --;
				}
				else if (minX < actualWidth){
					minX ++;
				}
			}

			minVal = M_Vertical[y - 1][minX];

			currentCoordinate = new Coordinate(minX, y - 1);
			currentSeam.AddPixel(currentCoordinate);
		}

		logger.log("Found vertical seam...");

		return currentSeam;
	}

	private Seam horizontalSeam(){

		logger.log("Find horizontal seam...");

		Seam currentSeam = new Seam();

		//Find Optimal Seam
		int minY = 0;
		double minVal = Integer.MAX_VALUE;

		for (int y = 0; y < M_Horizontal.length; y ++){
			if (minVal > M_Horizontal[y][M_Horizontal[0].length - 1]){
				minVal = M_Horizontal[y][M_Horizontal[0].length - 1];
				minY = y;
			}
		}

		Coordinate currentCoordinate = new Coordinate(M_Horizontal[0].length - 1, minY);
		currentSeam.AddPixel(currentCoordinate);

		for (int x = M_Horizontal[0].length - 2; x >= 0; x --){
			if (minY == 0){
				minY = M_Horizontal[minY][x] > M_Horizontal[minY + 1][x] ? minY + 1 : minY;
			}
			else if (minY >= actualHeight - 1) {
				minY = M_Horizontal[minY][x] > M_Horizontal[minY - 1][x] ? minY - 1 : minY;
			}
			else if (M_Horizontal[minY - 1][x] > M_Horizontal[minY][x]){
				minY = M_Horizontal[minY][x] > M_Horizontal[minY + 1][x] ? minY + 1 : minY;
			}
			else{
				minY = M_Horizontal[minY - 1][x] > M_Horizontal[minY + 1][x] ? minY + 1 : minY - 1;
			}

			currentCoordinate = new Coordinate(x, minY);
			currentSeam.AddPixel(currentCoordinate);
		}

		logger.log("Found horizontal seam...");

		return currentSeam;
	}

	private void removeHorizontalSeam(Seam seam){

		logger.log("Removing horizontal seam...");

		actualHeight --;

		for (Coordinate c : seam.pixels) {
			try {
				if (c.Y > 0) {
					updateEnergyHorizontal(c.X, c.Y - 1);
				}

				if (c.Y < actualHeight - 1){
					updateEnergyHorizontal(c.X, c.Y + 1);
				}

				for (int y = c.Y; y < actualHeight; y++) {
					M_Horizontal[y][c.X] = M_Horizontal[y + 1][c.X];
					coordinates[y][c.X] = coordinates[y + 1][c.X];
				}
			}catch (Exception e){
				logger.log("Failed Here: " + c.X + ", " + c.Y);
			}

		}

		logger.log("Finished removing vertical seam...");
	}

	private void removeVerticalSeam(Seam seam){

		logger.log("Removing vertical seam...");

		actualWidth --;

		List<Coordinate> coordinatesList = seam.pixels;

		//Reversed List, Top to Bottom
		for (int i = coordinatesList.size() - 1; i >= 0 ; i--) {
			Coordinate c = coordinatesList.get(i);

			for (int x = c.X; x < actualWidth; x++) {
				M_Vertical[c.Y][x] = M_Vertical[c.Y][x + 1];
				coordinates[c.Y][x] = coordinates[c.Y][x + 1];
			}

			/*if (c.X > 0) {
				calcCostVertical(c.X - 1, c.Y);
			}

			if (c.X < actualWidth){
				calcCostVertical(c.X, c.Y);
			}*/

		}

		logger.log("Finished removing vertical seam...");
	}

	private double getPixelEnergy(int x, int y){
		Coordinate coordinate = getSuitableCoordinate(x, y);

		double Ex = Math.abs(getRGBCoordinate(x, y) - (coordinate.X < actualWidth - 1 ? getRGBCoordinate(x + 1, y) : getRGBCoordinate(x - 1, y)));
		double Ey = Math.abs(getRGBCoordinate(x, y) - (coordinate.Y < actualHeight - 1 ? getRGBCoordinate(x, y + 1) : getRGBCoordinate(x, y - 1)));

		Ex = Math.pow(Ex, 2);
		Ey = Math.pow(Ey, 2);

		return Math.sqrt(Ex + Ey);
	}

	private Coordinate getSuitableCoordinate(int x, int y){
		return coordinates[y][x];
	}
	
	public BufferedImage showSeams(boolean showVerticalSeams, int seamColorRGB) {
		int numberOfVerticalSeamsToCarve = Math.abs(this.outWidth - this.inWidth);
		int numberOfHorizontalSeamsToCarve = Math.abs(this.outHeight - this.inHeight);

		setForEachInputParameters();
		InitializeCoordinates();

		BufferedImage ans = duplicateWorkingImage();
		if(showVerticalSeams){

			for (int i = 0; i < numberOfVerticalSeamsToCarve; i ++){
				Seam seam = verticalSeam();
				seams.add(seam);
				removeVerticalSeam(seam);
			}

			for (int i = 0; i < numberOfVerticalSeamsToCarve; i++) {
				for (Coordinate coordinate : seams.get(i).pixels)
				{
					ans.setRGB(coordinate.X, coordinate.Y, seamColorRGB);
				}
			}
		} else {
			InitializeHorizontalSeam();

			for (int i = 0; i < numberOfHorizontalSeamsToCarve; i ++){
				Seam seam = horizontalSeam();
				seams.add(seam);
				removeHorizontalSeam(seam);
			}
			for (int i = 0; i < numberOfHorizontalSeamsToCarve; i++) {
				for (int j = 0; j < inWidth; j++) {
					ans.setRGB(j, seams.get(i).pixels.get(j).Y, seamColorRGB);
				}
			}
		}

		return ans;
	}
}
