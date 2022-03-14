package hw3;


import hw1.Field;
import hw1.RelationalOperator;


public class BPlusTree {

    private Node root;

    public BPlusTree(int pInner, int pLeaf) {
    	//your code here
        this.root = new LeafNode(pLeaf);
        // assume that pInner = pLeaf + 1
    }
    
    public LeafNode search(Field f) {
    	//your code here
        LeafNode leafNode = searchHelper(f, this.root);
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
            else if (i == curInnerNode.getKeys().size()- 1 && f.compare(RelationalOperator.GT, curInnerNode.getKeys().get(i))) {
                return searchHelper(f, curInnerNode.getChildren().get(i + 1));
            }

        }
        return null;
    }

    public LeafNode searchLeaf(Field f) {
        //your code here
        return searchHelper(f, this.root);
    }

    
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
        this.root = leafNode.deleteEntry(e, this.root);
    }
    
    public Node getRoot() {
    	//your code here
    	return this.root;
    }

}
