import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Random;

public class BinaryTree<K extends Comparable<K>, V> {
    private Node<K, V> root;
    private int size;
    private Random rand = new Random();
    private Comparator<K> comp;

    public BinaryTree() {
        this(Comparator.naturalOrder());
    }

    public BinaryTree(Comparator<K> c) {
        root = null;
        size = 0;
        comp = c;
    }

    // randomly find a position
    public boolean add(K key, V value) {
        if (root == null) {
            root = new Node<>(key, value);
            size = 1;
            return true;
        }
        else
            return add(key, value, root);
    }

    private boolean add(K key, V value, Node<K,V> cur) {
        int c = comp.compare(key, cur.key);
        if (c == 0) {
            return false;
        }
        else if (c < 0) {
            if (cur.left != null) {
                boolean res = add(key, value, cur.left);
                if (res)
                    cur.resetHeight();
                balance(cur);
                return res;
            }
            else {
                cur.left = new Node<>(key, value);
                cur.left.parent = cur;
                cur.resetHeight();
                size++;
                balance(cur);
                return true;
            }
        }
        else {
            if (cur.right != null) {
                boolean res = add(key, value, cur.right);
                if (res)
                    cur.resetHeight();
                balance(cur);
                return res;
            }
            else {
                cur.right = new Node<>(key, value);
                cur.right.parent = cur;
                cur.resetHeight();
                size++;
                balance(cur);
                return true;
            }
        }
    }

    public V remove(K key) {
        var v = remove(key, root);
        if (v != null)
            size--;
        return v;
    }

    private V remove(K key, Node<K,V> cur) {
        if (cur == null)
            return null;

        int c = comp.compare(key, cur.key);
        if (c < 0)
            return remove(key, cur.left);
        else if (c > 0)
            return remove(key, cur.right);
        else {
            // found our value.
            V temp = cur.value;
            // several cases to consider.
            if (cur.left == null && cur.right == null) {
                if (cur.parent == null)
                    root = null;
                else if (cur.parent.left == cur)
                    cur.parent.left = null;
                else
                    cur.parent.right = null;
            }
            else if (cur.left != null && cur.right != null) {
                // find the value in the right subtree
                var rightMin = minNode(cur.right);
                // place rightMin's data here; this node is hooked up right
                cur.key = rightMin.key;
                cur.value = rightMin.value;
                // remove rightMin. This call will do it recursively. Note we
                // ARE NOT returning the result!
                remove(rightMin.key, rightMin);
            }
            else  { // exactly one child. Find and promote it.
                var childNode = (cur.left != null) ? cur.left : cur.right;
                // attach childNode to cur's parent
                if (cur.parent == null)
                    root = childNode;
                else if (cur.parent.left == cur)
                    cur.parent.left = childNode;
                else
                    cur.parent.right = childNode;
                // childNode's left and right are fine. update its parent.
                childNode.parent = cur.parent;
            }
            return temp;
        }
    }

    private Node<K, V> minNode(Node<K,V> right) {
        while (right.left != null)
            right = right.left;
        return right;
    }
    private void balance(Node<K, V> cur) {
        int leftHeight = 0;
        int rightHeight = 0;
        if (cur.left != null)
             leftHeight = cur.left.height;
        if (cur.right != null)
            rightHeight = cur.right.height;
        if (Math.abs(leftHeight - rightHeight) < 2)
            return;

        Node<K, V> leftChild = cur.left;
        Node<K, V> rightChild = cur.right;

        if (leftHeight > rightHeight) {
            if (leftChild.right == null) {
                rotateLL(cur);
            }
            else if (leftChild.left == null) {
                rotateLR(cur);
            }
            else if (leftChild.right.height > leftChild.left.height)
                rotateLR(cur);
            else rotateLL(cur);
        }
        else {
            if (rightChild.right == null) {
                rotateRL(cur);
            }
            else if (rightChild.left == null) {
                rotateRR(cur);
            }
            else if (rightChild.right.height > rightChild.left.height)
                rotateRR(cur);
            else rotateRL(cur);
        }

    }

