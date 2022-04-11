package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;

public class InnerNode implements Node {
    private final int degree;
    private final int nodeCount;
    private final int minKeys;
    private final int minChildren;
    private ArrayList<Field> keys;
    private ArrayList<Node> children;
    private InnerNode parent;


    public InnerNode(int degree) {
        //your code here
        this.degree = degree;
        this.nodeCount = degree - 1;
        keys = new ArrayList<>();
        children = new ArrayList<>();
        parent = null;
        // [ceil(pInner / 2), pInner] children. (half full) , #keys = #children - 1.
        minChildren = degree / 2 + degree % 2;
        minKeys = minChildren - 1;
    }

    public ArrayList<Field> getKeys() {
        //your code here
        return keys;
    }

    public ArrayList<Node> getChildren() {
        //your code here
        return children;
    }

    public int getDegree() {
        //your code here
        return this.degree;
    }

    public boolean isLeafNode() {
        return false;
    }

    public boolean isFull() {
        return keys.size() == this.nodeCount;
    }

    /*
    * Add a key and a child into the inner node.
    * */
    public void addKey(Field newField, Node newChild) {
        // 1. If the parent node is not full, then add the key and new child directly.
        if (!isFull()) {
            insertKey(newField);
            insertChild(newChild);
        } else {
            // 2. If the parent is full, then we need to split the parent into two halves.
            InnerNode newNode = new InnerNode(this.degree);
            // insert anyway, then split.
            insertKey(newField);
            insertChild(newChild);

            // 2.1. Split the node and reorganize keys
            // When we split the leaf node, we pass a copy of the largest field to the parent.
            // When we split the inner node, however, we pass the largest field to the parent and remove it from the current node.
            Field midField = keys.remove(keys.size() / 2);
            // add the right half to the new node's key list
            int keysCount = keys.size();
            int oldKeysCount = keysCount / 2 + (keysCount % 2);
            newNode.keys = new ArrayList<>(keys.subList(oldKeysCount, keysCount));
            // delete the right half of the old node's key list
            keys = new ArrayList<>(keys.subList(0, oldKeysCount));

            // 2.2. reorganize the children list  (#children = #keys + 1)
            int oldChildrenCount = oldKeysCount + 1;
            newNode.children = new ArrayList<>(this.children.subList(oldChildrenCount, children.size()));
            for (int i = oldChildrenCount; i < children.size(); ++i) {
                if (this.children.get(i).isLeafNode()) {
                    ((LeafNode) this.children.get(i)).setParent(newNode);
                } else {
                    ((InnerNode) this.children.get(i)).setParent(newNode);
                }
            }
            this.children = new ArrayList<>(this.children.subList(0, oldChildrenCount));

            // create a innerNode as their parent (new root)
            if (this.parent == null) {
                this.parent = new InnerNode(this.degree);
                this.parent.children.add(this);
            }
            newNode.parent = this.parent;
            parent.addKey(midField, newNode);
            // Insert the largest key of the left new node into the parent

        }
    }

    /*
    * Insert the key into the keys list using binary search
    * */
    public void insertKey(Field field) {
        int left = 0, right = keys.size() - 1;
        // find the first entry e that e.field > entry.field (binary search the insert index)
        while (left < right) {
            int mid = left + right >> 1;
            if (keys.get(mid).compare(RelationalOperator.GT, field)) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }
        // the new entry's field is greater than any other field in the entryList, push the new entry in the back
        if (keys.isEmpty() ||
                keys.get(right).compare(RelationalOperator.LT, field))
            keys.add(field);
            // insert the new entry beween entryList[right - 1] and entryList[right].
        else keys.add(right, field);
    }

    /*
    * Insert the new child into the children list using binary search
    * */
    public void insertChild(Node child) {
        int left = 0, right = children.size() - 1;
        Field childMaxField;
        if (child.isLeafNode()) {
            LeafNode leafChild = (LeafNode) child;
            childMaxField = leafChild.getEntries().get(leafChild.getEntries().size() - 1).getField();
        } else {
            InnerNode innerChild = (InnerNode) child;
            childMaxField = innerChild.getKeys().get(innerChild.getKeys().size() - 1);
        }
        // find the first entry e that e.field > entry.field (binary search the insert index)
        while (left < right) {
            int mid = left + right >> 1;
            Field midNodeMaxField = null;
            if (children.get(mid).isLeafNode()) {
                LeafNode midLeaf = (LeafNode) children.get(mid);
                midNodeMaxField = midLeaf.getEntries().get(midLeaf.getEntries().size() - 1).getField();
            } else {
                InnerNode midInner = (InnerNode) children.get(mid);
                midNodeMaxField = midInner.getKeys().get(midInner.getKeys().size() - 1);
            }
            if (midNodeMaxField.compare(RelationalOperator.GT, childMaxField)) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }

        Field rightMax;
        if (children.get(right).isLeafNode()) {
            rightMax = ((LeafNode) children.get(right)).getEntries().get(((LeafNode) children.get(right)).getEntries().size() - 1).getField();
        } else {
            rightMax = ((InnerNode) children.get(right)).getKeys().get(((InnerNode) children.get(right)).getKeys().size() - 1);
        }

        // the new entry's field is greater than any other field in the entryList, push the new entry in the back
        if (rightMax.compare(RelationalOperator.LT, childMaxField)) {
            children.add(child);
        }

        // insert the new entry between entryList[right - 1] and entryList[right].
        else children.add(right, child);
    }

