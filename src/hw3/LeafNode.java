package hw3;

import hw1.RelationalOperator;

import java.util.ArrayList;

public class LeafNode implements Node {
	private final int capacity;
	private ArrayList<Entry> entryList;
	private LeafNode preNode;
	private LeafNode nextNode;
	private InnerNode parent;
	// A leaf node has [ceil(pLeaf / 2), pLeaf] entries
	private final int minEntries;
	
	public LeafNode(int degree) {
		//your code here
		this.capacity = degree;
		this.entryList = new ArrayList<>();
		this.preNode = null;
		this.nextNode = null;
		this.parent = null;
		this.minEntries = capacity / 2 + capacity % 2;
	}
	
	public ArrayList<Entry> getEntries() {
		//your code here
		return entryList;
	}

	public int getDegree() {
		//your code here
		return this.capacity;
	}
	
	public boolean isLeafNode() {
		return true;
	}

	public boolean isFull() {
		return entryList.size() == this.capacity;
	}

	/*
	* Search up the B+ tree and return the root.
	* */
	public Node getRoot() {
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

	/*
	* Inserts an entry into this leaf node. If the node has already been full, then split.
	* Returns the root node of the B+ tree after the insertion.
	* */
	public Node addEntry(Entry entry) {
		for (Entry e: entryList)
			// insertion is requested for a value that is already in the tree
			if (e.getField().equals(entry.getField())) return this;
		// 1. If this leaf is not full, insert the entry directly, and the root would not be changed.
		if (!isFull()) {
			insertEntry(entry);
			return getRoot();
		}
		// 2. The leaf is full, we need to split the current leaf into two halves.
		else {
			// 2.1. Split the old leaf into two new leaves
			LeafNode newLeaf = new LeafNode(this.capacity);
			ArrayList<Entry> newEntryList;
			// insert anyway, split the overfull entry into two halves later.
			insertEntry(entry);
			int entryCount = this.entryList.size();
			int oldEntryListCount = entryCount / 2 + (entryCount % 2);
			// add the right half to the new leaf's entry list
			newEntryList = new ArrayList<>(this.entryList.subList(oldEntryListCount, entryCount));
			newLeaf.entryList = newEntryList;
			// delete the right half of the old leaf's entry list
			this.entryList = new ArrayList<>(this.entryList.subList(0, oldEntryListCount));

			// 2.2. Update the double linked list of leaves
			if (this.nextNode == null) {
				this.nextNode = newLeaf;
				newLeaf.preNode = this;
			} else {
				LeafNode tmp = this.nextNode;
				this.nextNode = newLeaf;
				newLeaf.nextNode = tmp;
				newLeaf.preNode = this;
				tmp.preNode = newLeaf;
			}

			// 2.3. Add the new key into the parent node.
			// Create a innerNode as their parent (root) if it is not exist
			if (this.parent == null) {
				this.parent = new InnerNode(this.capacity + 1);  // pInner = pLeaf + 1
				this.parent.getChildren().add(this);
			}
			newLeaf.parent = this.parent;
			// insert the largest key of the left new node into the parent
			// update the children list of the parent at the same time
			parent.addKey(this.entryList.get(entryList.size() - 1).getField(), newLeaf);
			return getRoot();
		}
	}

	/*
	* Insert the entry into the leaf node using binary search
	* */
	public void insertEntry(Entry entry) {
		int left = 0, right = entryList.size() - 1;
		// find the first entry e that e.field > entry.field (binary search the insert index)
		while (left < right) {
			int mid = left + right >> 1;
			if (entryList.get(mid).getField().compare(RelationalOperator.GT, entry.getField())) {
				right = mid;
			} else {
				left = mid + 1;
			}
		}
		// the new entry's field is greater than any other field in the entryList, push the new entry in the back
		if (entryList.isEmpty() ||
				entryList.get(right).getField().compare(RelationalOperator.LT, entry.getField()))
			entryList.add(entry);
		// insert the new entry between entryList[right - 1] and entryList[right].
		else entryList.add(right, entry);
	}

	public Node deleteEntry(Entry e) {
		Node root = getRoot();
		// 1. Remove the entry from the leaf.
		for (int i = 0; i < this.entryList.size(); ++i) {
			if (this.entryList.get(i).getField().compare(RelationalOperator.EQ, e.getField())) {
				this.entryList.remove(i);
				break;
			}
		}
		// There is no entry left.
		if (this.parent == null && this.entryList.size() == 0) {
			return null;
		}
		// 2. If there is less than minEntries entries in the leaf.
		if (this.parent != null && this.entryList.size() < this.minEntries) {
			// 2.1. if it can borrow an entry from the left sibling,
			// 	    borrow it and update the corresponding key of the parent.
			if (this.preNode != null && this.preNode.entryList.size() > this.preNode.minEntries) {
				this.insertEntry(this.preNode.entryList.remove(this.preNode.entryList.size() - 1));
				// Find the left sibling's index in its parent's children list.
				int index = 0;
				for (int i = 0; i < this.parent.getChildren().size(); ++i) {
					if (this.parent.getChildren().get(i).equals(this)) {
						index = i - 1;
						break;
					}
				}
				// update the search key of the parent.
				this.parent.getKeys().set(index, this.preNode.entryList.get(this.preNode.entryList.size() - 1).getField());
			}
			// can borrow from right sibling
			else if (this.nextNode != null
					&& this.nextNode.entryList.size() > this.nextNode.getDegree() / 2 + this.nextNode.getDegree() % 2) {
				this.insertEntry(this.nextNode.entryList.remove(0));
				int index = 0;
				for (int i = 0; i < this.parent.getChildren().size(); ++i) {
					if (this.parent.getChildren().get(i).equals(this)) {
						index = i;
						break;
					}
				}
				this.parent.getKeys().set(index, this.entryList.get(this.entryList.size() - 1).getField());
			}
			// 2.2. If it can't borrow from siblings, merge it into one of the siblings
			else  {
				int leftOrRight = 0;
				if (this.preNode != null) {
					// merge the rest of the entries to the left sibling
					preNode.entryList.addAll(this.entryList);
					leftOrRight = 1;
				} else if (this.nextNode != null) {
					// merge the rest of the entries to the right sibling
					nextNode.entryList.addAll(0, this.entryList);
					leftOrRight = 2;
				}

				// After merging, delete the empty leaf
				if (this.preNode != null) this.preNode.nextNode = this.nextNode;
				if (this.nextNode != null) this.nextNode.preNode = this.preNode;
				this.parent.getChildren().remove(this);
				// update the parent node, push through is needed
				root = this.parent.updateKeys();
				// Corner case: delete level to root.
//				if (this.parent.getKeys().size() == 0) {
//					if (leftOrRight == 1) return this.preNode;
//					else if (leftOrRight == 2) return this.nextNode;
//				}
//				else if (this.parent.getParent() == null) return this.parent;

			}
		}
		return root;
	}

	public void setParent(InnerNode parent) {
		this.parent = parent;
	}

}