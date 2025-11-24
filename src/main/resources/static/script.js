let selectedCell = null;
let isWhiteTurn = true; // Assuming white starts, will sync with backend if possible

document.addEventListener('DOMContentLoaded', () => {
    fetchBoard();
});

async function fetchBoard() {
    try {
        const response = await fetch('/game/board');
        const text = await response.text();
        renderBoard(text);
    } catch (error) {
        console.error('Error fetching board:', error);
        document.getElementById('message').textContent = 'Error de conexión';
    }
}

function renderBoard(boardString) {
    const boardDiv = document.getElementById('board');
    boardDiv.innerHTML = '';
    
    // Parse the board string from the backend
    // Format example:
    // 0 RB PB PB PB -- -- -- 
    // 1 PB -- -- -- -- -- -- 
    // ...
    
    const lines = boardString.trim().split('\n');
    // Filter out the coordinate lines if present (lines starting with space or numbers)
    // But based on previous output: "0 RB..." so we need to parse carefully.
    
    // Let's assume standard 7x7 grid.
    // We need to extract just the cell contents.
    
    let rowCount = 0;
    
    lines.forEach(line => {
        const parts = line.trim().split(/\s+/);
        // Skip coordinate lines (last line usually has column numbers)
        if (parts[0] === '0' || parseInt(parts[0]) > 0) {
             // This is a board row. parts[0] is row number.
             // parts[1] to parts[7] are the cells.
             for (let col = 1; col <= 7; col++) {
                 const cellContent = parts[col];
                 createCell(rowCount, col - 1, cellContent);
             }
             rowCount++;
        }
    });
}

function createCell(row, col, content) {
    const cell = document.createElement('div');
    cell.className = 'cell';
    cell.dataset.row = row;
    cell.dataset.col = col;
    cell.onclick = () => handleCellClick(row, col, content);

    if (content !== '--') {
        const piece = document.createElement('div');
        piece.className = 'piece';
        
        // Determine color
        if (content.includes('B')) {
            piece.classList.add('white');
        } else if (content.includes('N')) {
            piece.classList.add('black');
        }
        
        // Determine type
        if (content.includes('R')) {
            piece.classList.add('queen');
        } else if (content.includes('P')) {
            piece.classList.add('pawn');
        }
        
        cell.appendChild(piece);
    }

    document.getElementById('board').appendChild(cell);
}

async function handleCellClick(row, col, content) {
    const messageEl = document.getElementById('message');
    
    if (!selectedCell) {
        // Selecting a piece
        if (content === '--') {
            return; // Cannot select empty cell
        }
        
        selectedCell = { row, col };
        highlightCell(row, col);
        messageEl.textContent = `Seleccionado: ${row},${col}. Elige destino.`;
        
    } else {
        // Moving to destination
        const origin = `${selectedCell.row}${selectedCell.col}`;
        const dest = `${row}${col}`;
        const moveStr = `${origin}-${dest}`;
        
        // Optimistic UI update or wait for server? Let's wait.
        messageEl.textContent = `Moviendo ${moveStr}...`;
        
        try {
            const response = await fetch(`/game/move?jugada=${moveStr}`);
            const result = await response.text();
            
            messageEl.textContent = result;
            
            if (result.includes("Move applied")) {
                // Refresh board
                await fetchBoard();
                // Toggle turn visual (simple toggle, ideally backend tells us)
                isWhiteTurn = !isWhiteTurn;
                document.getElementById('status').textContent = isWhiteTurn ? "Turno: Blancas" : "Turno: Negras";
            }
            
        } catch (error) {
            messageEl.textContent = "Error al mover";
        }
        
        // Reset selection
        clearHighlight();
        selectedCell = null;
    }
}

function highlightCell(row, col) {
    const cells = document.querySelectorAll('.cell');
    cells.forEach(cell => {
        if (parseInt(cell.dataset.row) === row && parseInt(cell.dataset.col) === col) {
            cell.classList.add('selected');
        }
    });
}

function clearHighlight() {
    document.querySelectorAll('.cell').forEach(c => c.classList.remove('selected'));
}
