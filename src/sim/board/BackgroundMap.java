package sim.board;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.ShortLookupTable;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import api.Tools;
import api.pojo.GeoCoordinates;
import api.pojo.UTMCoordinates;
import sim.board.pojo.MercatorProjection;

/** Developed by: Francisco José Fabra Collado, fron GRC research group in Universitat Politècnica de València (Valencia, Spain). */

public class BackgroundMap {

	public Image img;					// Downloaded image
	public UTMCoordinates originUTM;	// Upper-left corner UTM coordinates
	public double xScale, yScale;		// px/UTM scale
	public double alfa;				// (rad) Mercator-UTM turn angle
	public double centerX, centerY;		// UTM coordinates of the center of the image
	

	@SuppressWarnings("unused")
	private BackgroundMap() {
	}

	/**
	 * Retrieves a geopositioned image from Google Static Maps.
	 * <p>The object also contains the coordinates of the center of the image.
	 * Returns with attribute img==null if the image could not be downloaded.</p>
	 * @param latitude of the center of the image
	 * @param longitude of the center of the image
	 * @param zoom level
	 * @param pxWidth to be filled with the image (pixels)
	 * @param pxHeight to be filled with the image (pixels)
	 * @param UTMx of the center of the image
	 * @param UTMy of the center of the image
	 */
	public BackgroundMap(double latitude, double longitude, int zoom, int pxWidth, int pxHeight, double UTMx, double UTMy) {

		this.centerX = UTMx;
		this.centerY = UTMy;

		MercatorProjection projection = new MercatorProjection(latitude, longitude, pxWidth, pxHeight, Math.pow(2, zoom));
		GeoCoordinates upLeft = projection.getGeoLocation(0, 0);

		UTMCoordinates upLeftUTM = Tools.geoToUTM(upLeft.latitude, upLeft.longitude);
		this.originUTM = upLeftUTM;

		GeoCoordinates upRight = projection.getGeoLocation(pxWidth, 0);
		UTMCoordinates upRightUTM = Tools.geoToUTM(upRight.latitude, upRight.longitude);
		GeoCoordinates bottomRight = projection.getGeoLocation(pxWidth, pxHeight);
		UTMCoordinates bottomRightUTM = Tools.geoToUTM(bottomRight.latitude, bottomRight.longitude);

		double incHorizontal = upRightUTM.distance(upLeftUTM);
		double incVertical = upRightUTM.distance(bottomRightUTM);
		this.xScale = (incHorizontal * BoardParam.screenScale) / pxWidth;
		this.yScale = (incVertical * BoardParam.screenScale) / pxHeight;
		this.alfa = Math.acos((upRightUTM.x - upLeftUTM.x) / incHorizontal);
		try {
			URL imagen = new URL("http://maps.googleapis.com/maps/api/staticmap?center=" + latitude + "," + longitude
					+ "&zoom=" + zoom + "&size=" + pxWidth + "x" + pxHeight + "&maptype=satellite");
			BufferedImage im = ImageIO.read(imagen);
			if (im != null) {

				BufferedImage imgDest = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);

				Graphics g = imgDest.getGraphics();
				g.drawImage(im, 0, 0, null);

				LookupTable it = new ShortLookupTable(0, BoardParam.brightness);
				LookupOp lop = new LookupOp(it, null);
				lop.filter(imgDest, imgDest);

				this.img = Toolkit.getDefaultToolkit().createImage(imgDest.getSource());
			}

		} catch (IOException e) {
		}
	}
}
