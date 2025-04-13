package com.mchouse.mcrassistant.animation;

import com.mchouse.mcrassistant.App;
import javafx.animation.Animation;
import javafx.scene.Parent;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;

public class StageResizeAnimation extends Thread{
    private static int id = 1;
    private static final double DEFAULT_INC = .0005;
    private final double endWidth;
    private final double endHeight;
    private final double startWidth;
    private final double startHeight;

    public StageResizeAnimation(Stage stage, double endWidth, double endHeight){
        super(new StageResizeAnimationRunnable(stage, endWidth, endHeight));
        this.setName("Stage Animation Thread-"+id);
        getLogger().debug("Creating stage animation thread num "+id);
        id++;
        this.startWidth = stage.getWidth();
        this.startHeight = stage.getHeight();
        this.endWidth = endWidth;
        this.endHeight = endHeight;
    }

    public double getAnimationTimeInMilli(){
        double wDiference = getValueDiference(endWidth,startWidth);
        double hDiference = getValueDiference(endHeight,startHeight);
        if(wDiference>hDiference) {
            return wDiference * DEFAULT_INC;
        }else{
            return hDiference * DEFAULT_INC;
        }
    }

    public double getValueDiference(double value1, double value2){
        return value1>value2?value1-value2:value2-value1;
    }

    private Logger getLogger(){
        return LogManager.getLogger(StageResizeAnimation.class);
    }

    private static class StageResizeAnimationRunnable implements Runnable{
        private final Stage stage;
        private final double endWidth;
        private final double endHeight;
        private final double startWidth;
        private final double startHeight;

        public StageResizeAnimationRunnable(Stage stage, double endWidth, double endHeight) {
            this.stage = stage;
            this.startWidth = stage.getWidth();
            this.startHeight = stage.getHeight();
            this.endWidth = endWidth;
            this.endHeight = endHeight;
        }

        @Override
        public void run() {
            Logger logger = LogManager.getLogger(StageResizeAnimationRunnable.class);

            LocalDateTime start = LocalDateTime.now();
            logger.debug("Starting stage resize animation");

            double wInc, hInc;
            wInc = stage.getWidth()<endWidth?DEFAULT_INC:-DEFAULT_INC;
            hInc = stage.getHeight()<endHeight?DEFAULT_INC:-DEFAULT_INC;

            while(isWidthResizing() || isHeightResizing()) {
                if(isWidthResizing()){
                    App.getApp().getMainStage().setWidth(stage.getWidth() + wInc);
                    stage.setX(stage.getX() - wInc/2);
                }
                if(isHeightResizing()){
                    App.getApp().getMainStage().setHeight(stage.getHeight() + hInc);
                    stage.setY(stage.getY() - hInc/2);
                }
            }

            Duration runDuration = Duration.between(LocalDateTime.now(), start);
            logger.debug("End stage resize animation: "+runDuration.toMillis()/1000+" seconds");
            id--;
        }

        private boolean isWidthResizing(){
            if(startWidth<endWidth){
                return App.getApp().getMainStage().getWidth()<=endWidth;
            }else{
                return App.getApp().getMainStage().getWidth()>=endWidth;
            }
        }

        private boolean isHeightResizing(){
            if(startHeight<endHeight){
                return App.getApp().getMainStage().getHeight()<=endHeight;
            }else{
                return App.getApp().getMainStage().getHeight()>=endHeight;
            }
        }
    }

}
