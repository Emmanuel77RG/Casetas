/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

// En Hilos/Caseta.java
package Hilos;

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class Caseta {
    private int contador;  // Contador de coches atendidos
    private JLabel labelCaseta;  // Imagen de la caseta

    public Caseta(Point ubicacion) {
        this.contador = 0;  // Inicializamos el contador a 0
        this.labelCaseta = new JLabel();  // Imagen de la caseta

        // Configuramos la imagen de la caseta
        URL imageUrl = getClass().getClassLoader().getResource("imagenes/CASETACERRADA.png");
        if (imageUrl != null) {
            ImageIcon icon = new ImageIcon(new ImageIcon(imageUrl).getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            labelCaseta.setIcon(icon);
        } else {
            labelCaseta.setBackground(Color.GRAY);
        }

        // Establecer la posición de la caseta
        labelCaseta.setBounds(ubicacion.x - 40, ubicacion.y - 40, 80, 80);
        
    }

    public void cambiarImagenCaseta(String imagen) {
        // Cambiar la imagen de la caseta sin alterar su posición o tamaño
        URL imageUrl = getClass().getClassLoader().getResource(imagen);
        if (imageUrl != null) {
            ImageIcon icon = new ImageIcon(new ImageIcon(imageUrl).getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            labelCaseta.setIcon(icon);
        } else {
            labelCaseta.setBackground(Color.GRAY);  // Si no se encuentra la imagen, se coloca un color de fondo
        }
    }


    // Método para incrementar el contador
    public void incrementarContador() {
        contador++;  // Incrementar el contador
    }

    // Métodos para obtener los JLabels (caseta y contador)
    public JLabel getLabel() {
        return labelCaseta;
    }

}