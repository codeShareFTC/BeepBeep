package com.example.beepbeepwin;

import static javax.swing.SwingConstants.HORIZONTAL;
import com.acmerobotics.roadrunner.Vector2d;
import com.example.wgwsimcore.BeepBeepCore;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BeepBeepWin extends JFrame {

    final int SEEK_BAR_SCALE = 10;

    BufferedImage robotlayer;
    Graphics2D robotGraphic;
    JTextArea textView;
    JTextArea mouseInfo;
    JSlider slider;
    JMenu menu;
    JButton playPause;
    JSpinner speedSelect;
    boolean startNewRun = true;
    boolean showMouseCursor = true;
    boolean showMouseCompass = true;
    int speed = 1;
    javax.swing.Timer timer;


    // initializing using constructor
    BeepBeepWin() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SpringLayout sprLayout = new SpringLayout();
        setLayout(sprLayout);

        // the graphics area of the field, contained this way to make scalable maintaining aspect ratio
        FieldPanel fieldPanel = new FieldPanel();
        JPanel jP = new JPanel();
        GridBagLayout grid = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        jP.setLayout(grid);
        jP.add(fieldPanel,gbc);
        add(jP);

        // create the text box to contain the messages from the robots
        textView = new JTextArea("Robot text will show here");
        textView.setEditable(false);
        textView.setSize(400,400);
        textView.setPreferredSize(textView.getSize());
        add(textView);


        mouseInfo = new JTextArea("Field Coordinates");
        mouseInfo.setEditable(false);
        add(mouseInfo);

        slider = new JSlider(HORIZONTAL,30*SEEK_BAR_SCALE);
        add(slider);
        slider.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                double time = ((double) slider.getValue() / slider.getMaximum()) * wgwSimCore.getActionDuration();
                wgwSimCore.showAtTimeMs(time);
                wgwSimCore.pauseAction();
                playPause.setText("Play");
            }
        });


        playPause = new JButton("Pause");
        add(playPause);
        playPause.addActionListener(actionEvent -> {
            if (wgwSimCore.isPaused()) {
                wgwSimCore.unpause();
                playPause.setText("Pause");
            } else {
                wgwSimCore.pauseAction();
                playPause.setText("Play");
            }
        });

        JButton rePLay = new JButton("rerun");
        add(rePLay);
        rePLay.addActionListener(actionEvent -> startNewRun = true);

        String[] positionOptions = {"Left", "Center", "Right"};
        JComboBox positionSel = new JComboBox(positionOptions);
        add(positionSel);
        positionSel.addActionListener(actionEvent -> {
            int max = menu.getItemCount();
            for(int i = 0; i<max;i++) {
                JMenu subMenu = (JMenu) menu.getItem(i);
                for (int j = 0; j < subMenu.getItemCount(); j++) {
                    if (!subMenu.getItem(0).isSelected()) {
                        subMenu.getItem(positionSel.getSelectedIndex() + 1).setSelected(true);
                    }
                }
                startNewRun = true;
            }
        });

        SpinnerModel spinnerModel = new SpinnerNumberModel(speed, //initial value
                1, //min
                25, //max
                1);//step
        speedSelect = new JSpinner(spinnerModel);
        speedSelect.addChangeListener(e -> {
            int newSpeed = (int) spinnerModel.getValue();
            wgwSimCore.setSpeed(newSpeed);
            speed = newSpeed;
            timer.setDelay((int)wgwSimCore.getTimerTickms() / speed);  //reset timer when speed changes
        });
        add(speedSelect);
        wgwSimCore.setSpeed(speed);  // set it to initial value


        // setup the spring layout constraints to position all the controls in the window
        sprLayout.putConstraint(SpringLayout.WEST, jP,   5, SpringLayout.WEST, getContentPane());
        sprLayout.putConstraint(SpringLayout.NORTH, jP,  5, SpringLayout.NORTH, getContentPane());

        // constraints for textView placement
        sprLayout.putConstraint(SpringLayout.WEST,  textView,  5, SpringLayout.EAST, jP);
        sprLayout.putConstraint(SpringLayout.NORTH, textView, 5, SpringLayout.NORTH, jP);

        // constraints for slider bar placement
        sprLayout.putConstraint(SpringLayout.NORTH, slider, 5, SpringLayout.SOUTH, jP);
        sprLayout.putConstraint(SpringLayout.WEST, slider,  0, SpringLayout.WEST, jP);
        sprLayout.putConstraint(SpringLayout.EAST, slider, 0, SpringLayout.EAST, jP);

        // constraints for PLay/Pause button
        sprLayout.putConstraint(SpringLayout.NORTH, playPause, 10, SpringLayout.SOUTH, textView);
        sprLayout.putConstraint(SpringLayout.WEST, playPause, 5,  SpringLayout.EAST, jP);

        // constraints for PLay/Pause button
        sprLayout.putConstraint(SpringLayout.NORTH, rePLay, 0, SpringLayout.NORTH, playPause);
        sprLayout.putConstraint(SpringLayout.WEST, rePLay, 5,  SpringLayout.EAST, playPause);

        // constraints for positionSel
        sprLayout.putConstraint(SpringLayout.NORTH, positionSel, 0, SpringLayout.NORTH, rePLay);
        sprLayout.putConstraint(SpringLayout.WEST, positionSel, 5,  SpringLayout.EAST, rePLay);

        // constraints for speedSelector
        sprLayout.putConstraint(SpringLayout.NORTH, speedSelect, 0, SpringLayout.NORTH, positionSel);
        sprLayout.putConstraint(SpringLayout.WEST, speedSelect, 5,  SpringLayout.EAST, positionSel);

        // constraints for mouseInfo
        sprLayout.putConstraint(SpringLayout.NORTH, mouseInfo, 5, SpringLayout.SOUTH, playPause);
        sprLayout.putConstraint(SpringLayout.WEST, mouseInfo, 5,  SpringLayout.WEST, playPause);


        // bottom and right edge of window.
        sprLayout.putConstraint(SpringLayout.EAST, getContentPane(), 5, SpringLayout.EAST, textView);
        sprLayout.putConstraint(SpringLayout.SOUTH, getContentPane(),5, SpringLayout.SOUTH, slider);


        fieldPanel.addLayer(FieldPanel.LayerNames.BACKGROUND, wgwSimCore.getBackGroundImage());
        setIconImage(wgwSimCore.getBackGroundImage());

        robotlayer = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        robotGraphic = (Graphics2D) robotlayer.getGraphics();
        fieldPanel.addLayer(FieldPanel.LayerNames.ROBOTS, robotlayer);
        BufferedImage mouseLayer = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        fieldPanel.addLayer(FieldPanel.LayerNames.MOUSE, mouseLayer);
        fieldPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseEvent.BUTTON2) {
                    showMouseCursor = !showMouseCursor;
                }
                if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
                    showMouseCompass = !showMouseCompass;
                }
            }
            @Override
            public void mouseReleased(MouseEvent mouseEvent) { }
            @Override
            public void mouseEntered(MouseEvent mouseEvent) { }
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                // clear our transparent overlay to draw robots on.
                Graphics2D g2 = mouseLayer.createGraphics();
                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(0, 0, mouseLayer.getWidth(), mouseLayer.getHeight());
                g2.dispose();
            }
        });

        fieldPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) { }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                // clear our transparent overlay to draw robots on.
                Graphics2D g2 = mouseLayer.createGraphics();
                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(0, 0, mouseLayer.getWidth(), mouseLayer.getHeight());
                g2.dispose();
                if (showMouseCursor) {
                    float x = mouseEvent.getX();
                    float y = mouseEvent.getY();
                    double fieldInches = wgwSimCore.getFieldDimensionInches();
                    double relativeY = -x * fieldInches / fieldPanel.getWidth() + fieldInches / 2.0;
                    double relativeX = -y * fieldInches / fieldPanel.getHeight() + fieldInches / 2.0;
                    String formatedStr = String.format(Locale.US, "Mouse at x: %.2f   y: %.2f\n", relativeX, relativeY);
                    mouseInfo.setText(formatedStr);
                    Graphics2D g = (Graphics2D) mouseLayer.getGraphics();
                    int layerHeight = mouseLayer.getHeight();
                    int layerWidth = mouseLayer.getWidth();
                    int xi = (int) ((x * layerWidth) / fieldPanel.getWidth());
                    int yi = (int) ((y * layerHeight) / fieldPanel.getHeight());
                    g.setColor(Color.MAGENTA);
                    g.drawLine(xi, 0, xi, layerHeight); // draw cross hairs
                    g.drawLine(0, yi, layerWidth, yi);
                    if (showMouseCompass) {
                        g.setFont(new Font("Courier New", Font.PLAIN, 16));
                        g.setColor(Color.YELLOW);
                        FontMetrics fm = g.getFontMetrics();
                        int fontHeight = fm.getHeight();
                        int hideSize = fontHeight * 4;
                        if (yi > (hideSize))
                            g.drawString(" +X 0°", xi, fontHeight * 2);
                        if (yi < layerHeight - hideSize)
                            g.drawString(" -X 180°", xi, layerHeight - fontHeight * 2);
                        if (xi > hideSize)
                            g.drawString(" +Y 90°", 0, yi - 2);
                        String str = "-Y 270°";
                        int strWidth = fm.stringWidth(str);
                        if (xi < layerWidth - strWidth)
                            g.drawString(str, layerWidth - strWidth, yi - 2);
                    }
                }
            }
        });

        // frame size
        setSize(1024,650);
        setPreferredSize(getSize());

        // setting the title of Frame
        setTitle("BeepBeepWin");

        // add a menu
        JMenu submenu;
        JMenuBar mb=new JMenuBar();
        menu = new JMenu("Robot Paths");
        mb.add(Box.createHorizontalGlue()); // make the robot menu on the right side
        mb.add(menu);
        setJMenuBar(mb);

        // add the robots and their paths to the menu
        for (int robotNum = 0; robotNum< wgwSimCore.getNumRobots(); robotNum++) {   // all robots, clear telemetry
            String robotname = wgwSimCore.getRobotName(robotNum);
            submenu = new JMenu(robotname);
            ButtonGroup buttonGroup = new ButtonGroup();
            JRadioButtonMenuItem diabledItem = new JRadioButtonMenuItem("Disabled", !wgwSimCore.getRobotEnabled(robotNum));
            submenu.add(diabledItem);
            buttonGroup.add(diabledItem);

            List<String> names = wgwSimCore.getActionNames(robotNum);
            boolean firstPath = true;
            for (String name : names) {   // add all the names of the paths.
                JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(name, (wgwSimCore.getRobotEnabled(robotNum) && firstPath));
                firstPath = false;
                submenu.add(menuItem);
                buttonGroup.add(menuItem);
                menuItem.addActionListener(this::actionPathChange);
            }
            menu.add(submenu);
        }

        // now make frame visible
        pack();
        setVisible(true);

        // run the simulation!
        wgwSimCore.setSpeed(0);
        timerTick(); // kick of the simulation
        long startTime = System.currentTimeMillis();
        long timeout = 2000; // timeout in milliseconds
        while(( !wgwSimCore.isSimulationComplete()) && (System.currentTimeMillis() - startTime < timeout) ) {
            wgwSimCore.timerTick();
        }
        wgwSimCore.setSpeed(speed);
        if (wgwSimCore.isSimulationComplete()) {
            wgwSimCore.showAtTimeMs(wgwSimCore.findStartTime() * 1000);
            timerTick();
            wgwSimCore.unpause();
        } else {  // not able to pre-run simulaiton due to some long delays possibly waitAction used
            startNewRun = true; // start the simulation ver based on timer ticks.
            JOptionPane.showMessageDialog(this, "Simulation taking too long.\nReplace SleepAction with SimTimedAction.\nSimulation will run in Real Time now.",
                    "ERROR", JOptionPane.ERROR_MESSAGE);
        }

        // Create a timer tic to run the sim core
        timer = new javax.swing.Timer((int)wgwSimCore.getTimerTickms() / speed, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timerTick();
            }
        });
        timer.start();
        timer.setDelay((int)wgwSimCore.getTimerTickms() / speed);
    }

    private void timerTick() {

        // clear our transparent overlay to draw robots on.
        Graphics2D g2 = robotlayer.createGraphics();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, robotlayer.getWidth(), robotlayer.getHeight());
        g2.dispose();

        if (startNewRun) {
            startNewRun = false;
            wgwSimCore.clearRobotActions();
            int max = menu.getItemCount();
            for(int i = 0; i<max;i++) {
                JMenu subMenu = (JMenu) menu.getItem(i);
                String robotName = subMenu.getText();
                for (int j = 0; j < subMenu.getItemCount(); j++) {
                    if (subMenu.getItem(j).isSelected()) {
                        String path = subMenu.getItem(j).getText();
                        wgwSimCore.setCurrentAction(robotName, path);
                    }
                }
            }
            wgwSimCore.startAction();
        }

        wgwSimCore.timerTick();
        getContentPane().repaint(); // don't forget to repaint the container
        double time = wgwSimCore.getCurrentTimeMs();
        slider.setValue((int) ( (double) slider.getMaximum() * time / wgwSimCore.getActionDuration() ));
    }

    // convert FTC field locaiton in inches to screen pixel locations
    Vector2d inch2Pixel (Vector2d inch) {
        int h = robotlayer.getHeight();
        int w = robotlayer.getWidth();
        double scale = Math.min(h, w) /  wgwSimCore.getFieldDimensionInches();
        int y = (int) (w / 2 - inch.x * scale);
        int x = (int) (h / 2 - inch.y * scale);
        return new Vector2d(x,y);
    }

    // convert FTC field locaiton in inches to screen pixels (not off set for center of screen)
    Vector2d scaleInch2Pixel (Vector2d inch) {
        int h = robotlayer.getHeight();
        int w = robotlayer.getWidth();
        double scale = Math.min(h, w) /  wgwSimCore.getFieldDimensionInches();
        int y = (int) (inch.x * scale);
        int x = (int) (inch.y * scale);
        return new Vector2d(x,y);
    }

    BeepBeepCore wgwSimCore = new BeepBeepCore() {
        @Override
        public void drawLineInches(Vector2d start, Vector2d end) {
            robotGraphic.drawLine((int)inch2Pixel(start).x, (int)inch2Pixel(start).y,
                    (int)inch2Pixel(end).x, (int)inch2Pixel(end).y);
        }

        @Override
        public void drawCircleInches(double xInches, double yInches, double rInches) {
            Vector2d start  = new Vector2d(xInches+rInches, yInches+rInches );
            Vector2d end    = new Vector2d(rInches*2, rInches*2);
            robotGraphic.drawOval((int)inch2Pixel(start).x, (int)inch2Pixel(start).y,
                    (int)scaleInch2Pixel(end).x, (int)scaleInch2Pixel(end).y);
        }

        @Override
        public void setColor(String colorString) {
            Color color = Color.decode(colorString);
            robotGraphic.setColor(color);
//            robotGraphic.setStroke(new BasicStroke(3));  // set line width for robot drawing
        }

        @Override
        public void setStrokeWidth(int width) {
            robotGraphic.setStroke(new BasicStroke(width * 2));  // set line width for robot drawing
        }

        @Override
        public void telemetryTextAddLine(String str) {
             textView.append(str);
        }

        @Override
        public void telemetryTextClear() {
            textView.setText("");  // got some new text so clear the display
        }
    };

    // user changes a path in the menu selection
    public void actionPathChange (ActionEvent e) {
        startNewRun = true;
    }


    // main method
    public static void main(String args[]) {
        // creating instance of Frame class
        //BeepBeepWin f = new BeepBeepWin();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BeepBeepWin f = new BeepBeepWin();
            }
        });
    }

}