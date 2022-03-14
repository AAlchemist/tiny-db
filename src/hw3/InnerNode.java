package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;

public class InnerNode implements Node {
    private final int degree;
    private final int nodeCount;
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

    public InnerNode addKey(Field newField, Node newChild) {
        if (!isFull()) {
            insertKey(newField);
            insertChild(newChild);
            return this;
        } else {
            // need split
            InnerNode newNode = new InnerNode(this.degree);
            ArrayList<Field> newKeys = new ArrayList<>();
            int keysCount = this.keys.size();
            int oldKeysCount = keysCount / 2 + (keysCount % 2);

            // insert anyway
            insertKey(newField);

            Field midField = keys.remove(keys.size() / 2);
            // add the right half to the new node's key list
//			for (int i = oldKeysCount; i < keysCount; ++i) newKeys.add(keys.get(i));
            newKeys = new ArrayList<>(keys.subList(oldKeysCount, keysCount));
            newNode.keys = newKeys;
            // delete the right half of the old node's key list
            keys = new ArrayList<>(keys.subList(0, oldKeysCount));
            // reorganize the children list
            insertChild(newChild);
            newNode.children = new ArrayList<>(this.children.subList(oldKeysCount + 1, keysCount + 2));
            for (int i = oldKeysCount + 1; i < keysCount + 2; ++i) {
                if (this.children.get(i).isLeafNode()) {
                    ((LeafNode) this.children.get(i)).setParent(newNode);
                } else {
                    ((InnerNode) this.children.get(i)).setParent(newNode);
                }
            }
            this.children = new ArrayList<>(this.children.subList(0, oldKeysCount + 1));

            // create a innerNode as their parent (new root)
            InnerNode root = null;
            if (this.parent == null) {
                this.parent = new InnerNode(this.degree);
                this.parent.children.add(this);
            }
            root = parent.addKey(midField, newNode);
//			this.parent.getChildren().add(this);
//			this.parent.getChildren().add(newNode);
            newNode.parent = this.parent;
            // Insert the largest key of the left new node into the parent
            return root;
        }
    }

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

    public void insertChild(Node child) {
        int left = 0, right = children.size() - 1;
        Field childMaxField = null;
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

        Field rightMax = null;
        if (children.get(right).isLeafNode()) {
            rightMax = ((LeafNode) children.get(right)).getEntries().get(((LeafNode) children.get(right)).getEntries().size() - 1).getField();
        } else {
            rightMax = ((InnerNode) children.get(right)).getKeys().get(((InnerNode) children.get(right)).getKeys().size() - 1);
        }

        // the new entry's field is greater than any other field in the entryList, push the new entry in the back
        if (rightMax.compare(RelationalOperator.LT, childMaxField)) {
            children.add(child);
        }

        // insert the new entry beween entryList[right - 1] and entryList[right].
        else children.add(right, child);
    }

    public InnerNode getParent() {
        return this.parent;
    }

    public void updateKeys() {
        this.keys.clear();

        for (int i = 0; i < this.children.size() - 1; ++i) {
            if (this.children.get(i).isLeafNode()) {
                LeafNode leafChild = (LeafNode) this.children.get(i);
//                if (temp.size() >= i + 1 && leafChild.getEntries().get(leafChild.getEntries().size() - 1).getField().compare(RelationalOperator.LTE, temp.get(i)))
//                    this.keys.add(temp.get(i));
//                else
                    this.keys.add(leafChild.getEntries().get(leafChild.getEntries().size() - 1).getField());
            } else {
                InnerNode innerChild = (InnerNode) this.children.get(i);
//                if (temp.size() >= i + 1 && innerChild.getKeys().get(innerChild.getKeys().size() - 1).compare(RelationalOperator.LTE, temp.get(i)))
//                    this.keys.add(temp.get(i));
//                else
                    this.keys.add(innerChild.getKeys().get(innerChild.getKeys().size() - 1));
            }
        }

//        System.out.println(this.keys.size() + " " + this.children.size());
//        if (this.keys.size() > this.children.size() - 1)
//            this.keys = new ArrayList<>(this.keys.subList(0, this.children.size() - 1));
//        // push through
//        if (addMode && this.keys.size() > this.children.size() - 1)
//            this.keys = new ArrayList<>(this.keys.subList(0, this.children.size()));
        // push through
        if (this.keys.isEmpty() && this.children.size() == 1) pushThrough();
//        if (this.parent != null) this.parent.updateKeys();
    }

    public void pushThrough() {
        InnerNode parent = this.parent;
        if (parent == null) return;
        int i = 0;
        for (i = 0; i < parent.getChildren().size(); ++i)
            if (parent.getChildren().get(i).equals(this)) break;
        Node borrowChild = null;
        if (i > 0) {
            InnerNode leftSibling = ((InnerNode) parent.getChildren().get(i - 1));
            if (leftSibling.getChildren().size() > leftSibling.getDegree() / 2 + leftSibling.getDegree() % 2) {
                borrowChild = leftSibling.getChildren().remove(leftSibling.getChildren().size() - 1);
                this.getChildren().add(0,borrowChild);
                parent.updateKeys();
                this.updateKeys();

                leftSibling.updateKeys();
            }
        }
        else if (i < parent.getChildren().size() - 1) {
            InnerNode rightSibling = ((InnerNode) parent.getChildren().get(i + 1));
            if (rightSibling.getChildren().size() > rightSibling.getDegree() / 2 + rightSibling.getDegree() % 2) {
                borrowChild = rightSibling.getChildren().remove(0);
                this.getChildren().add(borrowChild);
                parent.updateKeys();
                this.updateKeys();

                rightSibling.updateKeys();
            }
        }
        if (borrowChild == null) {
            // can't push through, try to merge to the left sibling
            if (i > 0) {
                InnerNode leftSibling = (InnerNode) this.parent.getChildren().get(i - 1);
                for (int j = 0; j < leftSibling.getChildren().size(); ++j) {
                    if (leftSibling.getChildren().get(j).isLeafNode())
                        ((LeafNode) leftSibling.getChildren().get(j)).setParent(this);
                    else
                        ((InnerNode) leftSibling.getChildren().get(j)).setParent(this);
                }
                this.getChildren().addAll(0, leftSibling.getChildren());

                this.parent.getChildren().remove(leftSibling);
                this.updateKeys();
                if (this.getParent().getChildren().size() == this.getParent().getKeys().size()) {
                    if (this.getParent().getParent() == null) this.setParent(null);
                    else this.getParent().pushThrough();
                }
            } else if (i < this.parent.getChildren().size() - 1) {
                InnerNode rightSibling = (InnerNode) this.parent.getChildren().get(i + 1);
                for (int j = 0; j < rightSibling.getChildren().size(); ++j) {
                    if (rightSibling.getChildren().get(j).isLeafNode())
                        ((LeafNode) rightSibling.getChildren().get(j)).setParent(this);
                    else
                        ((InnerNode) rightSibling.getChildren().get(j)).setParent(this);
                }
                this.getChildren().addAll(rightSibling.getChildren());

                this.parent.getChildren().remove(rightSibling);
                this.updateKeys();
                if (this.getParent().getChildren().size() == this.getParent().getKeys().size()) {
                    if (this.getParent().getParent() == null) this.setParent(null);
                    else this.getParent().pushThrough();
                }
            }
//            this.parent = null;
        }

    }

    private void removeLevel() {
        // children of this and siblings
        ArrayList<Node> children = new ArrayList<>();

    }

    public void setParent(InnerNode parent) {
        this.parent = parent;
    }
}