package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Hilos.Carril;
import Hilos.Coche;

public class CasetaGUI extends JFrame {

    private final Random random = new Random();
    private ScheduledExecutorService executorIda;
    private ScheduledExecutorService executorVenida;

    private int maxCochesIda;
    private int maxCochesVenida;
    private int cochesGeneradosIda = 0;
    private int cochesGeneradosVenida = 0;

    private Carril carril;

    public CasetaGUI() {
        super("Interfaz de Casetas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 750);
        JPanel panelPrincipal = new JPanel() {
            private Image imagen = new ImageIcon("imagenes/fondo.jpg").getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (imagen != null) {
                    g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        panelPrincipal.setLayout(null);
        panelPrincipal.setPreferredSize(new Dimension(800, 750));

        JScrollPane scrollPane = new JScrollPane(panelPrincipal);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane);

        int carrilWidth = 800;
        int carrilHeight = 750;

        carril = new Carril(0,0,carrilWidth,carrilHeight, this); // Inicializamos el carril una sola vez
        panelPrincipal.add(carril.getPanel());

        pack();
        setVisible(true);

        pedirCantidadCoches();
        iniciarGeneracionContinua();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (executorIda != null && !executorIda.isShutdown()) {
                    executorIda.shutdownNow();
                }
                if (executorVenida != null && !executorVenida.isShutdown()) {
                    executorVenida.shutdownNow();
                }
                System.exit(0);
            }
        });
    }

    private void pedirCantidadCoches() {
        String inputIda = JOptionPane.showInputDialog(this,
                "Ingrese la cantidad de coches (IDA, casetas 1,2,3):",
                "Cantidad de coches ida", JOptionPane.QUESTION_MESSAGE);
        if (inputIda == null || inputIda.isEmpty()) {
            maxCochesIda = 0;
        } else {
            try {
                maxCochesIda = Integer.parseInt(inputIda);
            } catch (NumberFormatException e) {
                maxCochesIda = 0;
            }
        }

        String inputVenida = JOptionPane.showInputDialog(this,
                "Ingrese la cantidad de coches para las casetas de venida (4,5):",
                "Cantidad de coches venida", JOptionPane.QUESTION_MESSAGE);
        if (inputVenida == null || inputVenida.isEmpty()) {
            maxCochesVenida = 0;
        } else {
            try {
                maxCochesVenida = Integer.parseInt(inputVenida);
            } catch (NumberFormatException e) {
                maxCochesVenida = 0;
            }
        }
    }

    public void iniciarGeneracionContinua() {
        if (executorIda == null || executorIda.isShutdown()) {
            executorIda = Executors.newSingleThreadScheduledExecutor();
        }

        if (executorVenida == null || executorVenida.isShutdown()) {
            executorVenida = Executors.newSingleThreadScheduledExecutor();
        }

        Runnable generarCocheIda = () -> {
            try {
                if (cochesGeneradosIda < maxCochesIda) {
                    Coche coche = new Coche(carril, true); // Dirección Ida (Arriba)
                    Thread hiloCoche = new Thread(coche);
                    hiloCoche.start();
                    cochesGeneradosIda++;
                } else {
                    if (!executorIda.isShutdown()) {
                        executorIda.shutdown();
                        verificarYReiniciar();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        Runnable generarCocheVenida = () -> {
            try {
                if (cochesGeneradosVenida < maxCochesVenida) {
                    Coche coche = new Coche(carril, false); // Dirección Venida (Abajo)
                    Thread hiloCoche = new Thread(coche);
                    hiloCoche.start();
                    cochesGeneradosVenida++;
                } else {
                    if (!executorVenida.isShutdown()) {
                        executorVenida.shutdown();
                        verificarYReiniciar();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        executorIda.scheduleAtFixedRate(generarCocheIda, 0, obtenerIntervaloAleatorio(), TimeUnit.MILLISECONDS);
        executorVenida.scheduleAtFixedRate(generarCocheVenida, 0, obtenerIntervaloAleatorio(), TimeUnit.MILLISECONDS);
    }

    private int obtenerIntervaloAleatorio() {
        int intervaloMinimo = 1000;
        int intervaloMaximo = 3000;
        return intervaloMinimo + random.nextInt(intervaloMaximo - intervaloMinimo);
    }

    public void verificarYReiniciar() {
        if (executorIda.isShutdown() && executorVenida.isShutdown()) {
            if (cochesGeneradosIda == maxCochesIda && cochesGeneradosVenida == maxCochesVenida) {
                int respuesta = JOptionPane.showConfirmDialog(this,
                        "¿Desea agregar más coches?", "Continuar Generación de Coches",
                        JOptionPane.YES_NO_OPTION);
                if (respuesta == JOptionPane.YES_OPTION) {
                    cochesGeneradosIda = 0;
                    cochesGeneradosVenida = 0;

                    pedirCantidadCoches();
                    iniciarGeneracionContinua();
                }
            }
        }
    }
}
