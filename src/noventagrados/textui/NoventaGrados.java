package noventagrados.textui;

import java.util.Date;
import java.util.Scanner;

import noventagrados.control.Arbitro;
import noventagrados.modelo.Celda;
import noventagrados.modelo.Jugada;
import noventagrados.modelo.Tablero;
import noventagrados.textui.excepcion.OpcionNoDisponibleException;
import noventagrados.util.Coordenada;
import noventagrados.control.undo.MecanismoDeDeshacer;
import noventagrados.control.undo.MaquinaDelTiempoConArbitros;
import noventagrados.control.undo.MaquinaDelTiempoConJugadas;

/**
 * Noventa grados en modo texto.
 * 
 * Se abusa del uso del modificador static tanto en atributos como en métodos
 * para comprobar su similitud a variables globales y funciones globales de
 * otros lenguajes.
 * 
 * La programación en este código sigue más el paradigma de programación
 * estructurada en mayor medida que la orientación a objetos.
 * 
 * En algunos casos los métodos estáticos son meros envoltorios o "wrappers" de
 * invocaciones a métodos del árbitro.
 *
 * @author <a href="rmartico@ubu.es">Raúl Marticorena</a>
 * @since 1.0
 * @version 1.0.1
 * @see noventagrados.modelo
 * @see noventagrados.control
 * @see noventagrados.util
 */
public class NoventaGrados {

    /** Tamaño en caracteres de una jugada. */
    private static final int TAMAÑO_JUGADA = 5;

    /** Posición en el texto de una jugada de la coordenada destino. */
    private static final int INICIO_COORDENADA_DESTINO = 3;

    /** Texto para interrumpir la partida. */
    private static final String TEXTO_SALIR = "salir";

    /** Texto para deshacer la última jugada. */
    private static final String TEXTO_DESHACER = "deshacer";

    /** Árbitro. */
    private static Arbitro arbitro;

    /** Lector por teclado. */
    private static Scanner scanner;

    /**
     * Mecanismo para deshacer partidas.
     */
    private static MecanismoDeDeshacer deshacer;

    /**
     * Tipo de mecanismo de deshacer a instanciar.
     */
    private static String configuracion;

    /** Oculta el constructor por defecto. */
    private NoventaGrados() {
    }

    /**
     * Método raíz con el algoritmo principal en modo texto.
     * 
     * @param args argumentos de entrada en línea de comandos
     */
    public static void main(String[] args) {
        try {
            // Extraer el modo de deshacer a partir de los argumentos.
            extraerModoDeshacer(args);

            // Inicializar la partida.
            inicializarPartida();

            // Mostrar mensaje de bienvenida.
            mostrarMensajeBienvenida();

            // Ciclo principal del juego.
            while (true) {
                mostrarTablero();
                String textoJugada = recogerTextoDeJugadaPorTeclado();

                if (comprobarSalir(textoJugada)) {
                    break; // Finalizar la partida.
                }

                if (comprobarDeshacer(textoJugada)) {
                    if (deshacer.consultarNumeroJugadasEnHistorico() > 0) {
                        deshacerJugada();
                    } else {
                        System.out.println("No hay jugadas para deshacer.");
                    }
                    continue;
                }

                if (!validarFormato(textoJugada)) {
                    mostrarErrorEnFormatoDeEntrada();
                    continue;
                }

                Jugada jugada = extraerJugada(textoJugada);

                if (!esLegal(jugada)) {
                    mostrarErrorPorMovimientoIlegal(textoJugada);
                    continue;
                }

                // Realizar la jugada.
                realizarEmpujón(jugada);

                // Comprobar si la partida ha terminado.
                if (comprobarFinalizacionPartida()) {
                    mostrarTablero();
                    mostrarGanador();
                    break;
                }

                // Cambiar el turno.
                cambiarTurnoPartida();
            }
        } catch (OpcionNoDisponibleException ex) {
            mostrarErrorSeleccionandoModo();
        } catch (RuntimeException ex) {
            mostrarErrorInterno(ex);
        } finally {
            finalizarPartida();
        }
    }

    /**
     * Extrae de los argumentos de ejecución el tipo de mecanismo de deshacer con el
     * que jugamos. Comprueba la corrección del texto introducido.
     * 
     * @param args argumentos
     * @throws OpcionNoDisponibleException si el argumento con el modo de deshacer no
     *                                     es correcto
     */
    private static void extraerModoDeshacer(String[] args) throws OpcionNoDisponibleException {
        if (args.length > 0) {
            String modo = args[0].toLowerCase();
            if ("jugadas".equals(modo) || "arbitros".equals(modo)) {
                configuracion = modo;
            } else {
                throw new OpcionNoDisponibleException("Modo no válido: " + args[0]);
            }
        } else {
            configuracion = "arbitros"; // Valor por defecto
        }
    }