    public InnerNode getParent() {
        return this.parent;
    }

    /*
    * Reorganize search keys of the inner node based on it's children
    * */
    public Node updateKeys() {
        this.keys.clear();
        for (int i = 0; i < this.children.size() - 1; ++i) {
            if (this.children.get(i).isLeafNode()) {
                LeafNode leafChild = (LeafNode) this.children.get(i);
                this.keys.add(leafChild.getEntries().get(leafChild.getEntries().size() - 1).getField());
            } else {
                InnerNode innerChild = (InnerNode) this.children.get(i);
                LeafNode maxChild = findLargestLeaf(innerChild);
                this.keys.add(maxChild.getEntries().get(maxChild.getEntries().size() - 1).getField());
            }
        }
        if (this.keys.size() == 0) {
            if (this.children.get(0).isLeafNode()) {
                LeafNode leafChild = (LeafNode) this.children.get(0);
                this.keys.add(leafChild.getEntries().get(leafChild.getEntries().size() - 1).getField());
            } else {
                InnerNode innerChild = (InnerNode) this.children.get(0);
                LeafNode maxChild = findLargestLeaf(innerChild);
                this.keys.add(maxChild.getEntries().get(maxChild.getEntries().size() - 1).getField());
            }
        }
        // Handle deficiency of the inner node if needed.
        if (this.keys.size() < this.minKeys || this.children.size() < this.minChildren)
            return handleDeficiency(this);

        // push through
//        if (this.keys.isEmpty() && this.children.size() == 1) pushThrough();
        return getRoot();
    }

    /*
    * Find the largest entry in the current subtree
    * */
    public LeafNode findLargestLeaf(InnerNode node) {
        Node rightmostChild = node.getChildren().get(node.getChildren().size() - 1);
        if (rightmostChild.isLeafNode()) {
            return (LeafNode) rightmostChild;
        } else {
            InnerNode innerChild = (InnerNode) rightmostChild;
            return findLargestLeaf(innerChild);
        }
    }

    /*
    * Returns the root
    * */
    private Node handleDeficiency(InnerNode node) {
        Node root = getRoot();
        InnerNode parent = node.parent;
        // The node is the root, delete itself, return the new root. (the height of the tree is shrinked)
        if (parent == null && node.children.size() == 1) {
            for (Node child: node.children) {
                if (child.isLeafNode()) {
                    ((LeafNode) child).setParent(null);
                } else {
                    ((InnerNode) child).setParent(null);
                }
                return child;
            }
            return null;
        } else if (parent == null) {
            return node;
        }
        // 1. Borrow from siblings
        InnerNode leftSibling = findLeftSibling(node), rightSibling = findRightSibling(node);
        // Borrow from the left first, then right.
        if (leftSibling != null && leftSibling.getKeys().size() > leftSibling.minKeys) {
            Node borrowedNode = leftSibling.children.remove(leftSibling.children.size() - 1);
            node.children.add(0, borrowedNode);
            // Update search keys of the sibling, the node, and the parent after borrowing.
            leftSibling.updateKeys();
            node.updateKeys();
            parent.updateKeys();
        } else if (rightSibling != null && rightSibling.getKeys().size() > rightSibling.minKeys) {
            Node borrowedNode = rightSibling.children.remove(0);
            node.children.add(borrowedNode);
            rightSibling.updateKeys();
            node.updateKeys();
            parent.updateKeys();
        }
        // Merge the node into the sibling (might change the root)
        else if (leftSibling != null) {
            leftSibling.keys.addAll(node.keys);
            leftSibling.children.addAll(node.children);
            leftSibling.updateKeys();
            parent.children.remove(node);
            node.parent = null;
            root = parent.updateKeys();
        } else if (rightSibling != null) {
            rightSibling.keys.addAll(0, node.keys);
            rightSibling.children.addAll(0, node.children);
            rightSibling.updateKeys();
            parent.children.remove(node);
            node.parent = null;
            root = parent.updateKeys();
        }
        // Remove level (the height of the tree gets shrinked)
        else {
            return handleDeficiency(parent);
        }
        if (!root.isLeafNode())
            ((InnerNode)root).updateKeys();
        return root;
    }

