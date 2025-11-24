package noventagrados.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {

    @GetMapping("/game/board")
    public String board() {
        try {
            return GameService.renderBoard();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/game/move")
    public String move(@RequestParam(value = "jugada", required = false) String jugada) {
        try {
            if (jugada == null)
                return "No move provided";
            return GameService.processMove(jugada);
        } catch (Throwable e) {
            return "Error: " + e.getClass().getName() + " - " + e.getMessage();
        }
    }

}