    /**
     * Selecciona el mecanismo de deshacer.
     * 
     * Por polimorfismo se conecta una instancia concreta del descendiente a la
     * interfaz para deshacer.
     *
     * @param configuracion texto con la selección actual de mecanismo de deshacer
     * @throws IllegalArgumentException mecanismo de deshacer no disponible
     */
    private static void seleccionarMecanismoDeshacer(String configuracion) throws IllegalArgumentException {
        switch (configuracion) {
        case "jugadas":
            deshacer = new MaquinaDelTiempoConJugadas(new Date());
            break;
        case "arbitros":
            deshacer = new MaquinaDelTiempoConArbitros(new Date());
            break;
        default:
            throw new IllegalArgumentException("Modo no definido:" + configuracion);
        }
    }

    /**
     * Inicializa el estado de los elementos de la partida.
     */
    private static void inicializarPartida() {
        // Inicializaciones
        seleccionarMecanismoDeshacer(configuracion);
        arbitro = new Arbitro(new Tablero());
        // Cargar piezas con la configuración inicial...
        arbitro.colocarPiezasConfiguracionInicial();
        // Registrar el estado inicial en el mecanismo de deshacer
        deshacer.hacerJugada(null);
        // Abrir la lectura desde teclado...
        scanner = new Scanner(System.in);
    }

    /**
     * Recoge el texto de la jugada por teclado.
     * 
     * @return jugada en formato texto
     */
    private static String recogerTextoDeJugadaPorTeclado() {
        System.out.print("Introduce jugada turno con piezas de color " + arbitro.consultarTurno() + ": ");
        return scanner.next();
    }

    /**
     * Comprueba si se quiere finalizar la partida por parte de los usuarios.
     * 
     * @param jugada jugada en formato texto
     * @return true si el usuario introduce salir, false en caso contrario
     */
    private static boolean comprobarSalir(String jugada) {
        return jugada.equalsIgnoreCase(TEXTO_SALIR);
    }

    /**
     * Comprueba si se quiere deshacer la última jugada.
     * 
     * @param texto texto
     * @return true si el usuario introduce deshacer, false en caso contrario
     */
    private static boolean comprobarDeshacer(String texto) {
        return texto.equalsIgnoreCase(TEXTO_DESHACER);
    }

    /**
     * Valida la corrección del formato de la jugada. Solo comprueba la corrección
     * del formato de entrada en cuanto al tablero, no la validez de la jugada en
     * cuanto a las reglas del juego.
     * 
     * La jugada tiene que tener cuatro dígitos separados por un guion.
     * 
     * @param textoJugada a validar
     * @return true si el formato de la jugada es correcta según las coordenadas
     *         disponibles del tablero
     */
    private static boolean validarFormato(String textoJugada) {
        // Si la longitud es correcta y a la mitad hay un guion...
        if (textoJugada.length() == TAMAÑO_JUGADA && textoJugada.charAt(TAMAÑO_JUGADA / 2) == '-') {
            // Validar dígitos en el resto de valores...
            String origen = textoJugada.substring(0, INICIO_COORDENADA_DESTINO);
            String destino = textoJugada.substring(INICIO_COORDENADA_DESTINO + 0, TAMAÑO_JUGADA);
            // Comprobar si ambos textos son correctos
            return esTextoCorrectoParaCoordenada(origen) && esTextoCorrectoParaCoordenada(destino);
        }
        return false;
    }

    /**
     * Comprueba que el texto de la coordenada son dos dígitos en el rango [0, 6].
     * 
     * @param textoCoordenada texto de la coordenada con dos caracteres
     * @return true si son dos dígitos entre [0, 6], false en caso contrario
     */
    private static boolean esTextoCorrectoParaCoordenada(String textoCoordenada) {
        char primerCaracter = textoCoordenada.charAt(0);
        char segundoCaracter = textoCoordenada.charAt(1);
        return (primerCaracter >= '0' && primerCaracter <= '6' && segundoCaracter >= '0' && segundoCaracter <= '6');
    }

    /**
     * Extrae la jugada a partir del texto introducido por teclado.
     * 
     * @param jugadaTexto texto con la jugada
     * @return jugada
     */
    private static Jugada extraerJugada(String jugadaTexto) {
        Coordenada coordenadaOrigen = extraerCoordenada(jugadaTexto, 0, INICIO_COORDENADA_DESTINO);
        Coordenada coordenadaDestino = extraerCoordenada(jugadaTexto, INICIO_COORDENADA_DESTINO + 0, TAMAÑO_JUGADA);
        Celda origen = arbitro.consultarTablero().consultarCelda(coordenadaOrigen);
        Celda destino = arbitro.consultarTablero().consultarCelda(coordenadaDestino);
        return new Jugada(origen, destino);
    }

