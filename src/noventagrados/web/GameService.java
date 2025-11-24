package noventagrados.web;

import noventagrados.control.Arbitro;
import noventagrados.control.undo.MecanismoDeDeshacer;
import noventagrados.modelo.Celda;
import noventagrados.modelo.Jugada;
import noventagrados.util.Coordenada;

/**
 * Helper that manages game state for the web API.
 * Self-contained implementation that doesn't depend on NoventaGrados static
 * state.
 */
public class GameService {
    private static final int TAMAÑO_JUGADA = 5;
    private static final int INICIO_COORDENADA_DESTINO = 3;
    private static final String TEXTO_SALIR = "salir";
    private static final String TEXTO_DESHACER = "deshacer";

    private static MecanismoDeDeshacer deshacer;
    private static Arbitro arbitro;
    private static String configuracion = "arbitros"; // default mode
    private static boolean initialized = false;

    private static void ensureInitialized() {
        if (!initialized) {
            inicializarPartida();
            initialized = true;
        }
    }

    private static void inicializarPartida() {
        if ("jugadas".equals(configuracion)) {
            deshacer = new noventagrados.control.undo.MaquinaDelTiempoConJugadas(new java.util.Date());
        } else {
            deshacer = new noventagrados.control.undo.MaquinaDelTiempoConArbitros(new java.util.Date());
        }
        arbitro = new Arbitro(new noventagrados.modelo.Tablero());
        arbitro.colocarPiezasConfiguracionInicial();
        deshacer.hacerJugada(null);
    }

    public static String processMove(String textoJugada) {
        try {
            ensureInitialized();

            if (comprobarSalir(textoJugada)) {
                return "Game ended by user";
            }
            if (comprobarDeshacer(textoJugada)) {
                if (deshacer.consultarNumeroJugadasEnHistorico() > 0) {
                    deshacer.deshacerJugada();
                    arbitro = deshacer.consultarArbitroActual();
                    return "Last move undone";
                }
                return "No moves to undo";
            }
            if (!validarFormato(textoJugada)) {
                return "Invalid format";
            }

            Jugada jugada = extraerJugada(textoJugada);
            if (jugada == null) {
                return "Invalid move";
            }
            if (!esLegal(jugada)) {
                return "Illegal move";
            }

            deshacer.hacerJugada(jugada);
            arbitro.empujar(jugada);
            return "Move applied";
        } catch (Throwable t) {
            return "Internal Error: " + t.toString();
        }
    }

    public static String renderBoard() {
        ensureInitialized();
        return arbitro.consultarTablero().aTexto();
    }

    private static boolean comprobarSalir(String jugada) {
        return jugada.equalsIgnoreCase(TEXTO_SALIR);
    }

    private static boolean comprobarDeshacer(String texto) {
        return texto.equalsIgnoreCase(TEXTO_DESHACER);
    }

    private static boolean validarFormato(String textoJugada) {
        if (textoJugada.length() == TAMAÑO_JUGADA && textoJugada.charAt(TAMAÑO_JUGADA / 2) == '-') {
            String origen = textoJugada.substring(0, INICIO_COORDENADA_DESTINO);
            String destino = textoJugada.substring(INICIO_COORDENADA_DESTINO, TAMAÑO_JUGADA);
            return esTextoCorrectoParaCoordenada(origen) && esTextoCorrectoParaCoordenada(destino);
        }
        return false;
    }

    private static boolean esTextoCorrectoParaCoordenada(String textoCoordenada) {
        char primerCaracter = textoCoordenada.charAt(0);
        char segundoCaracter = textoCoordenada.charAt(1);
        return (primerCaracter >= '0' && primerCaracter <= '6' && segundoCaracter >= '0' && segundoCaracter <= '6');
    }

    private static Jugada extraerJugada(String jugadaTexto) {
        Coordenada coordenadaOrigen = extraerCoordenada(jugadaTexto, 0, INICIO_COORDENADA_DESTINO);
        Coordenada coordenadaDestino = extraerCoordenada(jugadaTexto, INICIO_COORDENADA_DESTINO, TAMAÑO_JUGADA);
        if (coordenadaOrigen == null || coordenadaDestino == null) {
            return null;
        }
        noventagrados.modelo.Tablero tablero = arbitro.consultarTablero();
        Celda origen = tablero.consultarCelda(coordenadaOrigen);
        Celda destino = tablero.consultarCelda(coordenadaDestino);
        return new Jugada(origen, destino);
    }

    private static Coordenada extraerCoordenada(String jugada, int inicio, int fin) {
        if (jugada.length() != TAMAÑO_JUGADA)
            return null;
        String textoExtraido = jugada.substring(inicio, fin);
        int fila = Integer.parseInt(textoExtraido.substring(0, 1));
        int columna = Integer.parseInt(textoExtraido.substring(1, 2));
        return new Coordenada(fila, columna);
    }

    private static boolean esLegal(Jugada jugada) {
        return arbitro.esMovimientoLegal(jugada);
    }
}
