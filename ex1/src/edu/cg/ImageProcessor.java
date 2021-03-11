package edu.cg;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class ImageProcessor extends FunctioalForEachLoops {
	
	//MARK: Fields
	public final Logger logger;
	public final BufferedImage workingImage;
	public final RGBWeights rgbWeights;
	public final int inWidth;
	public final int inHeight;
	public final int workingImageType;
	public final int outWidth;
	public final int outHeight;
	
	//MARK: Constructors
	public ImageProcessor(Logger logger, BufferedImage workingImage,
			RGBWeights rgbWeights, int outWidth, int outHeight) {
		super(); //Initializing for each loops...
		
		this.logger = logger;
		this.workingImage = workingImage;
		this.rgbWeights = rgbWeights;
		inWidth = workingImage.getWidth();
		inHeight = workingImage.getHeight();
		workingImageType = workingImage.getType();
		this.outWidth = outWidth;
		this.outHeight = outHeight;
		setForEachInputParameters();
	}
	
	public ImageProcessor(Logger logger,
			BufferedImage workingImage,
			RGBWeights rgbWeights) {
		this(logger, workingImage, rgbWeights,
				workingImage.getWidth(), workingImage.getHeight());
	}
	
	//MARK: Change picture hue - example
	public BufferedImage changeHue() {
		logger.log("Prepareing for hue changing...");
		
		int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;
		int max = rgbWeights.maxWeight;
		
		BufferedImage ans = newEmptyInputSizedImage();
		
		forEach((y, x) -> {
			Color c = new Color(workingImage.getRGB(x, y));
			int red = r*c.getRed() / max;
			int green = g*c.getGreen() / max;
			int blue = b*c.getBlue() / max;
			Color color = new Color(red, green, blue);
			ans.setRGB(x, y, color.getRGB());
		});
		
		logger.log("Changing hue done!");
		
		return ans;
	}
	
	//MARK: Nearest neighbor - example
	public BufferedImage nearestNeighbor() {
		logger.log("applies nearest neighbor interpolation.");
		BufferedImage ans = newEmptyOutputSizedImage();
		
		pushForEachParameters();
		setForEachOutputParameters();
		
		forEach((y, x) -> {
			int imgX = (int)Math.round((x*inWidth) / ((float)outWidth));
			int imgY = (int)Math.round((y*inHeight) / ((float)outHeight));
			imgX = Math.min(imgX,  inWidth-1);
			imgY = Math.min(imgY, inHeight-1);
			ans.setRGB(x, y, workingImage.getRGB(imgX, imgY));
		});
		
		popForEachParameters();
		
		return ans;
	}
	
	//MARK: Unimplemented methods
	public BufferedImage greyscale() {
		logger.log("Prepareing for grey scale...");

		int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;
		int weightsSum = rgbWeights.weightsSum;

		BufferedImage ans = newEmptyInputSizedImage();

		forEach((y, x) -> {
			Color c = new Color(workingImage.getRGB(x, y));
			int greyValue = ((r * c.getRed()) + (g * c.getGreen()) + (b * c.getBlue())) / weightsSum;
			Color color = new Color(greyValue, greyValue, greyValue);
			ans.setRGB(x, y, color.getRGB());
		});

		logger.log("Grey scale is done!");

		return ans;
	}

	public BufferedImage gradientMagnitude() {
		logger.log("Prepareing for gradient magnitude...");

		BufferedImage grey = greyscale();
		BufferedImage ans = newEmptyInputSizedImage();

		forEach((y, x) -> {
			Color current = new Color(workingImage.getRGB(x, y));
			int gradientX;
			int gradientY;

			if (x < grey.getWidth() - 1){
				//Calc Forward
				gradientX = Differencing(new Color(workingImage.getRGB(x + 1, y)), current);
			}
			else{
				//Calc Backward
				gradientX = Differencing(current, new Color(workingImage.getRGB(x - 1, y)));
			}


			if (y < grey.getHeight() - 1){
				//Calc Forward
				gradientY = Differencing(new Color(workingImage.getRGB(x, y + 1)), current);
			}
			else{
				//Calc Backward
				gradientY = Differencing(current, new Color(workingImage.getRGB(x, y - 1)));
			}

			//Do gradientX and gradientY power in 2
			gradientX = (int)Math.pow(gradientX, 2);
			gradientY = (int)Math.pow(gradientY, 2);

			//Sqrt of Avg
			int gradientMagnitude = (int)Math.sqrt((gradientX + gradientY) / 2);

			Color color = new Color(gradientMagnitude, gradientMagnitude, gradientMagnitude);
			ans.setRGB(x, y, color.getRGB());
		});

		logger.log("Gradient magnitude is done!");

		return ans;
	}

	private int Differencing(Color c1, Color c2){
		return c1.getRed() - c2.getRed();
	}

	public BufferedImage bilinear() {
		logger.log("Prepareing for bilinear interpolation...");

		BufferedImage ans = newEmptyOutputSizedImage();

		forEach((y, x) -> {

			if (y == 0 && x == 2)
			{
				logger.log("Maya");
			}
			try {
				int x1 = (int)Math.round((x*inWidth) / ((float)outWidth));
				int y1 = (int)Math.round((y*inHeight) / ((float)outHeight));
				x1 = Math.min(x1,  inWidth-2);
				y1 = Math.min(y1, inHeight-2);

				int x2 = x1 + 1;
				int y2 = y1 + 1;

				Color Q11 = new Color(workingImage.getRGB(x1, y1));
				Color Q21 = new Color(workingImage.getRGB(x2, y1));
				Color Q12 = new Color(workingImage.getRGB(x1, y2));
				Color Q22 = new Color(workingImage.getRGB(x2, y2));

				final int denominator = ((x2 - x1) * (y2 - y1));

				Color C11 = ScalarMultColor(Q11, ((x2 - x) * (y2 - y)) / denominator);
				Color C21 = ScalarMultColor(Q21, ((x - x1) * (y2 - y)) / denominator);
				Color C12 = ScalarMultColor(Q12, ((x2 - x) * (y - y1)) / denominator);
				Color C22 = ScalarMultColor(Q22, ((x - x1) * (y - y1)) / denominator);

				Color P = new Color(C11.getRed() + C12.getRed() + C21.getRed() + C22.getRed(),
						C11.getGreen() + C12.getGreen() + C21.getGreen() + C22.getGreen(),
						C11.getBlue() + C12.getBlue() + C21.getBlue() + C22.getBlue());

				ans.setRGB(x, y, P.getRGB());
			}catch (Exception e){
				logger.log(x + ", " + y);
			}
		});

		logger.log("Bilinear interpolation is done!");

		return ans;
	}

	private Color ScalarMultColor (Color c, int scalar){
		int r = c.getRed() * scalar;
		int b = c.getBlue() * scalar;
		int g = c.getGreen() * scalar;

		return new Color(r, g, b);
	}
	//MARK: Utilities
	public final void setForEachInputParameters() {
		setForEachParameters(inWidth, inHeight);
	}
	
	public final void setForEachOutputParameters() {
		setForEachParameters(outWidth, outHeight);
	}
	
	public final BufferedImage newEmptyInputSizedImage() {
		return newEmptyImage(inWidth, inHeight);
	}
	
	public final BufferedImage newEmptyOutputSizedImage() {
		return newEmptyImage(outWidth, outHeight);
	}
	
	public final BufferedImage newEmptyImage(int width, int height) {
		return new BufferedImage(width, height, workingImageType);
	}
	
	public final BufferedImage duplicateWorkingImage() {
		BufferedImage output = newEmptyInputSizedImage();
		
		forEach((y, x) -> 
			output.setRGB(x, y, workingImage.getRGB(x, y))
		);
		
		return output;
	}
}
