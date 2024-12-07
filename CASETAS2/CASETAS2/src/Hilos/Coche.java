package Hilos;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

enum CocheEstado {
    EN_COLA_PRINCIPAL,
    EN_DECISION,
    MOVIENDO_A_CASETA,
    EN_CASETA,
    AVANZANDO_DERECHO,
    VOLVIENDO_A_CARRIL,
    ESPERANDO_REINCORPORAR,
    REINCORPORANDOSE,
    TERMINADO // Nuevo estado
}

public class Coche implements Runnable {
    private JLabel label;
    private Carril carril;
    private boolean direccionIda;
    private static ImageIcon[] imagenesCochesArriba;
    private static ImageIcon[] imagenesCochesAbajo;
    private static final String[] nombresImagenesArriba = {
        "imagenes/coche1_up.png",
        "imagenes/coche2_up.png"
    };
    private static final String[] nombresImagenesAbajo = {
        "imagenes/coche1_down.png",
        "imagenes/coche2_down.png"
    };
    private static final Random random = new Random();
    
    private int velocidad = 3;
    private int distanciaEntreCoches = 40;
    private boolean pagado = false;
    private int casetaIndex = -1;
    private boolean inMainLane = true;
    private CocheEstado estado = CocheEstado.EN_COLA_PRINCIPAL;

    private Point targetPoint;
    private Point posicionReincorporacion;
    private Point puntoAvanceDerecho;