    /**
     * Extrae una coordenada a partir del texto de entrada y de las posiciones
     * [inicio, fin) indicadas.
     * 
     * @param jugada texto en formato notación habitual (e.g. "2013")
     * @param inicio posición inicial en el texto
     * @param fin    posición final en el texto
     * @return coordenada o null si no es posible extraerla
     */
    private static Coordenada extraerCoordenada(String jugada, int inicio, int fin) {
        if (jugada.length() != TAMAÑO_JUGADA)
            return null;
        String textoExtraido = jugada.substring(inicio, fin);
        int fila = Integer.parseInt(textoExtraido.substring(0, 1));
        int columna = Integer.parseInt(textoExtraido.substring(1, 2));
        return new Coordenada(fila, columna);
    }

    /**
     * Comprueba la legalidad de la jugada.
     * 
     * @param jugada jugada
     * @return true si es legal, false en caso contrario
     */
    private static boolean esLegal(Jugada jugada) {
        return arbitro.esMovimientoLegal(jugada);
    }

    /**
     * Realiza la jugada completando el movimiento y las capturas correspondientes.
     * 
     * @param jugada jugada
     */
    private static void realizarEmpujón(Jugada jugada) {
        deshacer.hacerJugada(jugada);
        arbitro.empujar(jugada);
    }

    /**
     * Cambia el turno de la partida.
     */
    private static void cambiarTurnoPartida() {
        arbitro.cambiarTurno();
    }

    /**
     * Comprueba si está finalizada la partida.
     * 
     * @return true si hay victoria de atacante o defensor, false en caso contrario
     */
    private static boolean comprobarFinalizacionPartida() {
        return arbitro.estaFinalizadaPartida();
    }

    /**
     * Finaliza la partida cerrando recursos abiertos como el teclado.
     */
    private static void finalizarPartida() {
        System.out.println("Partida finalizada.");
        if (scanner != null) {
            scanner.close();
        }
    }

    /**
     * Muestra el mensaje de bienvenida con instrucciones para finalizar la partida.
     */
    private static void mostrarMensajeBienvenida() {
        System.out.println("Bienvenido al juego de Noventa Grados 2.0 - Máquina del tiempo con " + configuracion);
        System.out.println(
                "Introduzca sus jugadas con el formato dd-dd donde d es un dígito en el rango [0, 6] (por ejemplo 00-04 o 65-63).");
        System.out.println("Para interrumpir la partida introduzca \"salir\".");
        System.out.println("Para deshacer la última jugada introduzca \"deshacer\".");
        System.out.println("Disfrute de la partida...");
    }

    /**
     * Muestra la información de error en el formato de entrada, mostrando ejemplos.
     */
    private static void mostrarErrorEnFormatoDeEntrada() {
        System.out.println();
        System.out.println("Error en el formato de entrada.");
        System.out.println(
                "Se requieren cuatro dígitos en parejas separados por un guion, por ejemplo 04-06 o 62-63, o bien introducir la cadena \"salir\" para finalizar la partida.");
        System.out.println("Los números estarán siempre en el rango [0,6].");
    }

    /**
     * Informa de la ilegalidad de la jugada intentada.
     * 
     * @param textoJugada texto de la jugada introducido por teclado
     */
    private static void mostrarErrorPorMovimientoIlegal(String textoJugada) {
        System.out.printf("%nLa jugada %s es ilegal.%nRevise las reglas del juego.%n", textoJugada);
    }

    /**
     * Muestra el estado del tablero con sus piezas actuales en pantalla.
     */
    private static void mostrarTablero() {
        System.out.println();
        System.out.println(arbitro.consultarTablero().aTexto());
    }

    /**
     * Muestra el turno ganador de la partida en pantalla.
     */
    private static void mostrarGanador() {
        if (arbitro.estaFinalizadaPartida() && arbitro.consultarTurnoGanador() != null) {
            System.out.printf("%nHa ganado la partida el turno con piezas de color %s.%n",
                    arbitro.consultarTurnoGanador());
        } else {
            System.out.println("\nEmpate con ambas reinas empujadas fuera del tablero.");
        }
    }

    /**
     * Deshace la última jugada realizada volviendo al estado previo.
     */
    private static void deshacerJugada() {
        deshacer.deshacerJugada();
        arbitro = deshacer.consultarArbitroActual();
        mostrarTablero();
    }

    /**
     * Muestra mensaje de error grave por error en el código del que no podemos
     * recuperarnos.
     * 
     * @param ex excepción generada
     */
    private static void mostrarErrorInterno(RuntimeException ex) {
        System.err.println("Error interno en código a corregir por el equipo informático.");
        System.err.println("Mensaje asociado de error: " + ex.getMessage());
        System.err.println("Traza detallada del error:");
        ex.printStackTrace();
    }

    /**
     * Muestra mensaje de error grave si el modo de deshacer no es ninguno de los
     * dos disponibles.
     */
    private static void mostrarErrorSeleccionandoModo() {
        System.err.println("El modo seleccionado no se corresponde con ninguna de las dos opciones válidas.");
        System.err.println("Debe introducir \"jugadas\" o \"arbitros\".");
    }
}
