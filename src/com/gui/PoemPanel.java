package com.gui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public class PoemPanel  {

    private List<JPanel> poemLabels = new ArrayList<>();

    private JComponent poem;

    public PoemPanel(JComponent poem) {
        this.poem = poem;
    }

    public void add(JPanel panel) {
        poemLabels.add(panel);
    }

    public List<JPanel> getPoemLabels() {
        return poemLabels;
    }

    public JComponent getPoem() {
        return poem;
    }
}
