import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JPanel;


public class Entry extends JPanel {

	protected Image entryImage;

	protected Graphics entryGraphics;

	protected int lastX = -1;

	protected int lastY = -1;
      
	protected Sample sample;

	protected int downSampleLeft;

	protected int downSampleRight;

	protected int downSampleTop;

	protected int downSampleBottom;

	protected double ratioX;

	protected double ratioY;

	protected int pixelMap[];

	Entry() {
		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK
				| AWTEvent.MOUSE_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
	}

	protected void initImage() {
		try {
			entryImage = createBitmap();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected Image createBitmap() throws IOException {
		String filename = "sample/B.bmp";

		FileInputStream inputStream = new FileInputStream(filename);
		String extensionName = filename
				.substring(filename.lastIndexOf('.') + 1);
		Iterator readers = ImageIO.getImageReadersBySuffix(extensionName);
		ImageReader imageReader = (ImageReader) readers.next();
		ImageInputStream imageInputStream = ImageIO
				.createImageInputStream(inputStream);
		imageReader.setInput(imageInputStream, false);
		int num = imageReader.getNumImages(true);
		BufferedImage images[] = new BufferedImage[num];
		for (int i = 0; i < num; ++i) {
			images[i] = imageReader.read(i);
		}
		inputStream.close();
		return images[0];
	}

	public void paint(Graphics g) {
		if (entryImage == null)
			initImage();
		g.drawImage(entryImage, 0, 0 , this);
		g.setColor(Color.black);
		g.drawRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.red);
		g.drawRect(downSampleLeft, downSampleTop, downSampleRight - downSampleLeft, downSampleBottom - downSampleTop);

	}

	protected void processMouseEvent(MouseEvent e) {
		if (e.getID() != MouseEvent.MOUSE_PRESSED)
			return;
		lastX = e.getX();
		lastY = e.getY();
	}


	public void setSample(Sample s) {
		sample = s;
	}

	public Sample getSample() {
		return sample;
	}
        
        
	protected void processMouseMotionEvent(MouseEvent e) {
		if (e.getID() != MouseEvent.MOUSE_DRAGGED)
			return;

		entryGraphics.setColor(Color.black);
		entryGraphics.drawLine(lastX, lastY, e.getX(), e.getY());
		getGraphics().drawImage(entryImage, 0, 0, this);
		lastX = e.getX();
		lastY = e.getY();
	}

	protected boolean hLineClear(int y) {
		int w = entryImage.getWidth(this);
		for (int i = 0; i < w; i++) {
			if (pixelMap[(y * w) + i] != -1)
				return false;
		}
		return true;
	}

	protected boolean vLineClear(int x) {
		int w = entryImage.getWidth(this);
		int h = entryImage.getHeight(this);
		for (int i = 0; i < h; i++) {
			if (pixelMap[(i * w) + x] != -1)
				return false;
		}
		return true;
	}

	protected void findBounds(int w, int h) {
		
		for (int y = 0; y < h; y++) {
			if (!hLineClear(y)) {
				downSampleTop = y;
				break;
			}

		}
		
		for (int y = h - 1; y >= 0; y--) {
			if (!hLineClear(y)) {
				downSampleBottom = y;
				break;
			}
		}
	
		for (int x = 0; x < w; x++) {
			if (!vLineClear(x)) {
				downSampleLeft = x;
				break;
			}
		}

		
		for (int x = w - 1; x >= 0; x--) {
			if (!vLineClear(x)) {
				downSampleRight = x;
				break;
			}
		}
	}

	protected boolean downSampleQuadrant(int x, int y) {
		int w = entryImage.getWidth(this);
		int startX = (int) (downSampleLeft + (x * ratioX));
		int startY = (int) (downSampleTop + (y * ratioY));
		int endX = (int) (startX + ratioX);
		int endY = (int) (startY + ratioY);

		for (int yy = startY; yy <= endY; yy++) {
			for (int xx = startX; xx <= endX; xx++) {
				int loc = xx + (yy * w);

				if (pixelMap[loc] != -1)
					return true;
			}
		}

		return false;
	}


	public void downSample() {
		int w = entryImage.getWidth(this);
		int h = entryImage.getHeight(this);

		PixelGrabber grabber = new PixelGrabber(entryImage, 0, 0, w, h, true);
		try {

			grabber.grabPixels();
			pixelMap = (int[]) grabber.getPixels();
			findBounds(w, h);

			SampleData data = sample.getData();

			ratioX = (double) (downSampleRight - downSampleLeft)
					/ (double) data.getWidth();
			ratioY = (double) (downSampleBottom - downSampleTop)
					/ (double) data.getHeight();

			for (int y = 0; y < data.getHeight(); y++) {
				for (int x = 0; x < data.getWidth(); x++) {
					if (downSampleQuadrant(x, y))
						data.setData(x, y, true);
					else
						data.setData(x, y, false);
				}
			}

			sample.repaint();
			repaint();
		} catch (InterruptedException e) {
		}
	}

	public void clear() {

		this.downSampleBottom = this.downSampleTop = this.downSampleLeft = this.downSampleRight = 0;
		repaint();
	}
}