    private InnerNode findLeftSibling(InnerNode node) {
        InnerNode parent = node.parent;
        // 1. Find the index in the parent's children list (guarantee to have i < n)
        int i;
        for (i = 0; i < parent.getChildren().size(); ++i)
            if (parent.getChildren().get(i).equals(this)) break;
        if (i > 0) return (InnerNode) parent.getChildren().get(i - 1);
        return null;
    }

    private InnerNode findRightSibling(InnerNode node) {
        InnerNode parent = node.parent;
        int i, n = parent.getChildren().size();
        for (i = 0; i < n; ++i)
            if (parent.getChildren().get(i).equals(this)) break;
        if (i < n - 1) return (InnerNode) parent.getChildren().get(i + 1);
        return null;
    }

    private Node getRoot() {
        InnerNode tmp;
        if (this.parent != null) {
            tmp = this.parent;
            while (tmp.getParent() != null) {
                tmp = tmp.getParent();
            }
            return tmp;
        }
        else {
            return this;
        }
    }

//    private void pushThrough() {
//        InnerNode parent = this.parent;
//        if (parent == null) return;
//        int i;
//        for (i = 0; i < parent.getChildren().size(); ++i)
//            if (parent.getChildren().get(i).equals(this)) break;
//        Node borrowChild = null;
//        if (i > 0) {
//            InnerNode leftSibling = ((InnerNode) parent.getChildren().get(i - 1));
//            if (leftSibling.getChildren().size() > leftSibling.getDegree() / 2 + leftSibling.getDegree() % 2) {
//                borrowChild = leftSibling.getChildren().remove(leftSibling.getChildren().size() - 1);
//                this.getChildren().add(0,borrowChild);
//                parent.updateKeys();
//                this.updateKeys();
//
//                leftSibling.updateKeys();
//            }
//        }
//        else if (i < parent.getChildren().size() - 1) {
//            InnerNode rightSibling = ((InnerNode) parent.getChildren().get(i + 1));
//            if (rightSibling.getChildren().size() > rightSibling.getDegree() / 2 + rightSibling.getDegree() % 2) {
//                borrowChild = rightSibling.getChildren().remove(0);
//                this.getChildren().add(borrowChild);
//                parent.updateKeys();
//                this.updateKeys();
//
//                rightSibling.updateKeys();
//            }
//        }
//        if (borrowChild == null) {
//            // can't push through, try to merge to the left sibling
//            if (i > 0) {
//                InnerNode leftSibling = (InnerNode) this.parent.getChildren().get(i - 1);
//                for (int j = 0; j < leftSibling.getChildren().size(); ++j) {
//                    if (leftSibling.getChildren().get(j).isLeafNode())
//                        ((LeafNode) leftSibling.getChildren().get(j)).setParent(this);
//                    else
//                        ((InnerNode) leftSibling.getChildren().get(j)).setParent(this);
//                }
//                this.getChildren().addAll(0, leftSibling.getChildren());
//
//                this.parent.getChildren().remove(leftSibling);
//                this.updateKeys();
//                if (this.getParent().getChildren().size() == this.getParent().getKeys().size()) {
//                    if (this.getParent().getParent() == null) this.setParent(null);
//                    else this.getParent().pushThrough();
//                }
//            } else if (i < this.parent.getChildren().size() - 1) {
//                InnerNode rightSibling = (InnerNode) this.parent.getChildren().get(i + 1);
//                for (int j = 0; j < rightSibling.getChildren().size(); ++j) {
//                    if (rightSibling.getChildren().get(j).isLeafNode())
//                        ((LeafNode) rightSibling.getChildren().get(j)).setParent(this);
//                    else
//                        ((InnerNode) rightSibling.getChildren().get(j)).setParent(this);
//                }
//                this.getChildren().addAll(rightSibling.getChildren());
//
//                this.parent.getChildren().remove(rightSibling);
//                this.updateKeys();
//                if (this.getParent().getChildren().size() == this.getParent().getKeys().size()) {
//                    if (this.getParent().getParent() == null) this.setParent(null);
//                    else this.getParent().pushThrough();
//                }
//            }
////            this.parent = null;
//        }
//
//    }
//
//
//
//    private void removeLevel() {
//        // children of this and siblings
//        ArrayList<Node> children = new ArrayList<>();
//
//    }

    public void setParent(InnerNode parent) {
        this.parent = parent;
    }
}