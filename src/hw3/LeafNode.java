package hw3;

import hw1.RelationalOperator;

import java.util.ArrayList;

public class LeafNode implements Node {
	private int capacity;
	private ArrayList<Entry> entryList;
	private LeafNode preNode;
	private LeafNode nextNode;
	private InnerNode parent;
	
	public LeafNode(int degree) {
		//your code here
		this.capacity = degree;
		this.entryList = new ArrayList<>();
		this.preNode = null;
		this.nextNode = null;
		this.parent = null;
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

	public Node addEntry(Entry entry) {
		for (Entry e: entryList)
			// insertion is requested for a value that is already in the tree
			if (e.getField().equals(entry.getField())) return this;

		if (!isFull()) {
			insertEntry(entry);
			InnerNode tmp = null;
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
		else {
			// split the old leaf into two new leaves
			LeafNode newLeaf = new LeafNode(this.capacity);
			ArrayList<Entry> newEntryList = new ArrayList<>();
			int entryCount = entryList.size() + 1;
			int oldEntryListCount = entryCount / 2 + (entryCount % 2);

			// insert anyway
			insertEntry(entry);
			// add the right half to the new leaf's entry list
//			for (int i = oldEntryListCount; i < entryCount; ++i) newEntryList.add(entryList.get(i));
			newEntryList = new ArrayList<>(entryList.subList(oldEntryListCount, entryCount));
			newLeaf.entryList = newEntryList;
			// delete the right half of the old leaf's entry list
			entryList = new ArrayList<>(entryList.subList(0, oldEntryListCount));
			// set pre/next pointer
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

			// create a innerNode as their parent if it is not exist
			InnerNode root;
			if (this.parent == null) {
				this.parent = new InnerNode(this.capacity + 1);
				this.parent.getChildren().add(this);
			}
			// insert the largest key of the left new node into the parent
			// update the children list of the parent at the same time
			root = parent.addKey(this.entryList.get(entryList.size() - 1).getField(), newLeaf);
			// add the newLeaf into the children list of the parent as well
//			this.parent.getChildren().add(newLeaf);
			newLeaf.parent = this.parent;
			return root;
		}
	}

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
		// insert the new entry beween entryList[right - 1] and entryList[right].
		else entryList.add(right, entry);
	}

	public Node deleteEntry(Entry e, Node root) {
		for (int i = 0; i < this.entryList.size(); ++i) {
			if (this.entryList.get(i).getField().compare(RelationalOperator.EQ, e.getField())) {
				this.entryList.remove(i);
				break;
			}
		}
		if (this.parent == null && this.entryList.size() == 0) {
			return null;
		}

		if (this.parent != null && this.entryList.size() < this.getDegree() / 2 + this.getDegree() % 2) {
			// can borrow from left sibling
			if (this.preNode != null
				&& this.preNode.entryList.size() > this.preNode.getDegree() / 2 + this.preNode.getDegree() % 2) {
				this.insertEntry(this.preNode.entryList.remove(this.preNode.entryList.size() - 1));
				int index = 0;
				for (int i = 0; i < this.parent.getChildren().size(); ++i) {
					if (this.parent.getChildren().get(i).equals(this)) {
						index = i - 1;
						break;
					}
				}
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
			// can't borrow, merge
			else  {
				int leftOrRight = 0;
				if (this.preNode != null) {
					// merge the rest of the entries to the left sibling
					for (int i = 0; i < this.entryList.size(); ++i) {
						this.preNode.insertEntry(this.entryList.get(i));
						this.entryList.remove(i);
					}
					leftOrRight = 1;
				} else if (this.nextNode != null) {
					// merge the rest of the entries to the right sibling
					for (int i = 0; i < this.entryList.size(); ++i) {
						this.nextNode.insertEntry(this.entryList.get(i));
						this.entryList.remove(i);

					}
					leftOrRight = 2;
				}

				// delete the empty leaf
				if (this.preNode != null) this.preNode.nextNode = this.nextNode;
				if (this.nextNode != null) this.nextNode.preNode = this.preNode;
				this.parent.getChildren().remove(this);
				// update the parent node, push through is needed
				this.parent.updateKeys();
				if (this.parent.getKeys().size() == 0) {
					if (leftOrRight == 1) return this.preNode;
					else if (leftOrRight == 2) return this.nextNode;
				}
				else if (this.parent.getParent() == null) return this.parent;

			}
		}
		return root;
	}

	public void setParent(InnerNode parent) {
		this.parent = parent;
	}

}