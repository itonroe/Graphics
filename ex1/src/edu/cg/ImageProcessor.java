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
		if (inWidth < 2 || inHeight < 2){
			logger.log("Image is too small");
			return null;
		}

		logger.log("Prepareing for gradient magnitude...");

		BufferedImage grey = greyscale();
		BufferedImage ans = newEmptyInputSizedImage();

		forEach((y, x) -> {
			Color current = new Color(grey.getRGB(x, y));
			int gradientX;
			int gradientY;

			if (x < grey.getWidth() - 1){
				//Calc Forward
				gradientX = Differencing(new Color(grey.getRGB(x + 1, y)), current);
			}
			else{
				//Calc Backward
				gradientX = Differencing(current, new Color(grey.getRGB(x - 1, y)));
			}


			if (y < grey.getHeight() - 1){
				//Calc Forward
				gradientY = Differencing(new Color(grey.getRGB(x, y + 1)), current);
			}
			else{
				//Calc Backward
				gradientY = Differencing(current, new Color(grey.getRGB(x, y - 1)));
			}

			//Do gradientX and gradientY power in 2
			gradientX = (int)Math.pow(gradientX, 2);
			gradientY = (int)Math.pow(gradientY, 2);

			//Sqrt of Avg
			int gradientMagnitude = 255 - (int)Math.sqrt((gradientX + gradientY) / 2);

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

		for(int x = 0; x < outWidth; x++)
			for(int y = 0; y < outHeight; y++){

			if (y == 0 && x == 650)
			{
				logger.log("Maya");
			}
			try {
				float fx = ((float)x / outWidth) * (inWidth - 1);
				float fy = ((float)y / outHeight) * (inHeight - 1);
				int x1 = (int)fx;
				int y1 = (int)fy;
				x1 = Math.min(x1,  inWidth-2);
				y1 = Math.min(y1, inHeight-2);

				int x2 = x1 + 1;
				int y2 = y1 + 1;

				Color Q11 = new Color(workingImage.getRGB(x1, y1));
				Color Q21 = new Color(workingImage.getRGB(x2, y1));
				Color Q12 = new Color(workingImage.getRGB(x1, y2));
				Color Q22 = new Color(workingImage.getRGB(x2, y2));

				int red12 = (int)lerp(Q11.getRed(), Q21.getRed(), fx - x1);
				int green12 = (int)lerp(Q11.getGreen(), Q21.getGreen(), fx - x1);
				int blue12 = (int)lerp(Q11.getBlue(), Q21.getBlue(), fx - x1);
				int red34 = (int)lerp(Q12.getRed(), Q22.getRed(), fx - x1);
				int green34 = (int)lerp(Q12.getGreen(), Q22.getGreen(), fx - x1);
				int blue34 = (int)lerp(Q12.getBlue(), Q22.getBlue(), fx - x1);

				int red = (int)lerp(red12, red34, fy - y1);
				int green = (int)lerp(green12, green34, fy - y1);
				int blue = (int)lerp(blue12, blue34, fy - y1);

				Color P = new Color(red, green, blue);
				ans.setRGB(x, y, P.getRGB());
			}catch (Exception e){
				logger.log(x + ", " + y);
			}
		};

		logger.log("Bilinear interpolation is done!");

		return ans;
	}

	private static float lerp(int c1, int c2, float t) {
		return (1 - t) * c1 + t * c2;
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
