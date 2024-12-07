package Hilos;

import GUI.CasetaGUI;
import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Carril {
    private JPanel panel;
    private Caseta[] casetas;

    private int ancho;
    private int altura;

    private Point[] casetasIda;
    private Point[] casetasVenida;

    private int xIdaCentro = 320;
    private int xVenidaCentro = 520;

    private int yDecisionIda = 600;
    private int yDecisionVenida = 150;

    private final List<Coche> colaIda = Collections.synchronizedList(new LinkedList<>());
    private final List<Coche> colaVenida = Collections.synchronizedList(new LinkedList<>());

    private boolean[] casetaOcupada = new boolean[5];

    private List<Coche> cochesEnCarril;
    private int tiempoPago = 400;

    private boolean reincorporando = false;

    private int[] contadoresCasetas = new int[5];  
    private JPanel panelContadores;  
    private JLabel[] etiquetasContadores;  
    private JPanel panelTitulo;  
    private JLabel tituloContadores;  
    private CasetaGUI casetaGUI;

    public Carril(int x, int y, int width, int height, CasetaGUI casetaGUI) {
        this.ancho = width;
        this.altura = height;
        this.casetaGUI = casetaGUI;

        panel = new JPanel();
        panel.setLayout(null);
        panel.setBounds(x, y, width, height);
        panel.setBackground(Color.LIGHT_GRAY);

        int lineY = 375;

        casetasIda = new Point[3];
        casetasIda[0] = new Point(210, lineY);
        casetasIda[1] = new Point(305, lineY);
        casetasIda[2] = new Point(400, lineY);

        casetasVenida = new Point[2];
        casetasVenida[0] = new Point(495, lineY);
        casetasVenida[1] = new Point(590, lineY);

        casetas = new Caseta[5];

        panelContadores = new JPanel();
        panelContadores.setLayout(new BoxLayout(panelContadores, BoxLayout.Y_AXIS));
        etiquetasContadores = new JLabel[5];

        for (int i = 0; i < 3; i++) {
            casetas[i] = new Caseta(casetasIda[i]);
        }

        for (int i = 0; i < 2; i++) {
            casetas[3 + i] = new Caseta(casetasVenida[i]);
        }

        cochesEnCarril = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            casetaOcupada[i] = false;
        }

        for (int i = 0; i < 5; i++) {
            etiquetasContadores[i] = new JLabel("C.A Caseta " + (i + 1) + " = " + contadoresCasetas[i]);
            etiquetasContadores[i].setFont(new Font("Arial", Font.PLAIN, 18));
            panelContadores.add(etiquetasContadores[i]);
        }

        panelTitulo = new JPanel();
        panelTitulo.setBackground(Color.CYAN);
        tituloContadores = new JLabel("COCHES ATENDIDOS");
        tituloContadores.setFont(new Font("Arial", Font.BOLD, 18));
        panelTitulo.add(tituloContadores);

        panelContadores.setBounds(10, 50, 200, 120);
        panelContadores.setOpaque(true);
        panelTitulo.setBounds(10, 10, 200, 40);

        panel.add(panelTitulo);
        panel.add(panelContadores);

        colocarCasetasVisualmente();
    }

    public void atenderCocheEnCaseta(int casetaIndex) {
        // Incrementar el contador para la caseta indicada
        contadoresCasetas[casetaIndex]++;

        // Cambiar la imagen de la caseta al pasar el coche
        if (casetaIndex < casetas.length) {
            // Cambiar la imagen de la caseta a la versión con la barra de bloqueo arriba
            casetas[casetaIndex].cambiarImagenCaseta("imagenes/CASETAABIERTA.png");

            // Simular el avance del coche
            // Aquí debes agregar el código para que el coche avance, puedes usar un temporizador
            // o simplemente esperar un tiempo simulado
            new Thread(() -> {
                try {
                    // Simula el tiempo de espera mientras el coche avanza
                    Thread.sleep(3000); // Espera 2 segundos (o el tiempo que desees)

                    // Una vez que el coche haya avanzado, restaurar la imagen de la caseta
                    casetas[casetaIndex].cambiarImagenCaseta("imagenes/CASETACERRADA.png");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // Actualizar la etiqueta visualmente
        etiquetasContadores[casetaIndex].setText("C.A Caseta " + (casetaIndex + 1) + " = " + contadoresCasetas[casetaIndex]);
    }

    public JPanel getPanelContadores() {
        return panelContadores;
    }

    public void colocarCasetasVisualmente() {
        for (int i = 0; i < casetas.length; i++) {
            panel.add(casetas[i].getLabel());
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    public int getAltura() {
        return altura;
    }

    public int getTiempoPago() {
        return tiempoPago;
    }

    public synchronized void agregarCocheAlCarril(Coche coche) {
        cochesEnCarril.add(coche);
        if (coche.isDireccionIda()) {
            colaIda.add(coche);
        } else {
            colaVenida.add(coche);
        }
    }

    public synchronized void removerCocheDelCarril(Coche coche) {
        cochesEnCarril.remove(coche);
        if (coche.isDireccionIda()) {
            colaIda.remove(coche);
        } else {
            colaVenida.remove(coche);
        }
    }

    public int getXIdaCentro() {
        return xIdaCentro;
    }

    public int getXVenidaCentro() {
        return xVenidaCentro;
    }

    public Point[] getCasetasIda() {
        return casetasIda;
    }

    public Point[] getCasetasVenida() {
        return casetasVenida;
    }

    public int getYDecisionIda() {
        return yDecisionIda;
    }

    public int getYDecisionVenida() {
        return yDecisionVenida;
    }

    public synchronized boolean puedeAvanzarAlDecisionPoint(Coche coche) {
        if (coche.isDireccionIda()) {
            return !colaIda.isEmpty() && colaIda.get(0) == coche;
        } else {
            return !colaVenida.isEmpty() && colaVenida.get(0) == coche;
        }
    }

    public synchronized boolean casetaDisponiblePara(Coche coche, int casetaIndex) {
        if (casetaIndex < 0 || casetaIndex >= casetaOcupada.length) {
            return false;
        }
        return !casetaOcupada[casetaIndex];
    }

    public synchronized void ocuparCaseta(int casetaIndex) {
        casetaOcupada[casetaIndex] = true;
    }

    public synchronized void liberarCaseta(int casetaIndex) {
        casetaOcupada[casetaIndex] = false;
    }

    public synchronized boolean hayCocheDelanteEnCarrilPrincipal(Coche coche, int distanciaMinima) {
        List<Coche> cola = coche.isDireccionIda() ? colaIda : colaVenida;
        int yCoche = coche.getLabel().getY();
        int xCoche = coche.getLabel().getX();
        for (Coche c : cola) {
            if (c != coche) {
                int dy = Math.abs(c.getLabel().getY() - yCoche);
                int dx = Math.abs(c.getLabel().getX() - xCoche);
                if (dx < 40 && dy < distanciaMinima) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized List<Coche> getCochesEnCarrilPrincipal() {
        List<Coche> cochesEnPrincipal = new LinkedList<>();
        cochesEnPrincipal.addAll(colaIda);
        cochesEnPrincipal.addAll(colaVenida);
        return cochesEnPrincipal;
    }

    public synchronized void actualizarColaPrincipal(Coche coche) {
        if (coche.isInMainLane()) {
            if (coche.isDireccionIda()) colaIda.remove(coche);
            else colaVenida.remove(coche);
            coche.setInMainLane(false);
        }
    }

    public synchronized List<Coche> getColaIda() {
        return colaIda;
    }

    public synchronized List<Coche> getColaVenida() {
        return colaVenida;
    }

    public synchronized boolean solicitarReincorporacion() {
        if (!reincorporando) {
            reincorporando = true;
            return true;
        }
        return false;
    }

    public synchronized void finalizarReincorporacion() {
        reincorporando = false;
    }

    public Caseta getCaseta(int index) {
        if (index >= 0 && index < casetas.length) {
            return casetas[index];
        }
        return null;
    }
}