    private void rotateLL(Node<K, V> cur) {
        System.out.println("rotate LL");
        Node<K, V> oldRootNewLeft = null;
        if (cur.left.right != null)
            oldRootNewLeft = cur.left.right;
        Node<K, V> newRoot = cur.left;
        newRoot.right = cur;
        newRoot.parent = cur.parent;
        if (cur.parent == null) {
            root = newRoot;
        }
        if (cur.parent != null) {
            if (cur.parent.left == cur) {
                cur.parent.left = newRoot;
            }
            else
                cur.parent.left = newRoot;
        }
        cur.parent = newRoot;
        cur.left = oldRootNewLeft;
        if (oldRootNewLeft != null)
            oldRootNewLeft.parent = cur;
        newRoot.resetHeight();
        if (newRoot.parent == null) {
            root = newRoot;
        }
    }

    private void rotateLR(Node<K, V> cur) {
        System.out.println("rotate LR");
        Node<K, V> oldRootNewLeft = null;
        if (cur.left.right.right != null)
            oldRootNewLeft = cur.left.right.right;
        Node<K, V> newRootOldLeft = null;
        if (cur.left.right.left != null)
            newRootOldLeft = cur.left.right.left;
        Node<K, V> newRoot = cur.left.right;
        newRoot.left = newRoot.parent;
        newRoot.right = cur;
        if (cur.parent != null) {
            if (cur.parent.left == cur) {
                cur.parent.left = newRoot;
            }
            else
                cur.parent.left = newRoot;
        }
        cur.left = oldRootNewLeft;
        cur.parent = newRoot;
        newRoot.left.parent = newRoot;
        newRoot.left.right = newRootOldLeft;
        if (oldRootNewLeft != null)
            oldRootNewLeft.parent = cur;
        if (newRootOldLeft != null)
            newRootOldLeft.parent = newRoot.left;
        newRoot.resetHeight();
        if (newRoot.parent == null) {
            root = newRoot;
        }
    }

    public V find(K key){
        return find(key, root);
    }

    private V find(K key, Node<K, V> cur){
        if(cur == null)
            return null;
        int c = comp.compare(key, cur.key);
        if (c < 0){
            return find(key, cur.left);
        }
        else if(c == 0){
            return cur.value;
        }
        else{
            return find(key, cur.right);
        }
    }


    private void rotateRR(Node<K, V> cur) {
        System.out.println("rotate RR");
        Node<K,V> oldR = null;
        if(cur.right.left != null)
            oldR = cur.right.left;
        Node<K, V> pivot = cur.right;
        pivot.left = cur;
        pivot.parent = cur.parent;
        if (cur.parent != null) {
            if (cur.parent.left == cur) {
                cur.parent.left = pivot;
            }
            else
                cur.parent.right = pivot;
        }
        cur.parent = pivot;
        cur.right = oldR;
        if(oldR != null)
            oldR.parent = cur;
        pivot.resetHeight();
        if (pivot.parent == null) {
            root = pivot;
        }
    }

    private void rotateRL(Node<K, V> cur) {
        System.out.println("rotate RL");
        Node<K, V> oldR = null;
        if(cur.right.left.left != null)
            oldR = cur.right.left.left;
        Node<K,V> oldRL = null;
        if(cur.right.left.right != null)
            oldRL = cur.right.left.right;
        Node<K, V> pivot = cur.right.left;
        pivot.right = pivot.parent;
        pivot.left = cur;
        if (cur.parent != null) {
            if (cur.parent.left == cur) {
                cur.parent.left = pivot;
            }
            else
                cur.parent.right = pivot;
        }
        cur.right = oldR;
        cur.parent = pivot;
        pivot.right.parent = pivot;
        pivot.right.left = oldRL;
        if(oldR != null)
            oldR.parent = cur;
        if(oldRL != null)
            oldRL.parent = pivot.right;
        pivot.resetHeight();
        if (pivot.parent == null) {
            root = pivot;
        }
    }

