package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;
import java.util.Comparator;
import java.util.Optional;
import java.lang.Math;

public class BotService {
    // private GameObject bot;
    // private PlayerAction playerAction;
    // private GameState gameState;
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    private int searchRadiusModifier = 200;
    private boolean afterburnerOn = false;
    private PlayerAction lastAction;
    private int timeSinceLastAction;
    private GameObject target;
    private boolean targetIsPlayer = false;
    private GameObject worldCenter;



    public BotService() {
        playerAction = new PlayerAction();
        gameState = new GameState();
    }

    public GameObject getBot() {
        return bot;
    }

    public PlayerAction getPlayerAction() {
        return playerAction;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }


    public void computeNextPlayerAction(PlayerAction playerAction) {
        var foodList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        var foodNotNearEdgeList = this.gameState.getGameObjects()
                .stream().filter(item -> (item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPERFOOD) && (double)this.gameState.getWorld().radius.intValue() - getDistanceBetween(item, this.gameState.getWorld()) >= 1.2 * (double)this.bot.size.intValue()).sorted(Comparator.comparing(item -> this.getDistanceBetween(this.bot, (GameObject)item))).collect(Collectors.toList());

        System.out.println("foodNotNearEdgeList: " + foodNotNearEdgeList);

        var playerList = gameState.getPlayerGameObjects()
                .stream().filter(item -> item.getId() != this.bot.getId())
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
        System.out.println("playerList: " + playerList);

        var wormholeList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.WORMHOLE)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        var obstacleList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.GASCLOUD||item.getGameObjectType() == ObjectTypes.ASTEROIDFIELD)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        var asteroidFieldList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.ASTEROIDFIELD)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        var superFoodList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERFOOD)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        var supernovaPickupList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        var supernovaBombList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVABOMB)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        var teleporterList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        var shieldList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SHIELD)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        var torpedoSalvoList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDOSALVO)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
                
        Position mid = new Position(0,0);
        GameObject dummy = new GameObject(null, null, null, null, mid, null);
        var botToMid = getDistanceBetween(this.bot, dummy);
        var bordeRadius = gameState.world.getRadius();
        var botsize = this.bot.size;
        var headMid = getHeadingBetween(dummy);
        if(bordeRadius==null){
            bordeRadius = 0;
        }
        

    if (bordeRadius < botToMid + 1.2 * botsize){
            System.out.println("Bot is running from border!");
            playerAction.heading = headMid;
            playerAction.action = PlayerActions.FORWARD;
        }
    else if (playerList.size() > 0 && getDistanceBetween(bot, playerList.get(0)) < 200) {
            playerAction.action = PlayerActions.FIRETORPEDOES;
            playerAction.heading = getHeadingBetween(playerList.get(0));
            System.out.println("Firing torpedoes");
    }


    else if (playerList.size()>0 && getDistanceBetween(bot, playerList.get(0)) < 200 && this.bot.getSize()<playerList.get(0).getSize()){
            playerAction.action = PlayerActions.FORWARD;
            playerAction.heading = (getHeadingBetween(playerList.get(0))+180)%360;
            System.out.println("RUN");
}
        
    else if (playerList.size()>0&&getDistanceBetween(bot, playerList.get(0)) > 10 && this.bot.getSize()>playerList.get(0).getSize() && getDistanceBetween(bot, playerList.get(0)) < 250){
            playerAction.action = PlayerActions.FORWARD;
            playerAction.heading = getHeadingBetween(playerList.get(0));
            System.out.println("Heading to player");
        
    }

    else if (obstacleList.size()>0 && getDistanceBetween(obstacleList.get(0), bot)<100){
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = (getHeadingBetween(obstacleList.get(0))+180)%360;
        System.out.println("Heading away from gas cloud");
    }
    else if (foodNotNearEdgeList.size()>0){
        var food = foodNotNearEdgeList.get(0);
            playerAction.action = PlayerActions.FORWARD;
            playerAction.heading = getHeadingBetween(food);
            System.out.println("Heading to food");
    }

    this.playerAction = playerAction;
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
        var triangleX = Math.abs(object1.getPosition().x - object2.getCenterPoint().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getCenterPoint().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }


    private int ResolveNewTarget(){
        var foodList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        var playerList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        if (foodList.size() > 0 && playerList.size() > 0) {
            if (getDistanceBetween(bot, foodList.get(0)) < getDistanceBetween(bot, playerList.get(0))) {
                target = foodList.get(0);
                targetIsPlayer = false;
            } else {
                target = playerList.get(0);
                targetIsPlayer = true;
            }
        } else if (foodList.size() > 0) {
            target = foodList.get(0);
            targetIsPlayer = false;
        } else if (playerList.size() > 0) {
            target = playerList.get(0);
            targetIsPlayer = true;
        } else {
            target = worldCenter;
            targetIsPlayer = false;
        }

        return getHeadingBetween(target);
    }

    private int getAttackerResolution(GameObject bot, GameObject attacker, GameObject closestFood) {
        if (closestFood == null) {
        return GetOppositeDirection(bot, attacker);
        }
        
        double distanceToAttacker = getDistanceBetween(attacker,bot);
        double distanceBetweenAttackerAndFood = getDistanceBetween(attacker, closestFood);
        
        if (distanceToAttacker > attacker.getSpeed() &&
        distanceBetweenAttackerAndFood > distanceToAttacker) {
        System.out.println("Atk is far, going for food");
        return getDirection(bot, closestFood);
        } else {
        System.out.println("Running");
        return GetOppositeDirection(bot, attacker);
        }
        }

    private int GetOppositeDirection(GameObject gameObject1, GameObject gameObject2){
        var direction = toDegrees(Math.atan2(gameObject2.getPosition().y - gameObject1.getPosition().y, gameObject2.getPosition().x - gameObject1.getPosition().x));
        return (direction + 180) % 360;
        }

    private int getDirection(GameObject gameObject1, GameObject gameObject2){
        var direction = toDegrees(Math.atan2(gameObject2.getPosition().y - gameObject1.getPosition().y, gameObject2.getPosition().x - gameObject1.getPosition().x));
        return (direction + 360) % 360;
        }



}
