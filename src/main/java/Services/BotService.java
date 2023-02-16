package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }

    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    /* Set up variabel yang mungkin dibutuhkan */
    static boolean continueFood = false;
    static boolean haveTeleporter = false;
    static boolean myTeleporter = false;
    static int staTick = 0;
    static UUID myTeleporterId = null;

    public void computeNextPlayerAction(PlayerAction playerAction) {
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        if (!gameState.getGameObjects().isEmpty()) {

            var foodNotNearEdgeList = this.gameState.getGameObjects()
                .stream().filter(item -> (item.getGameObjectType() == ObjectTypes.FOOD || 
                item.getGameObjectType() == ObjectTypes.SUPERFOOD) && 
                (double)this.gameState.getWorld().radius.intValue() - 
                getDistanceBetween(item, this.gameState.getWorld()) >= 1.3 * 
                (double)this.bot.size.intValue()).sorted(Comparator.comparing(
                item -> this.getDistanceBetween(this.bot, (GameObject)item)))
                .collect(Collectors.toList());
  
            var playerList = gameState.getPlayerGameObjects()
                    .stream().filter(item -> item.getId() != this.bot.getId())
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
                    System.out.println("enemies: " + playerList.size());

            var gasCloudList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.GASCLOUD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            var asteroidFieldList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.ASTEROIDFIELD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            var teleporterList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());


            var torpedoSalvoList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDOSALVO)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            /* Untuk print tick saat ini */
            var ticker = gameState.getWorld().getCurrentTick();
            System.out.println("Tick: " + ticker);

            /* Mendapatkan nilai radius dari map saat ini */
            var bordeRadius = gameState.world.getRadius();

            /* Mendapatkan posisi tengah dari map */
            Position mid = new Position(0,0);

            /* Membuat game object yang berposisi di tengah map */
            GameObject dummy = new GameObject(null, null, null, null, mid, null);

            /* Mendapatkan jarak bot ke tengah */
            var botToMid = getDistanceBetween(this.bot, dummy);

            /* Mendapatkan jarak player terdekat */
            var nearestPlayer = getDistanceBetween(this.bot, playerList.get(0));

            /* Melakukan pengecekan dan mendapatkan jarak dan heading pada gas cloud */
            double gasDist;
            int headGas;
            if (gasCloudList.size() > 0){
                gasDist = getDistanceBetween(this.bot, gasCloudList.get(0));
                headGas = getHeadingBetween(gasCloudList.get(0));
            } else {
                gasDist = 99999;
                headGas = -1;
            }

            /* Melakukan pengecekan dan mendapatkan jarak dan heading pada asteroid field */
            double asteDist;
            int headAster;
            if (asteroidFieldList.size() > 0){
                asteDist = getDistanceBetween(this.bot, asteroidFieldList.get(0));
                headAster = getHeadingBetween(asteroidFieldList.get(0));
            } else {
                asteDist = 99999;
                headAster = -1;
            }

            /* Melakukan printing info ke layar */
            var headPlayer = getHeadingBetween(playerList.get(0));
            System.out.println("rd: " + gameState.getWorld().getRadius() + " Gs: " + gasDist + " As: " + asteDist + " Pl: " + nearestPlayer + " Md: " + botToMid + " sz: " + this.bot.size); 
            var headMid = getHeadingBetween(dummy);

            /* Melakukan pengecekan dan mendapatkan jarak dan heading pada makanan */
            double foodFarEdgeDist = 99999;
            int foodFarEdgeHead = -1;
            if (foodNotNearEdgeList.size() > 0){
                foodFarEdgeDist = getDistanceBetween(this.bot, foodNotNearEdgeList.get(0));
                foodFarEdgeHead = getHeadingBetween(foodNotNearEdgeList.get(0));
            } else {
                foodFarEdgeDist = 99999;
            }
            var teleportHeading=-1;
            /* Mendapatkan ukuran dari bot */
            var botsize = this.bot.size;
            var test=0;
            /* Mendapatkan ukuran dari musuh terdekat */
            var enemySize = playerList.get(0).size;

            /* Membuat boolean untuk memastikan bot hanya melakukan satu aksi */
            boolean done = false;

            /* Membuat boolean untuk pengecekan apakah bot harus teleport */
            boolean shouldTeleport = false;

            /* Melakukan pengecekan apakah ada teleporter di map */
            if (teleporterList.size() == 0){
                haveTeleporter = false;
            }
            
            /* Membuat kondisi penembakan teleported */
            if (botsize > 150 && !haveTeleporter){
                System.out.println("Bot sent teleporter!");
                playerAction.heading = getHeadingBetween(playerList.get(0));
                playerAction.action = PlayerActions.FIRETELEPORT;
                teleportHeading = getHeadingBetween(playerList.get(0));
                haveTeleporter = true;
                done = true;
            } else if (haveTeleporter && !done){
                int j;
                int k = 0;
                shouldTeleport = true;
                /* Mendapatkan kondisi teleporter, jika berbahaya maka bot tidak akan teleport */
                for (j = 0; (j < teleporterList.size()) && (!done); j++){
                    while (k < playerList.size()){
                        if ((getDistanceBetween(teleporterList.get(j), playerList.get(k)) < 100 + 1.2 * playerList.get(k).size) && (playerList.get(k).size > botsize) && teleporterList.get(j).currentHeading == teleportHeading){
                            System.out.println("Bot is NOT going for TELEPORT!");
                            shouldTeleport = false;
                            break;
                        } else {
                            k++;
                        }
                    }
                    
                }

                if (bordeRadius < botToMid + 1.3 * botsize){
                    /* Bot menghindar dari border dan menuju ke tengah */
                    System.out.println("Bot is running from border!");
                    playerAction.heading = headMid;
                    playerAction.action = PlayerActions.FORWARD;
                    done = true;
                }

                /* Mendapatkan kondisi teleporter apabila bot aman untuk teleport */
                for (j = 0; (j < teleporterList.size()) && (!done) && (shouldTeleport); j++){
                    k = 0;
                    while (k < playerList.size() && !done){
                        if (shouldTeleport && (getDistanceBetween(teleporterList.get(j), playerList.get(k)) < 100 + 1.2 * playerList.get(k).size) && (playerList.get(k).size < botsize)&& teleporterList.get(j).currentHeading == teleportHeading){
                            System.out.println("Bot is going for TELEPORT!");
                            playerAction.heading = getHeadingBetween(playerList.get(k));
                            playerAction.action = PlayerActions.TELEPORT;
                            haveTeleporter = false;
                            done = true;
                        } else {
                            k++;
                        }
                    }
                }    

            } else {
                // do nothing
            }

            /* Melakukan pengecekan apakah bot harus menghindari torpedo */
            if ((!done) && (torpedoSalvoList.size() > 0)){
                var salvoDist = getDistanceBetween(this.bot, torpedoSalvoList.get(0));
                var headSalvo = getHeadingBetween(torpedoSalvoList.get(0));
                System.out.println("Salvo: " + salvoDist + " Head: " + headSalvo); 
                if ((salvoDist < 100 + 1.2 * botsize) && (botsize > 100)){
                    /* Jika bot berukuran besar dan ada peluru mendekat maka bot akan mengaktifkan shield */
                    System.out.println("Bot is activating shield!");
                    playerAction.heading = foodFarEdgeHead;
                    playerAction.action = PlayerActions.ACTIVATESHIELD;
                    done = true;
                } else if (salvoDist < 100 + 1.2 * botsize){
                    /* Jika bot berukuran kecil dan ada peluru mendekat maka bot akan melarikan diri dari peluru */
                    System.out.println("Bot is running from salvo!");
                    playerAction.heading = (headSalvo + 120)%360;
                    playerAction.action = PlayerActions.FORWARD;
                    done = true;
                } else {
                    // do nothing
                }
            }

            /* Jika kondisi teleporter atau defending belum memberikan aksi maka akan masuk ke kondisional di bawah */
            if (!done){
                if (botsize > 150){
                    /* Jika ukuran bot sudah terlalu besar maka bot akan menembak musuh terdekat */
                    System.out.println("Bot is attacking because too fat!");
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                    playerAction.heading = headPlayer;
                } else if ((nearestPlayer < 150 + 4 * enemySize) && (enemySize < botsize) && (enemySize > 0.2 * botsize) && (botsize > 200)){
                    /* Bot akan menembak dengan kondisi tertentu */
                    System.out.println("Bot is attacking!");
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                    playerAction.heading = headPlayer;
                } else if ((nearestPlayer < 150 + 4 * enemySize) && (botsize > 0.4 * enemySize) && (botsize > 40)){
                    /* Bot berani melawan musuh yang sedikit lebih besar dari dirinya */
                    System.out.println("Bot is not scared at all!");
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                    playerAction.heading = headPlayer;
                } else if (nearestPlayer < 150 + 2 * enemySize){
                    /* Bot melarikan diri dari musuh yang terlalu besar */
                    System.out.println("Bot is running for his life!");
                    playerAction.action = PlayerActions.FORWARD;
                    playerAction.heading = (headPlayer + 120) % 360;
                } else if (bordeRadius < botToMid + 1.3 * botsize){
                    /* Bot menghindar dari border dan menuju ke tengah */
                    System.out.println("Bot is running from border!");
                    playerAction.heading = headMid;
                    playerAction.action = PlayerActions.FORWARD;
                } else if (gasDist < 100 + 1.2 * botsize){
                    /* Bot berbelok menghindari gas cloud */
                    System.out.println("Bot is running from gass");
                    playerAction.heading = (headGas + 120) % 360;
                    playerAction.action = PlayerActions.FORWARD;
                } else if (asteDist < 100 + 1.2 * botsize){
                    /* Bot berbelok menghindari asteroid */
                    System.out.println("Bot is running from asteroid");
                    playerAction.heading = (headAster + 120) % 360;
                    playerAction.action = PlayerActions.FORWARD;
                } else if ((nearestPlayer > enemySize + 250) && (foodNotNearEdgeList.size() > 0) && (foodFarEdgeDist < 400 + 5 * botsize)){
                    /* Mencari posisi makanan terdekat yang ada di dalam border */
                    int i;
                    for (i = 0; i < foodNotNearEdgeList.size(); i++){
                        if (getDistanceBetween(this.bot, foodNotNearEdgeList.get(i)) > 1.3 * botsize + gameState.getWorld().getRadius()){
                            continue;
                        } else {
                            break;
                        }
                    }
                    if (i == foodNotNearEdgeList.size()){
                        /* Jika makanan terdekat ada di luar border maka bot akan menuju ke tengah */
                        System.out.println("Bot is running from food!");
                        playerAction.heading = headMid;
                        playerAction.action = PlayerActions.FORWARD;
                    } else {
                        /* Jika makanan terdekat masih ada di dalam border maka bot akan memakannya */
                        System.out.println("Bot is eating food!");
                        playerAction.heading = getHeadingBetween(foodNotNearEdgeList.get(i));
                        playerAction.action = PlayerActions.FORWARD;
                    }
                } else if (foodFarEdgeDist > 500){
                    /* Jika posisi makanan terdekat terlalu jauh maka bot akan menuju ke tengah */
                    System.out.println("Bot is going to mid!");
                    playerAction.action = PlayerActions.FORWARD;
                    playerAction.heading = headMid;
                } else {
                    /* Jika tidak ada kondisi di atas maka bot akan memakan makanan terdekat */
                    System.out.println("Bot is now eating");
                    playerAction.action = PlayerActions.FORWARD;
                    playerAction.heading = foodFarEdgeHead;
                }
            }
            
            this.playerAction = playerAction;
        }
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private double getDistanceBetween(GameObject object1, World object2) {
        var radSqr = Math.pow(object1.getPosition().x, 2) + Math.pow(object1.getPosition().y, 2);
        var rad = Math.sqrt(radSqr);
        return object2.getRadius() - rad;
    }

    private int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }


}