    static {
        imagenesCochesArriba = new ImageIcon[nombresImagenesArriba.length];
        for (int i = 0; i < nombresImagenesArriba.length; i++) {
            URL imageUrl = Coche.class.getClassLoader().getResource(nombresImagenesArriba[i]);
            if (imageUrl != null) {
                imagenesCochesArriba[i] = new ImageIcon(new ImageIcon(imageUrl).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
            } else {
                imagenesCochesArriba[i] = new ImageIcon();
                System.err.println("Imagen no encontrada: " + nombresImagenesArriba[i]);
            }
        }

        imagenesCochesAbajo = new ImageIcon[nombresImagenesAbajo.length];
        for (int i = 0; i < nombresImagenesAbajo.length; i++) {
            URL imageUrl = Coche.class.getClassLoader().getResource(nombresImagenesAbajo[i]);
            if (imageUrl != null) {
                imagenesCochesAbajo[i] = new ImageIcon(new ImageIcon(imageUrl).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
            } else {
                imagenesCochesAbajo[i] = new ImageIcon();
                System.err.println("Imagen no encontrada: " + nombresImagenesAbajo[i]);
            }
        }
    }

    public Coche(Carril carril, boolean direccionIda) {
        this.carril = carril;
        this.direccionIda = direccionIda;

        int xPos = direccionIda ? carril.getXIdaCentro() : carril.getXVenidaCentro();
        int yPos = direccionIda ? carril.getAltura() - 40 : 0;

        this.label = new JLabel();
        this.label.setOpaque(false);
        this.label.setBounds(xPos, yPos, 40, 40);
        this.label.setIcon(elegirImagenAleatoria());

        carril.getPanel().add(this.label);
        carril.getPanel().setComponentZOrder(this.label, 0);

        synchronized (carril) {
            carril.agregarCocheAlCarril(this);
        }

        SwingUtilities.invokeLater(() -> {
            carril.getPanel().revalidate();
            carril.getPanel().repaint();
        });
    }

    private ImageIcon elegirImagenAleatoria() {
        if (direccionIda) {
            int index = random.nextInt(imagenesCochesArriba.length);
            return imagenesCochesArriba[index];
        } else {
            int index = random.nextInt(imagenesCochesAbajo.length);
            return imagenesCochesAbajo[index];
        }
    }

    public boolean isDireccionIda() {
        return direccionIda;
    }

    public int getCasetaIndex() {
        return casetaIndex;
    }

    public JLabel getLabel() {
        return label;
    }

    public boolean isInMainLane() {
        return inMainLane;
    }

    public void setInMainLane(boolean inMainLane) {
        this.inMainLane = inMainLane;
    }

    @Override
public void run() {
    try {
        while (estado != CocheEstado.TERMINADO) {
            switch (estado) {
                case EN_COLA_PRINCIPAL:
                    mantenerFilaEnCarrilPrincipal();
                    if (carril.puedeAvanzarAlDecisionPoint(this)) {
                        int yDecision = direccionIda ? carril.getYDecisionIda() : carril.getYDecisionVenida();
                        if ((direccionIda && getLabel().getY() <= yDecision) ||
                            (!direccionIda && getLabel().getY() >= yDecision)) {
                            estado = CocheEstado.EN_DECISION;
                        } else {
                            moverVerticalHacia(yDecision);
                        }
                    }
                    break;

                case EN_DECISION:
                    if (casetaIndex == -1) {
                        elegirCaseta();
                    }
                    if (casetaIndex != -1 && carril.casetaDisponiblePara(this, casetaIndex)) {
                        carril.ocuparCaseta(casetaIndex);
                        synchronized(carril) {
                            carril.actualizarColaPrincipal(this);
                        }
                        Point p = casetaIndex < 3 ? carril.getCasetasIda()[casetaIndex] : carril.getCasetasVenida()[casetaIndex-3];
                        int distanciaDeDetencion = -30;
                        int posicionDetencion = direccionIda ? p.y - distanciaDeDetencion : p.y + distanciaDeDetencion;
                        targetPoint = new Point(p.x, posicionDetencion);
                        estado = CocheEstado.MOVIENDO_A_CASETA;
                    }
                    break;

                case MOVIENDO_A_CASETA:
                    if (moverHacia(targetPoint)) {
                        estado = CocheEstado.EN_CASETA;
                        pagado = false;
                    }
                    break;

                case EN_CASETA:
                    try {
                        // Incrementar el contador de la caseta correspondiente
                        carril.atenderCocheEnCaseta(casetaIndex); // Incrementa el contador de la caseta

                        Thread.sleep(carril.getTiempoPago()); // Simulamos el tiempo que se pasa en la caseta
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    carril.liberarCaseta(casetaIndex);
                    pagado = true;

                    int avanceRecto = 50;
                    int xActual = label.getX();
                    int yActual = label.getY();
                    if (direccionIda) {
                        puntoAvanceDerecho = new Point(xActual, yActual - avanceRecto);
                    } else {
                        puntoAvanceDerecho = new Point(xActual, yActual + avanceRecto);
                    }
                    estado = CocheEstado.AVANZANDO_DERECHO;
                    break;


                case AVANZANDO_DERECHO:
                    if (moverHacia(puntoAvanceDerecho)) {
                        int yReincorp = direccionIda ? 200 : 600;
                        int xReincorp = direccionIda ? carril.getXIdaCentro() : carril.getXVenidaCentro();
                        posicionReincorporacion = new Point(xReincorp, yReincorp);
                        estado = CocheEstado.VOLVIENDO_A_CARRIL;
                    }
                    break;

                case VOLVIENDO_A_CARRIL:
                    if (moverHacia(posicionReincorporacion)) {
                        estado = CocheEstado.ESPERANDO_REINCORPORAR;
                    }
                    break;

                case ESPERANDO_REINCORPORAR:
                    if (carril.solicitarReincorporacion() && !carril.hayCocheDelanteEnCarrilPrincipal(this, 40)) {
                        inMainLane = true;
                        estado = CocheEstado.REINCORPORANDOSE;
                    } else {
                        Thread.sleep(100);
                    }
                    break;

                case REINCORPORANDOSE:
                    moverVerticalHacia(posicionReincorporacion.y);
                    carril.finalizarReincorporacion();
                    estado = CocheEstado.EN_COLA_PRINCIPAL;
                    break;
            }

            // Mover el coche al final del carril
            moverHastaFinal();
            
            Thread.sleep(50);
        }
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}

    private void elegirCaseta() {
        int[] opciones = direccionIda ? new int[]{0, 1, 2} : new int[]{3, 4, 5};
        for (int opcion : opciones) {
            if (carril.casetaDisponiblePara(this, opcion)) {
                casetaIndex = opcion;
                break;
            }
        }
    }
    
    private void moverVerticalHacia(int y) {
        int step = direccionIda ? -velocidad : velocidad;
        int nuevaY = label.getY() + step;
        if ((direccionIda && nuevaY < y) || (!direccionIda && nuevaY > y)) {
            nuevaY = y;
        }
        label.setLocation(label.getX(), nuevaY);
    }

    private boolean moverHacia(Point p) {
        int xActual = label.getX();
        int yActual = label.getY();
        int xDestino = p.x;
        int yDestino = p.y;
        int xStep = 0;
        int yStep = 0;

        if (xActual < xDestino) {
            xStep = Math.min(velocidad, xDestino - xActual);
        } else if (xActual > xDestino) {
            xStep = -Math.min(velocidad, xActual - xDestino);
        }

        if (yActual < yDestino) {
            yStep = Math.min(velocidad, yDestino - yActual);
        } else if (yActual > yDestino) {
            yStep = -Math.min(velocidad, yActual - yDestino);
        }

        label.setLocation(xActual + xStep, yActual + yStep);
        return label.getX() == xDestino && label.getY() == yDestino;
    }

    private void moverHastaFinal() {
        int yFin = direccionIda ? -40 : carril.getAltura() + 40;
        moverVerticalHacia(yFin);
        if ((direccionIda && label.getY() <= -40) || (!direccionIda && label.getY() >= carril.getAltura() + 40)) {
            estado = CocheEstado.TERMINADO;
        }
    }

    private void mantenerFilaEnCarrilPrincipal() {
        if (!isInMainLane()) return; // Corregido

        List<Coche> coches = carril.getCochesEnCarrilPrincipal(); // Corregido
        for (int i = coches.size() - 1; i >= 0; i--) {
            Coche coche = coches.get(i);
            if (coche == this) continue;
            JLabel cocheLabel = coche.getLabel();
            int diferenciaY = cocheLabel.getY() - this.label.getY();
            if (direccionIda) {
                if (diferenciaY < distanciaEntreCoches && diferenciaY > 0) {
                    this.label.setLocation(label.getX(), cocheLabel.getY() - distanciaEntreCoches);
                }
            } else {
                if (diferenciaY > -distanciaEntreCoches && diferenciaY < 0) {
                    this.label.setLocation(label.getX(), cocheLabel.getY() + distanciaEntreCoches);
                }
            }
        }
    }
}
