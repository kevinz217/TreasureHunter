/**
 * The Town Class is where it all happens.
 * The Town is designed to manage all the things a Hunter can do in town.
 * This code has been adapted from Ivan Turner's original program -- thank you Mr. Turner!
 */

public class Town {
    // instance variables
    private Hunter hunter;
    private Shop shop;
    private Terrain terrain;
    private String printMessage;
    private boolean toughTown;
    private boolean searched;
    private boolean dugged;

    private String townTreasure;
    private static int treasureCount = 0;

    /**
     * The Town Constructor takes in a shop and the surrounding terrain, but leaves the hunter as null until one arrives.
     *
     * @param shop The town's shoppe.
     * @param toughness The surrounding terrain.
     */
    public Town(Shop shop, double toughness, String treasure) {
        this.shop = shop;
        this.terrain = getNewTerrain();

        // the hunter gets set using the hunterArrives method, which
        // gets called from a client class
        hunter = null;

        printMessage = "";


        // higher toughness = more likely to be a tough town
        toughTown = (Math.random() < toughness);
        townTreasure = treasure;
        searched = false;
        dugged = false;
    }

    public String getLatestNews() {
        return printMessage;
    }

    /**
     * Assigns an object to the Hunter in town.
     *
     * @param hunter The arriving Hunter.
     */
    public void hunterArrives(Hunter hunter) {
        this.hunter = hunter;
        printMessage = "Welcome to town, " + hunter.getHunterName() + ".";

        if (toughTown) {
            printMessage += "\nIt's pretty rough around here, so watch yourself.";
        } else {
            printMessage += "\nWe're just a sleepy little town with mild mannered folk.";
        }
    }

    /**
     * Handles the action of the Hunter leaving the town.
     *
     * @return true if the Hunter was able to leave town.
     */
    public boolean leaveTown() {
        boolean canLeaveTown = terrain.canCrossTerrain(hunter);
        if (canLeaveTown) {
            String item = terrain.getNeededItem();
            printMessage = "You used your " + item + " to cross the " + terrain.getTerrainName() + ".";
            if (!(TreasureHunter.getEasyMode())) {
                if (checkItemBreak()) {
                hunter.removeItemFromKit(item);
                printMessage += "\nUnfortunately, you lost your  " + item;
                }
            }

            return true;
        }

        printMessage = "You can't leave town, " + hunter.getHunterName() + ". You don't have a " + terrain.getNeededItem() + ".";
        return false;
    }

    /**
     * Handles calling the enter method on shop whenever the user wants to access the shop.
     *
     * @param choice If the user wants to buy or sell items at the shop.
     */
    public void enterShop(String choice) {
        shop.enter(hunter, choice);
        printMessage = "You left the shop";
    }

    /**
     * Gives the hunter a chance to fight for some gold.<p>
     * The chances of finding a fight and winning the gold are based on the toughness of the town.<p>
     * The tougher the town, the easier it is to find a fight, and the harder it is to win one.
     */
    public void lookForTrouble() {
        double noTroubleChance;
        if (toughTown) {
            noTroubleChance = 0.66;
        } else {
            noTroubleChance = 0.33;
        }

        if (Math.random() > noTroubleChance) {
            printMessage = "You couldn't find any trouble";
        } else {
            printMessage = Colors.RED + "You want trouble, stranger!  You got it!\nOof! Umph! Ow!\n" + Colors.RESET;
            int goldDiff = (int) (Math.random() * 10) + 1;
            if (Hunter.hasItemInKit("sword") || Math.random() > noTroubleChance) {
                if (Hunter.hasItemInKit("sword")) {
                    printMessage += "Your opponent sees your sword and gets intimidated ";
                }
                printMessage += Colors.RED + "Okay, stranger! You proved yer mettle. Here, take my gold." + Colors.RESET;
                printMessage += "\nYou won the brawl and receive "+ Colors.YELLOW + goldDiff + Colors.RESET + " gold.";
                hunter.changeGold(goldDiff);
            } else {
                printMessage += Colors.RED + "That'll teach you to go lookin' fer trouble in MY town! Now pay up!" + Colors.RESET;
                printMessage += "\nYou lost the brawl and pay " + goldDiff + " gold.";
                hunter.changeGold(-goldDiff);
            }
        }
    }

    /**
     * Gives the hunter a chance to search for treasure.<p>
     * One treasure is assigned to each town.<p>
     * Dust can be found but not added, and only one of each treasure can be obtained
     */
    public void huntForTreasure() {
        String[] treasureList = hunter.getTreasureList();
        if (!searched) {
            // checks if the hunter already has the treasure
            for (String treasure : treasureList) {
                if (treasure != null) {
                    if (treasure.equals(townTreasure)) {
                        System.out.println("You have already collected " + Colors.GREEN + townTreasure + Colors.RESET);
                        searched = true;
                        return;
                    }
                }
            }

            for (int i = 0; i < treasureList.length; i++) {
                if (treasureList[i] == null) {
                    if (townTreasure.equals("dust")) {
                        System.out.println("You found dust! (nothing has been added to your inventory)");
                        searched = true;
                        return;
                    } else {
                        treasureList[i] = townTreasure;
                        System.out.println("You found " + Colors.GREEN + townTreasure + Colors.RESET + "!");
                        searched = true;
                        treasureCount++;
                        return;
                    }
                }
            }
            System.out.println("You have already searched this town!");
            searched = true;
        } else {
            System.out.println("You have already searched this town!");
        }
    }

    public void digForGold() {
        if (hunter.hasItemInKit("shovel")) {
            if (!dugged) {
                double chance = (Math.random());
                if (chance >= 0.5) {
                    int goldGain = (int) (Math.random() * 19) + 1;
                    System.out.println("You have gained " + Colors.YELLOW + goldGain + Colors.RESET + " gold from digging!");
                    hunter.changeGold(goldGain);
                    dugged = true;
                } else {
                    System.out.println("You dug but only found dirt");
                    dugged = true;
                }
            } else {
                System.out.println("You have already dug in this town for gold!");
            }
        } else {
            System.out.println("You can't dig for gold without a shovel!");
        }
    }

    public String toString() {
        return "This nice little town is surrounded by " + Colors.CYAN + terrain.getTerrainName() + Colors.RESET + ".";
    }

    /**
     * Determines the surrounding terrain for a town, and the item needed in order to cross that terrain.
     *
     * @return A Terrain object.
     */
    private Terrain getNewTerrain() {
        double rnd = Math.random();
        if (rnd < .16) {
            return new Terrain("Mountains", "Rope");
        } else if (rnd < .32) {
            return new Terrain("Ocean", "Boat");
        } else if (rnd < .48) {
            return new Terrain("Plains", "Horse");
        } else if (rnd < .64) {
            return new Terrain("Desert", "Water");
        } else if (rnd < .82){
            return new Terrain("Jungle", "Machete");
        } else {
            return new Terrain("Marsh", "Boots");
        }
    }

    /**
     * Determines whether a used item has broken.
     *
     * @return true if the item broke.
     */
    private boolean checkItemBreak() {
        double rand = Math.random();
        return (rand < 0.5);
    }

    public boolean checkTreasures() {
        if (treasureCount == 3) {
            return true;
        }
        return false;
    }
}