package com.example.beepbeepwin;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.LinkedHashMap;

// concept taking from https://stackoverflow.com/questions/35974972/multiple-independent-layers-in-graphics

public class FieldPanel extends JPanel {
    public enum LayerNames {BACKGROUND, ROBOTS, MOUSE}

    LinkedHashMap<LayerNames, Image> layers;

    public FieldPanel() {
        setBorder(BorderFactory.createLineBorder(Color.black));
        setBackground(Color.pink);
        setSize(500,500);
        setMinimumSize(getSize());
        setPreferredSize(new Dimension(500,500));
        layers = new LinkedHashMap<>();
    }

    public void addLayer(LayerNames layer, Image image){
        layers.put(layer, image);
    }


    public void paint(Graphics g) {
        super.paintComponent(g);
        for(LayerNames i : layers.keySet())  //render all layers
            g.drawImage(layers.get(i), 0, 0, getWidth(), getHeight(), null);
    }

    /**
     * Override the preferred size to return the largest it can, in
     * a square shape.  Must (must, must) be added to a GridBagLayout
     * as the only component (it uses the parent as a guide to size)
     * with no GridBagConstaint (so it is centered).
     * from: <a href="https://coderanch.com/t/629253/java/create-JPanel-maintains-aspect-ratio">...</a>
     */
    @Override
    public final Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
//        System.out.println("d=" + d.width + ", " + d.height);
        Dimension prefSize;
        Component c = getParent();
        if (c == null) {
            prefSize = new Dimension((int) d.getWidth(), (int) d.getHeight());
        } else if (c.getWidth() > d.getWidth() && c.getHeight() > d.getHeight()) {
            prefSize = c.getSize();
        } else {
            prefSize = d;
        }
        int w = (int) prefSize.getWidth();
        int h = (int) prefSize.getHeight();
        // the smaller of the two sizes
        int s = (Math.min(w, h));
        return new Dimension(s, s);
    }


}
