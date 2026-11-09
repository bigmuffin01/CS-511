import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Customer implements Runnable {
    private Bakery bakery;
    private Random rnd;
    private List<BreadType> shoppingList;
    private int shopTime;
    private int checkoutTime;
    private CountDownLatch doneSignal;
    
    /**
     * Initialize a customer object and randomize its shopping list
     */
    public Customer(Bakery bakery, CountDownLatch l) {
        // TODO
        this.bakery = bakery;
        this.doneSignal = l;
        this.shoppingList = new ArrayList<>();
        rnd = new Random();
        fillShoppingList();
        shopTime = rnd.nextInt(200)+1;
        checkoutTime = rnd.nextInt(50)+1;
    }

    /**
     * Run tasks for the customer
     */
    public void run() {
        // TODO
        System.out.println("Customer "+hashCode()+" now shopping");
        for (BreadType bread : shoppingList) { // get bread
            try {
                if (bread == BreadType.RYE) {
                    bakery.getRye().acquire();
                    Thread.sleep(shopTime);
                    bakery.takeBread(bread);
                    System.out.println("Customer "+hashCode()+" took RYE bread");
                    bakery.getRye().release();
                } else if (bread == BreadType.SOURDOUGH) {
                    bakery.getSourdough().acquire();
                    Thread.sleep(shopTime);
                    bakery.takeBread(bread);
                    System.out.println("Customer "+hashCode()+" took SOURDOUGH bread");
                    bakery.getSourdough().release();
                } else {
                    bakery.getWonder().acquire();
                    Thread.sleep(shopTime);
                    bakery.takeBread(bread);
                    System.out.println("Customer "+hashCode()+" took WONDER bread");
                    bakery.getWonder().release();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        try { // checkout
            bakery.getCashiers().acquire();
            Thread.sleep(checkoutTime);
            System.out.println("Customer "+hashCode()+" buying items");
            bakery.getMutexSales().acquire(); // mutex for adding sales
            bakery.addSales(getItemsValue());
            bakery.getMutexSales().release();
            bakery.getCashiers().release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Customer "+hashCode()+" finished");
        doneSignal.countDown();
    }

    /**
     * Return a string representation of the customer
     */
    public String toString() {
        return "Customer " + hashCode() + ": shoppingList=" + Arrays.toString(shoppingList.toArray()) + ", shopTime=" + shopTime + ", checkoutTime=" + checkoutTime;
    }

    /**
     * Add a bread item to the customer's shopping list
     */
    private boolean addItem(BreadType bread) {
        // do not allow more than 3 items, chooseItems() does not call more than 3 times
        if (shoppingList.size() >= 3) {
            return false;
        }
        shoppingList.add(bread);
        return true;
    }

    /**
     * Fill the customer's shopping list with 1 to 3 random breads
     */
    private void fillShoppingList() {
        int itemCnt = 1 + rnd.nextInt(3);
        while (itemCnt > 0) {
            addItem(BreadType.values()[rnd.nextInt(BreadType.values().length)]);
            itemCnt--;
        }
    }

    /**
     * Calculate the total value of the items in the customer's shopping list
     */
    private float getItemsValue() {
        float value = 0;
        for (BreadType bread : shoppingList) {
            value += bread.getPrice();
        }
        return value;
    }
}
