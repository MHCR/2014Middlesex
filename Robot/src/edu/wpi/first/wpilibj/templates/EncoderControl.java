/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Encoder;

/**
 *
 * @author Computer
 */
public class EncoderControl {

    private static final int LEFT_ENCODER_1 = 13;
    private static final int LEFT_ENCODER_2 = 14;
    private static final int RIGHT_ENCODER_1 = 9;
    private static final int RIGHT_ENCODER_2 = 10;
    private static Encoder right, left;
    private static EncoderControl encoderC;
    public static double CLICKS_PER_INCH = ((72/11) * 250 ) / (Math.PI * 4);

    private EncoderControl() {
        left = new Encoder(LEFT_ENCODER_1,LEFT_ENCODER_2, true);
        right = new Encoder(RIGHT_ENCODER_1,RIGHT_ENCODER_2, false);
        left.start();
        right.start();
    }

    public static EncoderControl getInstance() {
        if(encoderC==null) {
            encoderC = new EncoderControl();
        }
        return encoderC;
    }
    //in case we want to change how we get the distance
    public double getLeftDistance() {
        return left.get();
    }
    
    public void reset(){
        left.reset();
        right.reset();
    }

    public double getRightDistance() {
        return right.get();
    }
    
    public Encoder getLeft() {
        return left;
    }

    public Encoder getRight() {
        return right;
    }
}
