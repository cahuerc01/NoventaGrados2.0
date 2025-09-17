# NoventaGrados

**NoventaGrados** es un juego de mesa abstracto de origen alemán, diseñado para dos jugadores. Se juega en un tablero de 7x7 casillas y tiene como objetivo principal empujar las piezas del oponente fuera del tablero o lograr que la reina propia llegue al centro del tablero.

## 📜 Reglas del Juego

- Cada jugador comienza con **siete piezas** (seis peones y una reina).
- Las piezas blancas inician en la esquina superior izquierda, mientras que las negras comienzan en la esquina inferior derecha.
- En cada turno, un jugador puede mover una pieza en línea recta (horizontal o vertical).
- La distancia del movimiento está determinada por el número total de piezas (de cualquier color) en su línea de movimiento, incluyendo la pieza que se mueve.
- Si una pieza colisiona con otra durante su movimiento, la empuja en la misma dirección.
- Si una pieza es empujada fuera del tablero, se elimina del juego.
- El juego termina cuando:
  - Un jugador logra expulsar la reina del oponente del tablero.
  - Un jugador logra colocar su reina en el centro del tablero.
  - Si ambas reinas son expulsadas en la misma jugada, la partida termina en empate.

## 🎮 Modos de Juego

- **Modo Texto (`textui`)**: Interfaz basada en consola para jugar mediante comandos.
- **Modo Gráfico (`gui`)**: Interfaz visual donde los jugadores pueden interactuar con el tablero con el ratón.


## 🔧 Instalación y Ejecución

### 1️⃣ Clonar el repositorio

```sh
git clone https://github.com/tuusuario/NoventaGrados.git
cd NoventaGrados
```


### 2️⃣ Compilar el código
```sh
sh compilar.sh
```
### 3️⃣ Ejecutar el modo texto
```sh
sh ejecutar_textui.sh
```
### 4️⃣ Ejecutar el modo gráfico
```sh
sh ejecutar_gui.sh
```

## 📖 Documentación
Para generar la documentación del código en HTML:
```sh
sh documentar.sh
```
Luego, puedes abrir doc/index.html en tu navegador para ver la documentación.
