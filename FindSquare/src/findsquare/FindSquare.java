/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package findsquare;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_imgproc.IplConvKernel;
import edu.wpi.first.smartdashboard.camera.WPICameraExtension;
import edu.wpi.first.smartdashboard.gui.DashboardFrame;
import edu.wpi.first.wpijavacv.WPIBinaryImage;
import edu.wpi.first.wpijavacv.WPIColor;
import edu.wpi.first.wpijavacv.WPIColorImage;
import edu.wpi.first.wpijavacv.WPIContour;
import edu.wpi.first.wpijavacv.WPIImage;
import edu.wpi.first.wpijavacv.WPIJavaCVExtension;
import edu.wpi.first.wpijavacv.WPIPoint;
import edu.wpi.first.wpijavacv.WPIPolygon;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author kevin and chinsy
 */
public class FindSquare extends WPICameraExtension {
    private static final String HOT = "hot";
    private static final String RANGE = "Range";
    private static final String AZIMUTH = "azimuth";
    private static final String TARGETS = "Targets";
    private static final String VERTICIES = "verticies";

    private static final long serialVersionUID = 100;
    private WPIColor targetColor = new WPIColor(0, 255, 0);
    private static final double kNearlyHorizontalSlope = Math.tan(Math.toRadians(10));  // 20 degrees
    private static final double kNearlyVerticalSlope = Math.tan(Math.toRadians(90 - 10)); // 70 degrees
    private static final int kMinWidth = 5;
    private static final int kMaxWidth = 150;
    private static final int kHoleClosingIterations = 9;

    private static final double kHorizontalFOVDeg = 47.5;
    private static final double kHorizontalFOVRad = Math.toRadians(kHorizontalFOVDeg);
    private static final double kTargetWidth = 4;
    private static final double kTargetHeight = 31;

    // Store JavaCV temporaries as members to reduce memory management during processing
    private CvSize size = null;
    private WPIContour[] contours;
    private ArrayList<WPIPolygon> polygons;
    private IplConvKernel morphKernel;

    private IplImage bin;
    private IplImage hsv;
    private IplImage hue;
    private IplImage sat;
    private IplImage val;

    private WPIPoint linePt1;
    private WPIPoint linePt2;
    private WPIPoint linePt3;
    private WPIPoint linePt4;

    private int horizontalOffsetPixels;
    public static NetworkTable table;
    public boolean networkReady = false;
    
    private final boolean debug;
    private boolean fire;

    private double
            boundAngle0to360Degrees(double angle) {
        // Naive algorithm
        while (angle >= 360.0) {
            angle -= 360.0;
        }

        while (angle < 0.0) {
            angle += 360.0;
        }
        return angle;
    }

    public FindSquare() {
        this(false);
    }

    public FindSquare(boolean debug) {
        this.debug = debug;
        fire = false;
        morphKernel = IplConvKernel.create(3, 3, 1, 1, opencv_imgproc.CV_SHAPE_RECT, null);
        table = NetworkTable.getTable("SmartDashboard");
        WPIJavaCVExtension.init();
    }

