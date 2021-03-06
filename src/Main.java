import java.util.Random;

public class Main {

    public static void main(String[] args) {
        Random r = new Random();
        BinaryTree<Integer, Integer> tree = new BinaryTree<>();

        int[] keys = new int[20];
        for (int i=0; i<keys.length; ++i)
            keys[i] = r.nextInt(100);

        for (int i=0; i<20; ++i) {
            tree.add(keys[i], i);
        }



        tree.makePic("tree");
        for (int i = 0; i <10; i++) {
            tree.remove(keys[i]);
            System.out.println("Removed a node from the tree!");
            tree.isBalanced();
        }
        tree.makePic("treeAfterRemove");
        var iter = tree.iterator();
        while (iter.hasNext()) {
            System.out.println(iter.key() + ": " + iter.value());
            iter.next();
        }
    }
}