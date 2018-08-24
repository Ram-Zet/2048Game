
import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    private Stack<Tile[][]> previousStates = new Stack();
    private Stack<Integer> previousScores = new Stack();
    private boolean isSaveNeeded = true;
    int score = 0;
    int maxTile = 0;

    public Model() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        resetGameTiles();
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> result = new ArrayList<Tile>();
        for (int i = 0; i < FIELD_WIDTH; i++)
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value == 0) result.add(gameTiles[i][j]);
            }
        return result;
    }

    private void addTile() {
        List<Tile> list = getEmptyTiles();
        if (list != null && list.size() > 0)
            list.get((int) (Math.random() * list.size())).value = Math.random() < 0.9 ? 2 : 4;
    }

    public void resetGameTiles() {
        for (int i = 0; i < FIELD_WIDTH; i++)
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        addTile();
        addTile();
    }

    private boolean compressTiles(Tile[] tiles) {
        boolean result = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (tiles[i].value == 0) {
                int index = i;
                for (int j = i; j < FIELD_WIDTH; j++) {
                    if (tiles[j].value != 0) {
                        index = j;
                        break;
                    }
                }
                if (index != i) {
                    tiles[i].value = tiles[index].value;
                    tiles[index].value = 0;
                    result = true;
                }
            }
        }
        return result;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean result = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (tiles[i].value != 0 && (i + 1) != FIELD_WIDTH && tiles[i].value == tiles[i + 1].value) {
                tiles[i].value *= 2;
                score += tiles[i].value;
                if (tiles[i].value > maxTile) maxTile = tiles[i].value;
                tiles[i + 1].value = 0;
                result = true;
            }
        }
        compressTiles(tiles);
        return result;
    }

    void left() {
        if (isSaveNeeded) saveState(gameTiles);
        boolean flag = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])) flag = true;
        }
        if (flag) addTile();
        isSaveNeeded = true;
    }

    void down() {
        saveState(gameTiles);
        turn90();
        left();
        turn90();
        turn90();
        turn90();
    }

    void right() {
        saveState(gameTiles);
        turn90();
        turn90();
        left();
        turn90();
        turn90();
    }

    void up() {
        saveState(gameTiles);
        turn90();
        turn90();
        turn90();
        left();
        turn90();
    }

    private void turn90() {
        Tile[][] tempTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                tempTiles[j][FIELD_WIDTH - 1 - i] = gameTiles[i][j];
            }
        }
        gameTiles = tempTiles;
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public boolean canMove() {
        for (int i = 0; i < FIELD_WIDTH - 1; i++) {
            for (int j = 0; j < FIELD_WIDTH - 1; j++) {
                if (gameTiles[i][j].value == 0) return true;
                if (gameTiles[i][j + 1].value == 0) return true;
                if (gameTiles[i + 1][j].value == 0) return true;
                if (gameTiles[i][j].value == gameTiles[i][j + 1].value) return true;
                if (gameTiles[i][j].value == gameTiles[i + 1][j].value) return true;
            }
        }
        return false;
    }

    public void saveState(Tile[][] tiles) {
        Tile[][] newTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++)
            for (int j = 0; j < FIELD_WIDTH; j++) newTiles[i][j] = new Tile(tiles[i][j].value);
        previousStates.push(newTiles);
        previousScores.push(score);
        isSaveNeeded = false;

    }

    public void rollback() {
        try {
            gameTiles = (Tile[][]) previousStates.pop();
            score = (int) previousScores.pop();
        } catch (Exception e) {

        }
    }

    public void randomMove() {
        int r = (int) (Math.random() * 4);
        switch (r) {
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                down();
                break;
        }
    }

    public boolean hasBoardChanged(){
        Tile[][] tile = previousStates.peek();
        int sum1 = 0;
        int sum2 = 0;
        for (int i = 0; i< FIELD_WIDTH; i++){
            for (int j = 0; j< FIELD_WIDTH; j++){
                sum1 += gameTiles[i][j].value;
                sum2 += tile[i][j].value;
            }
        }
        return (sum1 != sum2);
    }

    public MoveEfficiency getMoveEfficiency(Move move){
        MoveEfficiency moveEfficiency;
        move.move();
        if (hasBoardChanged()) {
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        }
        else {
            moveEfficiency = new MoveEfficiency(-1, 0, move);
        }
        rollback();
        return moveEfficiency;
    }

    public void autoMove(){
        PriorityQueue<MoveEfficiency> priorityQueue = new PriorityQueue<MoveEfficiency>(4, Collections.reverseOrder());
        priorityQueue.offer(getMoveEfficiency(() -> left()));
        priorityQueue.offer(getMoveEfficiency(() -> right()));
        priorityQueue.offer(getMoveEfficiency(this::up));
        priorityQueue.offer(getMoveEfficiency(this::down));
        priorityQueue.peek().getMove().move();
    }
}

