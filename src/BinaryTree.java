import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

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
        Stack<Node<K,V>> stack = new Stack<>();
        var v = remove(key, root, stack);
        if (v != null)
            size--;
        return v;
    }

    private V remove(K key, Node<K,V> cur, Stack<Node<K,V>> stack) {
        if (cur == null)
            return null;

        int c = comp.compare(key, cur.key);
        if (c < 0) {
            stack.add(cur);
            return remove(key, cur.left, stack);
        }
        else if (c > 0) {
            stack.add(cur);
            return remove(key, cur.right, stack);
        }
        else {
            // found our value.
            V temp = cur.value;
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
                //TODO findSearchPath method
                var rightMin = minNode(cur.right);

                LinkedList<Node<K, V>> stack2 = findSearchPath(cur);
                Node<K, V> test;
                while(!stack2.isEmpty())
                    stack.add(stack2.remove());


                // place rightMin's data here; this node is hooked up right
                cur.key = rightMin.key;
                cur.value = rightMin.value;
                // remove rightMin. This call will do it recursively. Note we
                // ARE NOT returning the result!
                remove(rightMin.key, rightMin, stack);
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
            //TODO not sure if this is right
            while(!stack.isEmpty()){
                Node<K,V> pop = stack.pop();
                pop.resetHeight();
                balance(pop);
            }
            return temp;
        }
    }

    public LinkedList<Node<K,V>> findSearchPath(Node<K, V> root){
        LinkedList<Node<K,V>> out = new LinkedList<>();
        return findSearchPath(root, out);
    }

    private LinkedList<Node<K,V>> findSearchPath(Node<K, V> cur, LinkedList<Node<K,V>> out){
        out.add(cur);
        out.add(cur.right);
        cur = cur.right;
        while (cur.left != null) {
            cur = cur.left;
            out.add(cur);
        }
        return out;
    }

    private Node<K, V> minNode(Node<K,V> right) {
        while (right.left != null)
            right = right.left;
        return right;
    }
    private void balance(Node<K, V> cur) {
        int leftHeight = -1;
        int rightHeight = -1;
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
            else if (leftChild.right.height < leftChild.left.height)
                rotateLL(cur);
            else rotateLR(cur);
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

    private void rotateLL(Node<K, V> N) {
        Node<K, V> L = N.left;
        Node<K, V> LR = null;
        Node<K, V> NPar = N.parent;
        if (L.right != null)
            LR = L.right;

        L.parent = NPar;
        L.right = N;
        if (N.parent == null) {
            root = L;
        }
        if (NPar != null) {
            if (NPar.left != null) {
                if (NPar.left.equals(N)) {
                    NPar.left = L;
                }
            }
            if (NPar.right != null) {
                if (NPar.right.equals(N)) {
                    NPar.right = L;
                }
            }
        }
        N.parent = L;
        N.left = LR;
        if (LR != null)
            LR.parent = N;
        if (LR != null) {
            LR.resetHeight();
        }
        N.resetHeight();
        L.resetHeight();
    }

    private void rotateLR(Node<K, V> N) {
        Node<K, V> LR = N.left.right;
        Node<K, V> LRR = null;
        Node<K, V> NPar = N.parent;
        if (LR.right != null)
            LRR = LR.right;
        Node<K, V> LRL = null;
        if (LR.left != null)
            LRL = LR.left;

        LR.parent = NPar;
        LR.left = N.left;
        LR.left.parent = LR;
        LR.right = N;
        if (NPar != null) {
            if (NPar.left != null) {
                if (NPar.left.equals(N)) {
                    NPar.left = LR;
                }
            }
            if (NPar.right != null) {
                if (NPar.right.equals(N)) {
                    NPar.right = LR;
                }
            }
        }
        N.left = LRR;
        N.parent = LR;
        LR.left.right = LRL;
        if (LRR != null)
            LRR.parent = N;
        if (LRL != null)
            LRL.parent = LR.left;
        if (LR.parent == null) {
            root = LR;
        }
        //Might not need to reset height here?
        LR.left.resetHeight();
        N.resetHeight();
        LR.resetHeight();
    }



    private void rotateRR(Node<K, V> N) {
        Node<K, V> R = N.right;
        Node<K, V> RL = null;
        Node<K, V> NPar = N.parent;
        if (R.left != null)
            RL = R.left;

        R.parent = NPar;
        R.left = N;
        if (N.parent == null) {
            root = R;
        }
        if (NPar != null) {
            if (NPar.left != null) {
                if (NPar.left.equals(N)) {
                    NPar.left = R;
                }
            }
            if (NPar.right != null) {
                if (NPar.right.equals(N)) {
                    NPar.right = R;
                }
            }
        }
        N.parent = R;
        N.right = RL;
        if (RL != null)
            RL.parent = N;
        if (RL != null) {
            RL.resetHeight();
        }
        N.resetHeight();
        R.resetHeight();
    }

    private void rotateRL(Node<K, V> N) {
        Node<K, V> RL = N.right.left;
        Node<K, V> RLL = null;
        Node<K, V> NPar = N.parent;
        if (RL.left != null)
            RLL = RL.left;
        Node<K, V> RLR = null;
        if (RL.right != null)
            RLR = RL.right;

        RL.parent = NPar;
        RL.right = N.right;
        RL.right.parent = RL;
        RL.left = N;
        if (NPar != null) {
            if (NPar.left != null) {
                if (NPar.left.equals(N)) {
                    NPar.left = RL;
                }
            }
            if (NPar.right != null) {
                if (NPar.right.equals(N)) {
                    NPar.right = RL;
                }
            }
        }
        N.right = RLL;
        N.parent = RL;
        RL.right.left = RLR;
        if (RLL != null)
            RLL.parent = N;
        if (RLR != null)
            RLR.parent = RL.right;
        if (RL.parent == null) {
            root = RL;
        }
        RL.right.resetHeight();
        N.resetHeight();
        RL.resetHeight();
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

    public void isBalanced() {
        if (isBalanced(root)) {
            System.out.println("This tree is balanced!");
        }
        else {
            System.out.println("NOT BALANCED-------------------------");
        }
    }

    private Boolean isBalanced(Node<K, V> root) {
        Boolean isRight;
        Boolean isLeft;
        if (root.right != null) {
            isRight = isBalanced(root.right);
        }
        else isRight = true;
        if (root.left != null) {
            isLeft = isBalanced(root.left);
        }
        else isLeft = true;
        return isRight && isLeft;
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
