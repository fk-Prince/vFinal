package SwingComponents.Renderer;


import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

public class MyComboBox extends BasicComboBoxRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        label.setFont(new Font("Arial", Font.BOLD, 14));

        if (isSelected) {
            label.setBackground(Color.LIGHT_GRAY);
        } else {
            label.setBackground(Color.WHITE);
        }
        label.setForeground(Color.BLACK);
        return label;
    }

}


