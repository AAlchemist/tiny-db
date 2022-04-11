package hw3;


import hw1.Field;
import hw1.RelationalOperator;

import java.util.ArrayDeque;
import java.util.Queue;


public class BPlusTree {

    private Node root;
    // pInner = pLeaf + 1
    // An internal node has [ceil(pInner / 2), pInner] children. #keys = #children - 1.
    // The root node has [2, pInner] children.
    // A leaf node has [ceil(pLeaf / 2), pLeaf] entries
    public BPlusTree(int pInner, int pLeaf) {
    	//your code here
        this.root = new LeafNode(pLeaf);
    }

    /*
    * Search down the B+ tree from the root.
    * Returns the leaf node that contains the entry (f, page).
    * */
    public LeafNode search(Field f) {
    	//your code here
        // 1. Find the leaf node
        LeafNode leafNode = searchHelper(f, this.root);
        // 2. Find the entry in the leaf node
        for (Entry entry: leafNode.getEntries())
            if (entry.getField().compare(RelationalOperator.EQ, f)) return leafNode;
        return null;
    }

    public LeafNode searchHelper(Field f, Node curNode) {
        if (curNode.isLeafNode()) return (LeafNode) curNode;
        InnerNode curInnerNode = (InnerNode) curNode;
        for (int i = 0; i < curInnerNode.getKeys().size(); ++i) {
            if (f.compare(RelationalOperator.LTE, curInnerNode.getKeys().get(i))) {
                return searchHelper(f, curInnerNode.getChildren().get(i));
            }
        }
        return searchHelper(f, curInnerNode.getChildren().get(curInnerNode.getChildren().size() - 1));
    }

    public LeafNode searchLeaf(Field f) {
        //your code here
        return searchHelper(f, this.root);
    }

    /*
    * Insert an entry into the B+ tree.
    * */
    public void insert(Entry e) {
    	//your code here
        LeafNode leafNode = searchLeaf(e.getField());
        if (leafNode == null) return;
        this.root = leafNode.addEntry(e);
    }
    
    public void delete(Entry e) {
    	//your code here
        LeafNode leafNode = search(e.getField());
        if (leafNode == null) {
            System.out.println("Not exist!");
            return;
        }
        this.root = leafNode.deleteEntry(e);
    }
    
    public Node getRoot() {
    	//your code here
    	return this.root;
    }

    /*
    * Print B+ tree layer by layer (BFS)
    * */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Queue<Node> q = new ArrayDeque<>();
        q.offer(this.root);
        while (!q.isEmpty()) {
            int size = q.size();
            for (int i = 0; i < size; ++i) {
                Node node = q.poll();
                if (node.isLeafNode()) {
                    LeafNode leaf = (LeafNode) node;
                    sb.append("[");
                    for (Entry entry: leaf.getEntries()) sb.append(entry.getField()).append(" ");
                    sb.append("]");
                } else {
                    InnerNode inner = (InnerNode) node;
                    sb.append("[");
                    for (Field field : inner.getKeys()) sb.append(field).append(" ");
                    sb.append("]");
                    for (Node child : inner.getChildren()) q.offer(child);
                }
            }
            sb.append("\n");

        }
        return sb.toString();
    }

}