    @Override
    public WPIImage processImage(WPIColorImage rawImage) {
        if (size == null || size.width() != rawImage.getWidth() || size.height() != rawImage.getHeight()) {
            size = opencv_core.cvSize(rawImage.getWidth(), rawImage.getHeight());
            // Allocate and zero images
            bin = IplImage.create(size, 8, 1);
            hsv = IplImage.create(size, 8, 3);
            hue = IplImage.create(size, 8, 1);
            sat = IplImage.create(size, 8, 1);
            val = IplImage.create(size, 8, 1);

            horizontalOffsetPixels = (int) Math.round(size.width() / kHorizontalFOVDeg);

            linePt1 = new WPIPoint(size.width() / 2 + horizontalOffsetPixels, size.height() - 1);
            linePt2 = new WPIPoint(size.width() / 2 + horizontalOffsetPixels, 0);
            linePt3 = new WPIPoint(0, size.height() / 2 + horizontalOffsetPixels);
            linePt4 = new WPIPoint(size.width() - 1, size.height() / 2 + horizontalOffsetPixels);
        }

        // Get the raw IplImages for OpenCV
        IplImage input = WPIJavaCVExtension.getIplImage(rawImage);

        // Convert to HSV color space
        opencv_imgproc.cvCvtColor(input, hsv, opencv_imgproc.CV_BGR2HSV);
        opencv_core.cvSplit(hsv, hue, sat, val, null);

        // Threshold each component separately, looking for green
        // Hue
        opencv_imgproc.cvThreshold(hue, bin, 70 - 15, 255, opencv_imgproc.CV_THRESH_BINARY);
        opencv_imgproc.cvThreshold(hue, hue, 70 + 15, 255, opencv_imgproc.CV_THRESH_BINARY_INV);

        // Saturation
        opencv_imgproc.cvThreshold(sat, sat, 200, 255, opencv_imgproc.CV_THRESH_BINARY);

        // Value
        opencv_imgproc.cvThreshold(val, val, 75, 255, opencv_imgproc.CV_THRESH_BINARY);

        // Combine the results to obtain our binary image which should for the most
        // part only contain pixels that we care about
        opencv_core.cvAnd(hue, bin, bin, null);
        opencv_core.cvAnd(bin, sat, bin, null);
        opencv_core.cvAnd(bin, val, bin, null);

        // Fill in any gaps using binary morphology
        opencv_imgproc.cvMorphologyEx(bin, bin, null, morphKernel, opencv_imgproc.CV_MOP_CLOSE, kHoleClosingIterations);

        // Find contours
        WPIBinaryImage binWpi = WPIJavaCVExtension.makeWPIBinaryImage(bin);
        contours = WPIJavaCVExtension.findConvexContours(binWpi);

        polygons = new ArrayList<>();
        for (WPIContour c : contours) {
            double ratio = ((double) c.getWidth()) / ((double) c.getHeight());
            double ratio2 = ((double) c.getHeight()) / ((double) c.getWidth());
            
            if(((double)c.getWidth() / (double)c.getHeight()) > .7 && c.getY() < (binWpi.getHeight() / 2)){
                table.putBoolean("hot", true);
                rawImage.drawContour(c, WPIColor.BLUE, 2);
               
                break;
            } else {
                table.putBoolean("hot", false);
            }
//            if ((ratio2 < .28 && ratio2 > 0.09 && c.getHeight() > kMinWidth && c.getHeight() < kMaxWidth) || (ratio < 0.28 && ratio > 0.03 && c.getWidth() > kMinWidth && c.getWidth() < kMaxWidth)) {
//              //  polygons.add(c.approxPolygon(5));
//            }
        }

//        WPIPolygon square = null;
//        int highest = Integer.MAX_VALUE;
////        if(polygons.size() == 0 || polygons.size() == 1 ){
////            table.putBoolean("hot", false);
////        }
//        for (WPIPolygon p : polygons) {
//            if(debug) {
//                System.out.println("verticies " + p.getNumVertices());
//            } else {
//                table.putNumber(VERTICIES, p.getNumVertices());
//            }
//            rawImage.drawPolygon(p, WPIColor.BLUE, 4);
//            if (p.isConvex() && p.getNumVertices() == 4) {
//                // We passed the first test...we fit a rectangle to the polygon
//                // Now do some more tests
//                WPIPoint[] points = p.getPoints();
//                // We expect to see a top line that is nearly horizontal, and two side lines that are nearly vertical
//                int numNearlyHorizontal = 0;
//                int numNearlyVertical = 0;
//                for (int i = 0; i < 4; i++) {
//                    double dy = points[i].getY() - points[(i + 1) % 4].getY();
//                    double dx = points[i].getX() - points[(i + 1) % 4].getX();
//                    double slope = Double.MAX_VALUE;
//                    if (dx != 0) {
//                        slope = Math.abs(dy / dx);
//                    }
//                    if (slope < kNearlyHorizontalSlope) {
//                        ++numNearlyHorizontal;
//                    } else if (slope > kNearlyVerticalSlope) {
//                        ++numNearlyVertical;
//                    }
//                }
//
//                if (numNearlyHorizontal >= 1 && numNearlyVertical == 2) {
//                    rawImage.drawPolygon(p, WPIColor.BLUE, 2);
//
//                    int pCenterX = (p.getX() + (p.getWidth() / 2));
//                    int pCenterY = (p.getY() + (p.getHeight() / 2));
//
//                    rawImage.drawPoint(new WPIPoint(pCenterX, pCenterY), targetColor, 5);
//                    if (pCenterY < highest) { // Because coord system is funny
//                        square = p;
//                        highest = pCenterY;
//                    }
//                }
//            } else {
//                rawImage.drawPolygon(p, WPIColor.GREEN, 1);
//
//            }
//        }
//
//        if (square != null) {
//            double ratio = (double)square.getHeight() / (double)square.getWidth();
//            table.putNumber("ratio", ratio);
//            if(ratio < .28 && ratio > 0.09 ){
//                
//                table.putBoolean(HOT, true);
//            }else{
//                table.putBoolean(HOT, false);
//            }
//            
//            double x = square.getX() + (square.getWidth() / 2);
//            x = (2 * (x / size.width())) - 1;
//            double y = square.getY() + (square.getHeight() / 2);
//            y = -((2 * (y / size.height())) - 1);
//            double squareWidth = square.getWidth();
//            double imageWidth = size.width();
//            if(debug) {
//                System.out.println("Square height: " + square.getHeight() + " Image Height: " + size.height() + " ratio: " + (double) (square.getHeight() / size.height()));
//            }
//            double azimuth = this.boundAngle0to360Degrees(x * kHorizontalFOVDeg / 2.0);
//            double range = (kTargetHeight) / Math.tan(kHorizontalFOVRad * ((double) square.getHeight() / (double) size.height()));
//            if(debug) {
//                System.out.println("Target found");
//                System.out.println("x: " + x);
//                System.out.println("y: " + y);
//                System.out.println("Height: " + square.getHeight() + " image height: " + size.height());
//                System.out.println("Width: " + square.getWidth() + " image width: " + size.width());
//                System.out.println("TargetWdith: " + kTargetWidth + " ratio: " + (squareWidth / imageWidth));
//                System.out.println("azimuth: " + azimuth);
//                System.out.println("range: " + range);
//            } else {
//                table.putNumber(RANGE, range);
//                table.putNumber(AZIMUTH, azimuth);
//            }
//            if (polygons.size() == 2 || polygons.size() == 3) {
//                WPIPolygon poly = null;
//                
//                
//            }
//        } else {
//            if(!debug) {
//                //get rid of old values
//                table.putNumber(RANGE, -1);
//                table.putNumber(AZIMUTH, -1);
//                table.putBoolean(HOT, false);
//            }
//        }
//        //either way
//        if(debug) {
//            System.out.println("Targets: "+polygons.size());
//        } else {
//            table.putNumber(TARGETS, polygons.size());
//           // table.putBoolean("hot", fire);
//        }

        // Draw a crosshair
//        rawImage.drawLine(linePt1, linePt2, targetColor, 2);
//        rawImage.drawLine(linePt3, linePt4, targetColor, 2);

        WPIJavaCVExtension.releaseMemory();

        //System.gc();
        return rawImage;
    }