    public Iterator iterator() {
        return new Iterator();
    }

    // Use the GraphViz library of tools to produce a picture of this graph. We
    // do this by generating a .dot file that describes the tree, and then
    // executes the dot command to convert that file into a .png file.
    private int inviz = 0;

    public void makePic(String name) {
        String dotFileName = name + ".dot";
        String picName = name + ".png";
        PrintWriter dotFile = null;
        try {
            dotFile = new PrintWriter(new BufferedWriter(new FileWriter(dotFileName)));
        }
        catch (IOException e) {
            return;
        }

        // boilerplate header for the
        dotFile.println("digraph tree {");
        dotFile.println("\tratio=0.5;");
        dotFile.println("\tsplines=false;");

        // recursively handle all the nodes in the tree
        inviz = 0;
        dotNode(root, dotFile);

        dotFile.println("}");
        dotFile.close();

        try {
            String cmd = "dot -Tpng -o" + picName + " " + dotFileName;
            Process p = Runtime.getRuntime().exec(cmd);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dotNode(Node<K, V> cur, PrintWriter file) {
        if (cur == null)
            return;

        // at least one child if we get here
        if (cur.left != null) {
            file.printf("\t\"%s: %s (%d)\":sw -> \"%s: %s (%d)\";\n",
                    cur.key.toString(), cur.value.toString(), cur.height,
                    cur.left.key.toString(), cur.left.value.toString(),
                    cur.left.height);
        }
        else {
            file.printf("%d [style=invis];\n", inviz);
            file.printf("\t\"%s: %s (%d)\":sw -> %d [style=invis];\n",
                    cur.key.toString(), cur.value.toString(), cur.height,
                    inviz);
            inviz++;
        }
        if (cur.right != null) {
            file.printf("\t\"%s: %s (%d)\":se -> \"%s: %s (%d)\";\n",
                    cur.key.toString(), cur.value.toString(), cur.height,
                    cur.right.key.toString(), cur.right.value.toString(),
                    cur.right.height);
        }
        else {
            file.printf("%d [style=invis];\n", inviz);
            file.printf("\t\"%s: %s (%d)\":se -> %d [style=invis];\n",
                    cur.key.toString(), cur.value.toString(), cur.height,
                    inviz);
            inviz++;
        }

        dotNode(cur.left, file);
        dotNode(cur.right, file);
    }

    public class Iterator {
        private Node<K, V> cur;

        public Iterator(Node<K, V> c) {
            cur = c;
        }

        public Iterator() {
            // find the first element in an in-order iteration
            if (root == null)
                cur = null;
            else
                cur = minNode(root);
        }

        public boolean hasNext() {
            return cur != null;
        }

        public void next() {
            if (cur == null)
                return;

            // iterate to the next item, using an in-order iteration
            if (cur.right != null) {
                cur = minNode(cur.right);
            }
            else {
                // find the closest ancestor we are in the left subtree of
                while (cur.parent != null && cur.parent.right == cur)
                    cur = cur.parent;
                // step up one more time -- why?
                cur = cur.parent;
            }
        }

        public K key() {
            return cur == null ? null : cur.key;
        }

        public V value() {
            return cur == null ? null : cur.value;
        }
    }

    private static class Node<K, V> {
        K key;
        V value;
        int height;

        Node<K, V> parent;
        Node<K, V> left;
        Node<K, V> right;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
            parent = left = right = null;
            height = 0;
        }

        void resetHeight() {
            int ell = left == null ? -1 : left.height;
            int r = right == null ? -1 : right.height;

            height = Math.max(ell, r) + 1;
        }
    }
}
