package edu.cg;

import java.awt.*;
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
	double M[][];
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
		M = new double[actualHeight][actualWidth];
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
		/*
		InitializeVerticalSeam();

		for (int i = 0; i < numberOfVerticalSeamsToCarve; i ++){
			Seam seam = verticalSeam();
			seams.add(seam);
			removeVerticalSeam(seam);
		}
		*/

		InitializeHorizontalSeam();

		for (int i = 0; i < numberOfHorizontalSeamsToCarve; i ++){
			Seam seam = horizontalSeam();
			seams.add(seam);
			removeHorizontalSeam(seam);
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

	private void removeHorizontalSeam(Seam seam){

		logger.log("Removing horizontal seam...");

		actualHeight --;

		for (Coordinate c : seam.pixels) {
			if (c.X == 162 && c.Y == 442){
				logger.log("MAYA");
			}

			try {
				updateEnergyHorizontal(c.X, c.Y - 1);
				updateEnergyHorizontal(c.X, c.Y + 1);

				for (int y = c.Y; y < actualHeight; y ++){
					M[y][c.X] = M[y + 1][c.X];
					coordinates[y][c.X] = coordinates[y + 1][c.X];
				}
			}
			catch (Exception e){
				logger.log(c.X + ", " + c.Y);
			}
		}

		logger.log("Finished removing vertical seam...");
	}

	private void removeVerticalSeam(Seam seam){

		logger.log("Removing vertical seam...");

		actualWidth --;

		for (Coordinate c : seam.pixels) {
			updateEnergyVertical(c.X - 1, c.Y);
			updateEnergyVertical(c.X + 1, c.Y);

			for (int x = c.X; x < actualWidth; x ++){
				M[c.Y][x] = M[c.Y][x + 1];
				coordinates[c.Y][x] = coordinates[c.Y][x + 1];
			}
		}

		logger.log("Finished removing vertical seam...");
	}

	private void InitializeHorizontalSeam(){

		logger.log("Initialize horizontal seam...");

		//Initialize Matrix Value
		forEach((y, x) -> {
			try{
				if (y == 294 && x == 1){
					logger.log("r");
				}
				coordinates[y][x] = new Coordinate(x,y);
				updateEnergyHorizontal(x, y);
			}
			catch (Exception e){
				logger.log(x + ", " + y);
			}
		});

		logger.log("Done horizontal seam...");
	}

	private void InitializeVerticalSeam(){

		logger.log("Initialize vertical seam...");

		//Initialize Matrix Value
		forEach((y, x) -> {
			coordinates[y][x] = new Coordinate(x,y);
			updateEnergyVertical(x, y);
		});

		logger.log("Done vertical seam...");
	}

	private void updateEnergyVertical(int x, int y){
		double currentEnergy = getPixelEnergy(x, y);

		if (y <= 0){
			M[y][x] = currentEnergy;
		}else {
			double L, R, V = 0;

			if (x <= 0) {
				L = Integer.MAX_VALUE;
				V = M[y - 1][x];
				R = M[y - 1][x + 1] + Math.abs(greyImage.getRGB(x, y - 1) - greyImage.getRGB(x + 1, y));
			} else if (x >= actualWidth - 2) {
				L = M[y - 1][x - 1] + Math.abs(greyImage.getRGB(x, y - 1) - greyImage.getRGB(x - 1, y));
				V = M[y - 1][x];
				R = Integer.MAX_VALUE;
			} else {
				L = M[y - 1][x - 1] + Math.abs(greyImage.getRGB(x, y - 1) - greyImage.getRGB(x - 1, y));
				V = M[y - 1][x] + Math.abs(greyImage.getRGB(x + 1, y) - greyImage.getRGB(x - 1, y));
				R = M[y - 1][x + 1] + Math.abs(greyImage.getRGB(x, y - 1) - greyImage.getRGB(x + 1, y));
			}

			M[y][x] = getPixelEnergy(x, y) + Math.min(L, Math.min(R, V));
		}
	}

	private void updateEnergyHorizontal(int x, int y){
		double currentEnergy = getPixelEnergy(x, y);

		if (x <= 0){
			M[y][x] = currentEnergy;
		}else {
			double L, R, V = 0;

			if (y <= 0) {
				L = Integer.MAX_VALUE;
				V = M[y][x - 1];
				R = M[y + 1][x - 1] + Math.abs(greyImage.getRGB(x - 1, y) - greyImage.getRGB(x, y + 1));
			} else if (y >= actualHeight - 2) {
				L = M[y - 1][x - 1] + Math.abs(greyImage.getRGB(x - 1, y) - greyImage.getRGB(x, y - 1));
				V = M[y][x - 1];
				R = Integer.MAX_VALUE;
			} else {
				L = M[y - 1][x - 1] + Math.abs(greyImage.getRGB(x - 1, y) - greyImage.getRGB(x, y - 1));
				V = M[y][x - 1] + Math.abs(greyImage.getRGB(x, y + 1) - greyImage.getRGB(x, y - 1));
				R = M[y + 1][x - 1] + Math.abs(greyImage.getRGB(x - 1, y) - greyImage.getRGB(x, y + 1));
			}

			M[y][x] = getPixelEnergy(x, y) + Math.min(L, Math.min(R, V));
		}
	}

	private Seam verticalSeam(){

		logger.log("Find vertical seam...");

		Seam currentSeam = new Seam();

		//Find Optimal Seam
		int minX = 0;
		double minVal = Integer.MAX_VALUE;

		for (int x = 0; x < M[0].length; x ++){
			if (minVal > M[M.length - 1][x]){
				minVal = M[M.length - 1][x];
				minX = x;
			}
		}

		Coordinate currentCoordinate = new Coordinate(minX, M.length - 1);
		currentSeam.AddPixel(currentCoordinate);

		for (int y = M.length - 2; y >= 0; y --){
			if (minX <= 0){
				minX = M[y][minX] > M[y][minX + 1] ? minX + 1 : minX;
			}
			else if (minX >= actualWidth - 2) {
				minX = M[y][minX] > M[y][minX - 1] ? minX - 1 : minX;
			}
			else if (M[y][minX - 1] > M[y][minX]){
				minX = M[y][minX] > M[y][minX + 1] ? minX + 1 : minX;
			}
			else{
				minX = M[y][minX - 1] > M[y][minX + 1] ? minX + 1 : minX - 1;
			}

			currentCoordinate = new Coordinate(minX, y);
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

		for (int y = 0; y < M.length; y ++){
			if (minVal > M[y][M[0].length - 1]){
				minVal = M[y][M[0].length - 1];
				minY = y;
			}
		}

		Coordinate currentCoordinate = new Coordinate(M[0].length - 1, minY);
		currentSeam.AddPixel(currentCoordinate);

		for (int x = M[0].length - 2; x >= 0; x --){
			if (minY <= 0){
				minY = M[minY][x] > M[minY + 1][x] ? minY + 1 : minY;
			}
			else if (minY >= actualHeight - 2) {
				minY = M[minY][x] > M[minY - 1][x] ? minY - 1 : minY;
			}
			else if (M[minY - 1][x] > M[minY][x]){
				minY = M[minY][x] > M[minY + 1][x] ? minY + 1 : minY;
			}
			else{
				minY = M[minY - 1][x] > M[minY + 1][x] ? minY + 1 : minY - 1;
			}

			currentCoordinate = new Coordinate(minY, x);
			currentSeam.AddPixel(currentCoordinate);
		}

		logger.log("Found horizontal seam...");

		return currentSeam;
	}

	private double getPixelEnergy(int x, int y){
		int Ex = Math.abs(greyImage.getRGB(x, y) - (x < inWidth - 1 ?  greyImage.getRGB(x + 1, y) : greyImage.getRGB(x - 1, y)));
		int Ey = Math.abs(greyImage.getRGB(x, y) - (y < inHeight - 1 ?  greyImage.getRGB(x, y + 1) : greyImage.getRGB(x, y - 1)));
		return Ex + Ey;
	}
	
	public BufferedImage showSeams(boolean showVerticalSeams, int seamColorRGB) {
		int numberOfVerticalSeamsToCarve = Math.abs(this.outWidth - this.inWidth);
		int numberOfHorizontalSeamsToCarve = Math.abs(this.outHeight - this.inHeight);
		// TODO :  Present either vertical or horizontal seams on the input image.
				// If showVerticalSeams = true, carve 'numberOfVerticalSeamsToCarve' vertical seams from the image.
				// Then, generate a new image from the input image in which you mark all of the vertical seams that
				// were chosen in the Seam Carving process. 
				// This is done by painting each pixel in each seam with 'seamColorRGB' (overriding its' previous value). 
				// Similarly, if showVerticalSeams = false, carve 'numberOfHorizontalSeamsToCarve' horizontal seams
				// from the image.
				// Then, generate a new image from the input image in which you mark all of the horizontal seams that
				// were chosen in the Seam Carving process.
		throw new UnimplementedMethodException("showSeams");
	}
}