    public static void main(String[] args) {

        FindSquare l;
        IplImage frame = null;

        if (args.length == 0) {
            System.out.println("Usage: Arguments are paths to image files to test the program on");
            return;
        }

        //magic when running the smart dashboard standalone
        DashboardFrame dashFrame = new DashboardFrame(false);

        //Robot.getTable().putNumber("Orange", 22);
        l = new FindSquare(true);

        long totalTime = 0;

        WPIColorImage rawImage;
        long startTime, endTime;
        double milliseconds;
        WPIImage resultImage;
        FrameGrabber grabber = new FFmpegFrameGrabber("http://10.8.69.11/mjpg/video.mjpg");
        grabber.setFormat("mjpeg");
        CanvasFrame cf = new CanvasFrame("raw");
        CanvasFrame cr = new CanvasFrame("Result");
        try {
            grabber.start();
            while (true) {
//                rawImage = new WPIColorImage(ImageIO.read(new File("C:\\Documents and Settings\\mechinn\\code\\FindSquare\\TestImages\\frcCameraImg.jpg")));
                rawImage = new WPIColorImage(grabber.grab().getBufferedImage());

                cf.showImage(rawImage.getBufferedImage());
                startTime = System.nanoTime();
                resultImage = l.processImage(rawImage);

                cr.showImage(resultImage.getBufferedImage());
                endTime = System.nanoTime();

                totalTime += (endTime - startTime);
                milliseconds = (double) (endTime - startTime) / 1000000.0;

//                System.out.format("Processing took %.2f milliseconds%n", milliseconds);
//                System.out.format("(%.2f frames per second)%n", 1000.0 / milliseconds);
            }
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(FindSquare.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(FindSquare.class.getName()).log(Level.SEVERE, null, ex);
        }
//        double milliseconds = (double) (totalTime) / 1000000.0 / (args.length);
//        System.out.format("AVERAGE:%.2f milliseconds%n", milliseconds);
//        System.out.format("(%.2f frames per second)%n", 1000.0 / milliseconds);
//        System.exit(0);
    }

}
