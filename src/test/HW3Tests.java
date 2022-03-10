package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;
import hw1.StringField;
import hw3.BPlusTree;
import hw3.Entry;
import hw3.InnerNode;
import hw3.LeafNode;
import hw3.Node;

public class HW3Tests {

	@Test
	public void testSimpleInsert() {
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 1));
		assertTrue(bt.getRoot().isLeafNode());

		LeafNode l = (LeafNode)bt.getRoot();

		assertTrue(l.getEntries().get(0).getField().equals(new IntField(4)));
		assertTrue(l.getEntries().get(1).getField().equals(new IntField(9)));

		assertTrue(l.getEntries().get(0).getPage() == 1);
		assertTrue(l.getEntries().get(1).getPage() == 0);


	}

	@Test
	public void testComplexInsert() {

		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));

		//verify root properties
		Node root = bt.getRoot();

		assertTrue(root.isLeafNode() == false);

		InnerNode in = (InnerNode)root;

		ArrayList<Field> k = in.getKeys();
		ArrayList<Node> c = in.getChildren();

		assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(7)));

		//grab left and right children from root
		InnerNode l = (InnerNode)c.get(0);
		InnerNode r = (InnerNode)c.get(1);

		assertTrue(l.isLeafNode() == false);
		assertTrue(r.isLeafNode() == false);

		//check values in left node
		ArrayList<Field> kl = l.getKeys();
		ArrayList<Node> cl = l.getChildren();

		assertTrue(kl.get(0).compare(RelationalOperator.EQ, new IntField(2)));
		assertTrue(kl.get(1).compare(RelationalOperator.EQ, new IntField(4)));

		//get left node's children, verify
		Node ll = cl.get(0);
		Node lm = cl.get(1);
		Node lr = cl.get(2);

		assertTrue(ll.isLeafNode());
		assertTrue(lm.isLeafNode());
		assertTrue(lr.isLeafNode());

		LeafNode lll = (LeafNode)ll;
		LeafNode lml = (LeafNode)lm;
		LeafNode lrl = (LeafNode)lr;

		ArrayList<Entry> ell = lll.getEntries();

		assertTrue(ell.get(0).getField().equals(new IntField(1)));
		assertTrue(ell.get(1).getField().equals(new IntField(2)));

		ArrayList<Entry> elm = lml.getEntries();

		assertTrue(elm.get(0).getField().equals(new IntField(3)));
		assertTrue(elm.get(1).getField().equals(new IntField(4)));

		ArrayList<Entry> elr = lrl.getEntries();

		assertTrue(elr.get(0).getField().equals(new IntField(6)));
		assertTrue(elr.get(1).getField().equals(new IntField(7)));

		//verify right node
		ArrayList<Field> kr = r.getKeys();
		ArrayList<Node> cr = r.getChildren();

		assertTrue(kr.get(0).compare(RelationalOperator.EQ, new IntField(9)));

		//get right node's children, verify
		Node rl = cr.get(0);
		Node rr = cr.get(1);

		assertTrue(rl.isLeafNode());
		assertTrue(rr.isLeafNode());

		LeafNode rll = (LeafNode)rl;
		LeafNode rrl = (LeafNode)rr;

		ArrayList<Entry> erl = rll.getEntries();

		assertTrue(erl.get(0).getField().equals(new IntField(9)));

		ArrayList<Entry> err = rrl.getEntries();

		assertTrue(err.get(0).getField().equals(new IntField(10)));
		assertTrue(err.get(1).getField().equals(new IntField(12)));
	}

	@Test
	public void testSearch() {
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));

		//these values should exist
		assertTrue(bt.search(new IntField(12)) != null);
		assertTrue(bt.search(new IntField(3)) != null);
		assertTrue(bt.search(new IntField(7)) != null);

		//these values should not exist
		assertTrue(bt.search(new IntField(8)) == null);
		assertTrue(bt.search(new IntField(11)) == null);
		assertTrue(bt.search(new IntField(5)) == null);

	}
	
	@Test
	public void testHigherDegrees() {
		BPlusTree bt = new BPlusTree(4, 3);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));
		
		//verify root properties
				Node root = bt.getRoot();

				assertTrue(root.isLeafNode() == false);

				InnerNode in = (InnerNode)root;

				ArrayList<Field> k = in.getKeys();
				ArrayList<Node> c = in.getChildren();

				assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(2)));
				assertTrue(k.get(1).compare(RelationalOperator.EQ, new IntField(4)));
				assertTrue(k.get(2).compare(RelationalOperator.EQ, new IntField(7)));

				//grab left and right children from root
				LeafNode c0 = (LeafNode)c.get(0);
				LeafNode c1 = (LeafNode)c.get(1);
				LeafNode c2 = (LeafNode)c.get(2);
				LeafNode c3 = (LeafNode)c.get(3);
				assertTrue(c0.isLeafNode() == true);
				assertTrue(c1.isLeafNode() == true);
				assertTrue(c2.isLeafNode() == true);
				assertTrue(c3.isLeafNode() == true);

				

				//check values in left node
				ArrayList<Entry> c0entries = c0.getEntries();

				assertTrue(c0entries.get(0).getField().equals(new IntField(1)));
				assertTrue(c0entries.get(1).getField().equals(new IntField(2)));

				//check values in second node
				ArrayList<Entry> c1entries = c1.getEntries();

				assertTrue(c1entries.get(0).getField().equals(new IntField(3)));
				assertTrue(c1entries.get(1).getField().equals(new IntField(4)));
				
				//check values in third node
				ArrayList<Entry> c2entries = c2.getEntries();

				assertTrue(c2entries.get(0).getField().equals(new IntField(6)));
				assertTrue(c2entries.get(1).getField().equals(new IntField(7)));
				
				//check values in right most node
				ArrayList<Entry> c3entries = c3.getEntries();

				assertTrue(c3entries.get(0).getField().equals(new IntField(9)));
				assertTrue(c3entries.get(1).getField().equals(new IntField(10)));
				assertTrue(c3entries.get(2).getField().equals(new IntField(12)));



	}

	@Test
	public void testDelete() {
		//Create a tree, then delete some values

		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));

		bt.delete(new Entry(new IntField(7), 0));
		bt.delete(new Entry(new IntField(3), 0));
		bt.delete(new Entry(new IntField(4), 0));
		bt.delete(new Entry(new IntField(10), 0));
		bt.delete(new Entry(new IntField(2), 0));

		//verify root properties
		Node root = bt.getRoot();

		assertTrue(root.isLeafNode() == false);

		InnerNode in = (InnerNode)root;

		ArrayList<Field> k = in.getKeys();
		ArrayList<Node> c = in.getChildren();

		assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(7)));

		//grab left and right children from root
		InnerNode l = (InnerNode)c.get(0);
		InnerNode r = (InnerNode)c.get(1);

		assertTrue(l.isLeafNode() == false);
		assertTrue(r.isLeafNode() == false);

		//check values in left node
		ArrayList<Field> kl = l.getKeys();
		ArrayList<Node> cl = l.getChildren();

		assertTrue(kl.get(0).compare(RelationalOperator.EQ, new IntField(1)) || kl.get(0).compare(RelationalOperator.EQ, new IntField(4)));

		//get left node's children, verify
		Node ll = cl.get(0);
		Node lr = cl.get(1);

		assertTrue(ll.isLeafNode());
		assertTrue(lr.isLeafNode());

		LeafNode lll = (LeafNode)ll;
		LeafNode lrl = (LeafNode)lr;

		ArrayList<Entry> ell = lll.getEntries();

		assertTrue(ell.get(0).getField().equals(new IntField(1)));

		ArrayList<Entry> elr = lrl.getEntries();

		assertTrue(elr.get(0).getField().equals(new IntField(6)));

		//verify right node
		ArrayList<Field> kr = r.getKeys();
		ArrayList<Node> cr = r.getChildren();

		assertTrue(kr.get(0).compare(RelationalOperator.EQ, new IntField(9)));

		//get right node's children, verify
		Node rl = cr.get(0);
		Node rr = cr.get(1);

		assertTrue(rl.isLeafNode());
		assertTrue(rr.isLeafNode());

		LeafNode rll = (LeafNode)rl;
		LeafNode rrl = (LeafNode)rr;

		ArrayList<Entry> erl = rll.getEntries();

		assertTrue(erl.get(0).getField().equals(new IntField(9)));

		ArrayList<Entry> err = rrl.getEntries();

		assertTrue(err.get(0).getField().equals(new IntField(12)));
	}
	
	@Test
	public void testDeleteLevelToRoot() {
		// assuming insertion works perfectly
		
		// inserting 4 values so that the tree has two nodes
		BPlusTree bt = new BPlusTree(4, 3);
		bt.insert(new Entry(new IntField(15), 0));
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(1), 0));
		
	
		bt.delete(new Entry(new IntField(9), 0));

		
		assertTrue(bt.getRoot().isLeafNode());
		LeafNode l = (LeafNode) bt.getRoot();

		assertTrue(l.getEntries().get(0).getField().equals(new IntField(1)));
		assertTrue(l.getEntries().get(1).getField().equals(new IntField(4)));
		assertTrue(l.getEntries().get(2).getField().equals(new IntField(15)));

	}
	
	@Test
	public void testDeleteMergeNodes() {
		// assuming insertion works perfectly
		
		BPlusTree bt = new BPlusTree(4, 3);
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(5), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(1), 0));
	
		bt.delete(new Entry(new IntField(1), 0));

		// Tree should look like:
		//	(4,null, null)
		//   /		\
		// (2,3,4)	(5,6,null)
		
		// root should not be leaf node
		assertFalse(bt.getRoot().isLeafNode());
		InnerNode l = (InnerNode) bt.getRoot();

		// 4 is max of left child, so that should be the only key, the other two should be null
		assertTrue(l.getKeys().get(0).equals(new IntField(4)));

		// left and right children should be leaf nodes
		LeafNode leftChild = (LeafNode) l.getChildren().get(0);
		LeafNode rightChild = (LeafNode) l.getChildren().get(1);
		
		assertTrue(leftChild.isLeafNode());
		assertTrue(rightChild.isLeafNode());
		
		// left child should contain (2,3,4)
		assertTrue(leftChild.getEntries().get(0).getField().equals(new IntField(2)));
		assertTrue(leftChild.getEntries().get(1).getField().equals(new IntField(3)));
		assertTrue(leftChild.getEntries().get(2).getField().equals(new IntField(4)));

		// right child should contain (5,6, null)
		assertTrue(rightChild.getEntries().get(0).getField().equals(new IntField(5)));
		assertTrue(rightChild.getEntries().get(1).getField().equals(new IntField(6)));

	}
	
	@Test
	public void testInsertCreateNewRoot(){
		BPlusTree bt = new BPlusTree(3, 2);
		
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(8), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(9), 0));
		
		//Check tree before root split
		assertTrue(!bt.getRoot().isLeafNode());
		
		InnerNode in = (InnerNode)bt.getRoot();
		
		ArrayList<Field> k = in.getKeys();
		ArrayList<Node> c = in.getChildren();
		
		assertTrue(k.size() == 2);
		assertTrue(c.size() == 3);
		
		assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(7)));
		assertTrue(k.get(1).compare(RelationalOperator.EQ, new IntField(8)));
		
		assertTrue(c.get(0).isLeafNode());
		assertTrue(c.get(1).isLeafNode());
		assertTrue(c.get(2).isLeafNode());
		
		LeafNode l = (LeafNode)c.get(0);
		LeafNode m = (LeafNode)c.get(1);
		LeafNode r = (LeafNode)c.get(2);
		
		ArrayList<Entry> cl = l.getEntries();
		assertTrue(cl.get(0).getField().equals(new IntField(1)));
		assertTrue(cl.get(1).getField().equals(new IntField(7)));
		
		ArrayList<Entry> cm = m.getEntries();
		assertTrue(cm.get(0).getField().equals(new IntField(8)));
		
		ArrayList<Entry> cr = r.getEntries();
		assertTrue(cr.get(0).getField().equals(new IntField(9)));
		assertTrue(cr.get(1).getField().equals(new IntField(12)));
		
		
		//Insert to cause a root split 
		bt.insert(new Entry(new IntField(2), 0));
		
		//Check tree after root split
		assertTrue(!bt.getRoot().isLeafNode());
		
		InnerNode in2 = (InnerNode)bt.getRoot(); 
		
		ArrayList<Field> k2 = in2.getKeys();
		ArrayList<Node> c2 = in2.getChildren();
		
		assertTrue(k2.size() == 1);
		assertTrue(c2.size() == 2);
		
		assertTrue(k2.get(0).compare(RelationalOperator.EQ, new IntField(7))); 
		
		assertTrue(!c2.get(0).isLeafNode());
		assertTrue(!c2.get(1).isLeafNode());
		
		//Check Left Subtree
		InnerNode l2 = (InnerNode)c2.get(0);
		ArrayList<Field> lk = l2.getKeys();
		ArrayList<Node> lc = l2.getChildren();
		
		assertTrue(lk.size() == 1);
		assertTrue(lc.size() == 2);
		
		assertTrue(lk.get(0).compare(RelationalOperator.EQ, new IntField(2))); 
		
		assertTrue(lc.get(0).isLeafNode());
		assertTrue(lc.get(1).isLeafNode());
		
		LeafNode lcl = (LeafNode) lc.get(0);
		LeafNode lcr = (LeafNode) lc.get(1);
		
		ArrayList<Entry> lclE = lcl.getEntries();
		assertTrue(lclE.get(0).getField().equals(new IntField(1)));
		assertTrue(lclE.get(1).getField().equals(new IntField(2)));

		ArrayList<Entry> lcrE = lcr.getEntries();
		assertTrue(lcrE.get(0).getField().equals(new IntField(7)));
		
		//Check Right Subtree
		InnerNode r2 = (InnerNode)c2.get(1);
		ArrayList<Field> rk = r2.getKeys();
		ArrayList<Node> rc = r2.getChildren();
		
		assertTrue(rk.size() == 1);
		assertTrue(rc.size() == 2);
		
		assertTrue(rk.get(0).compare(RelationalOperator.EQ, new IntField(8))); 
		
		assertTrue(rc.get(0).isLeafNode());
		assertTrue(rc.get(1).isLeafNode());

		LeafNode rcl = (LeafNode) rc.get(0);
		LeafNode rcr = (LeafNode) rc.get(1);
		
		ArrayList<Entry> rclE = rcl.getEntries();
		assertTrue(rclE.get(0).getField().equals(new IntField(8)));
		
		ArrayList<Entry> rcrE = rcr.getEntries();
		assertTrue(rcrE.get(0).getField().equals(new IntField(9)));
		assertTrue(rcrE.get(1).getField().equals(new IntField(12)));
	}
	
	@Test
	public void deletionDirectly() {

		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(4, 3);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		// delete 7
		// delete directly
		bt.delete(new Entry(new IntField(7), 0));
				
		//verify root properties
		Node root = bt.getRoot();
		assertTrue(root.isLeafNode() == false);
		InnerNode in = (InnerNode)root;
		ArrayList<Field> k = in.getKeys();
		IntField searchKey = (IntField)k.get(0);
		assertTrue(searchKey.equals(new IntField(7)) || searchKey.equals(new IntField(4)));
		ArrayList<Node> c = in.getChildren();
		
		LeafNode c0 = (LeafNode)c.get(0);
		assertTrue(c0.isLeafNode() == true);

		//check values
		ArrayList<Entry> c0entries = c0.getEntries();
		assertTrue(c0entries.get(0).getField().equals(new IntField(2)));
		assertTrue(c0entries.get(1).getField().equals(new IntField(4)));
		
		LeafNode c1 = (LeafNode)c.get(1);
		assertTrue(c1.isLeafNode() == true);

		//check values
		ArrayList<Entry> c1entries = c1.getEntries();
		assertTrue(c1entries.get(0).getField().equals(new IntField(9)));
		assertTrue(c1entries.get(1).getField().equals(new IntField(12)));
	}
	
	@Test
	// Deletion
	// Borrow and update root
	public void deletionborrow() {
		
		//create a tree, insert a bunch of values
		BPlusTree bt = new BPlusTree(4, 3);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		
		// delete 9
		// borrow from left
		bt.delete(new Entry(new IntField(9), 0));
				
		//verify root properties
		Node root = bt.getRoot();
		assertTrue(root.isLeafNode() == false);
		InnerNode in = (InnerNode)root;
		ArrayList<Field> k = in.getKeys();
		ArrayList<Node> c = in.getChildren();
		
		LeafNode c0 = (LeafNode)c.get(0);
		assertTrue(c0.isLeafNode() == true);
		LeafNode c1 = (LeafNode)c.get(1);
		assertTrue(c1.isLeafNode() == true);
		
		//check root
		//need to be update
		assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(4)));

		//check values
		ArrayList<Entry> c0entries = c0.getEntries();
		assertTrue(c0entries.get(0).getField().equals(new IntField(2)));
		assertTrue(c0entries.get(1).getField().equals(new IntField(4)));
		ArrayList<Entry> c1entries = c1.getEntries();
		assertTrue(c1entries.get(0).getField().equals(new IntField(7)));
		assertTrue(c1entries.get(1).getField().equals(new IntField(12)));
		
	}
	
	@Test
	public void testSimpleLeafSplit() {
		// create a new B+ tree
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(5), 0));
		bt.insert(new Entry(new IntField(2), 0));
		
		// ensure root node is not leaf
		Node root = bt.getRoot();
		assertTrue(root.isLeafNode() == false);

		// get root keys and children
		InnerNode in = (InnerNode) root;
		ArrayList<Field> k = in.getKeys();
		ArrayList<Node> c = in.getChildren();
		
		// make sure root has both 3 and 4 after shift
		assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(3)));
		assertTrue(k.get(1).compare(RelationalOperator.EQ, new IntField(4)));

		// grab left and right children from root
		LeafNode l = (LeafNode) c.get(0);
		LeafNode m = (LeafNode) c.get(1);
		LeafNode r = (LeafNode) c.get(2);

		assertTrue(l.isLeafNode() == true);
		assertTrue(m.isLeafNode() == true);
		assertTrue(r.isLeafNode() == true);
		
		// make sure split kept larger left leaf and has appropriate equality rule
		assertTrue(l.getEntries().get(0).getField().equals(new IntField(2)));
		assertTrue(l.getEntries().get(1).getField().equals(new IntField(3)));
		assertTrue(m.getEntries().get(0).getField().equals(new IntField(4)));
		assertTrue(r.getEntries().get(0).getField().equals(new IntField(5)));
	}
	
	@Test
	public void testRemoveLevel() {
		//instantiate a tree with pInner = 3, pLeaf = 2
		int pInner = 3;
		int pLeaf = 2;
		BPlusTree bt = new BPlusTree(pInner, pLeaf);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		//							[7, ]
		// 						   /     \
		//                        /       \
		//                     [4, ]     [9, ]
		//                     /   \     /   \
		//                 [2,4] [7, ] [9, ] [12, ]    
		//delete 9
		//                     [4,7]
		//                    /  |  \
		//                   /   |   \
		//               [2,4] [7, ] [12, ]
		bt.delete(new Entry(new IntField(9), 0));
		Node root = bt.getRoot();
		assertTrue(root.isLeafNode() == false);
		ArrayList<Field> root_keys = ((InnerNode)root).getKeys();
		assertTrue(root_keys.size() == 2);
		assertTrue(root_keys.get(0).compare(RelationalOperator.EQ, new IntField(4)));
		assertTrue(root_keys.get(1).compare(RelationalOperator.EQ, new IntField(7)));
		ArrayList<Node> root_c = ((InnerNode)root).getChildren();
		assertTrue(root_c.size() == 3);
		Node l = root_c.get(0);
		Node m = root_c.get(1);
		Node r = root_c.get(2);
		assertTrue(l.isLeafNode() == true);
		assertTrue(m.isLeafNode() == true);
		assertTrue(r.isLeafNode() == true);
		ArrayList<Entry> le = ((LeafNode)l).getEntries();
		ArrayList<Entry> me = ((LeafNode)m).getEntries();
		ArrayList<Entry> re = ((LeafNode)r).getEntries();
		assertTrue(le.size() == 2);
		assertTrue(le.get(0).getField().compare(RelationalOperator.EQ, new IntField(2)));
		assertTrue(le.get(1).getField().compare(RelationalOperator.EQ, new IntField(4)));
		assertTrue(me.size() == 1);
		assertTrue(me.get(0).getField().compare(RelationalOperator.EQ, new IntField(7)));
		assertTrue(re.size() == 1);
		assertTrue(re.get(0).getField().compare(RelationalOperator.EQ, new IntField(12)));
	}
	
	@Test
	public void testPushThrough() {
		//instantiate a tree with pInner = 3, pLeaf = 2
		int pInner = 3;
		int pLeaf = 2;
		BPlusTree bt = new BPlusTree(pInner, pLeaf);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		//                           [7, ]
		//                         /       \
		//                       /           \
		//                     /               \
		//                 [2,4]               [9, ]
		//                /  |  \             /     \
		//               /   |   \           /       \
		//           [1,2] [3,4] [6,7]     [9, ]    [12, ]
		//delete 12
		bt.delete(new Entry(new IntField(12), 0));
		//                           [4, ]
		//                         /       \
		//                       /           \
		//                     /               \
		//                 [2, ]               [7, ]
		//                /     \             /     \
		//               /       \           /       \
		//           [1,2]       [3,4]     [6,7]    [9, ]
		assertTrue(bt.getRoot().isLeafNode() == false);
		InnerNode root = (InnerNode)bt.getRoot();
		assertTrue(root.getKeys().get(0).compare(RelationalOperator.EQ, new IntField(4)));
		
		ArrayList<Node> root_c = root.getChildren();
		assertTrue(root_c.get(0).isLeafNode() == false);
		assertTrue(root_c.get(1).isLeafNode() == false);
		InnerNode l = (InnerNode)root_c.get(0);
		InnerNode r = (InnerNode)root_c.get(1);
		assertTrue(l.getKeys().get(0).compare(RelationalOperator.EQ, new IntField(2)));
		assertTrue(r.getKeys().get(0).compare(RelationalOperator.EQ, new IntField(7)));
		
		ArrayList<Node> lc = l.getChildren();
		assertTrue(lc.get(0).isLeafNode() == true);
		assertTrue(lc.get(1).isLeafNode() == true);
		ArrayList<Node> rc = r.getChildren();
		assertTrue(rc.get(0).isLeafNode() == true);
		assertTrue(rc.get(1).isLeafNode() == true);
		LeafNode ll = (LeafNode)lc.get(0);
		LeafNode lr = (LeafNode)lc.get(1);
		LeafNode rl = (LeafNode)rc.get(0);
		LeafNode rr = (LeafNode)rc.get(1);
		assertTrue(ll.getEntries().get(0).getField().compare(RelationalOperator.EQ, new IntField(1)));
		assertTrue(ll.getEntries().get(1).getField().compare(RelationalOperator.EQ, new IntField(2)));
		assertTrue(lr.getEntries().get(0).getField().compare(RelationalOperator.EQ, new IntField(3)));
		assertTrue(lr.getEntries().get(1).getField().compare(RelationalOperator.EQ, new IntField(4)));
		assertTrue(rl.getEntries().get(0).getField().compare(RelationalOperator.EQ, new IntField(6)));
		assertTrue(rl.getEntries().get(1).getField().compare(RelationalOperator.EQ, new IntField(7)));
		assertTrue(rr.getEntries().get(0).getField().compare(RelationalOperator.EQ, new IntField(9)));
	}
	
	@Test
	public void testSearchOnlyRootExists() {
		// create a tree
		BPlusTree bt = new BPlusTree(3, 2);

		// insert one value (i.e., root)
		Field f = new IntField(9);
		Entry entry = new Entry(f, 0);
		bt.insert(entry);
		
		// search for value
		LeafNode searchResult = bt.search(f);

		// search should return a leafNode
		assertTrue(searchResult != null);

		// returned leafNode should contain the searched for entry
		assertTrue(searchResult.getEntries().contains(entry));
	}
	
	@Test
	public void testDeleteRoot() {
		// create a tree
		BPlusTree bt = new BPlusTree(3, 2);

		// insert one value (i.e., root)
		Entry entry = new Entry(new IntField(9), 0);
		bt.insert(entry);

		// delete the root
		bt.delete(entry);

		// tree should be empty
		assertTrue(bt.getRoot() == null);
	}
	
	@Test 
	public void stringFieldInsert() {
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new StringField("test"), 0));
		bt.insert(new Entry(new StringField("database"), 1));
		assertTrue(bt.getRoot().isLeafNode());

		LeafNode l = (LeafNode)bt.getRoot();

		assertTrue(l.getEntries().get(0).getField().equals(new StringField("database")));
		assertTrue(l.getEntries().get(1).getField().equals(new StringField("test")));

		assertTrue(l.getEntries().get(0).getPage() == 1);
		assertTrue(l.getEntries().get(1).getPage() == 0);
	}
	
	@Test
	public void testStringSearch() {
		BPlusTree bpt = new BPlusTree(3, 2);
		bpt.insert(new Entry(new StringField("a"), 0));
		bpt.insert(new Entry(new StringField("g"), 0));
		bpt.insert(new Entry(new StringField("z"), 0));
		bpt.insert(new Entry(new StringField("w"), 0));
		bpt.insert(new Entry(new StringField("t"), 0));
		bpt.insert(new Entry(new StringField("m"), 0));
		
		assertTrue(bpt.search(new StringField("a")) != null);
		assertTrue(bpt.search(new StringField("w")) != null);
		assertTrue(bpt.search(new StringField("m")) != null);
		
		assertFalse(bpt.search(new StringField("b")) != null);
		assertFalse(bpt.search(new StringField("d")) != null);
		assertFalse(bpt.search(new StringField("s")) != null);
	}
	
	@Test
	/*
	 * test HigherDegrees deletion for int.
	 * Only involves merge.
	 */
	public void testHigherDegreesDelete() {
		BPlusTree bt = new BPlusTree(4, 3);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(2), 0));
		bt.insert(new Entry(new IntField(6), 0));
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(3), 0));
		bt.insert(new Entry(new IntField(10), 0));
		
		bt.delete(new Entry(new IntField(4), 0));
		bt.delete(new Entry(new IntField(6), 0));	// That is where merge should happen
		
		Node root = bt.getRoot();
		assertTrue(root.isLeafNode() == false);
		
		InnerNode in = (InnerNode)root;
		ArrayList<Field> k = in.getKeys();
		ArrayList<Node> c = in.getChildren();
		
		assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(2)) || k.get(0).compare(RelationalOperator.EQ, new IntField(3)));
		assertTrue(k.get(1).compare(RelationalOperator.EQ, new IntField(7)));
		assertTrue(k.size() == 2);
		
		LeafNode c0 = (LeafNode)c.get(0);
		LeafNode c1 = (LeafNode)c.get(1);
		LeafNode c2 = (LeafNode)c.get(2);
		assertTrue(c0.isLeafNode());
		assertTrue(c1.isLeafNode());
		assertTrue(c2.isLeafNode());
		
		//check values in left node
		ArrayList<Entry> c0Entities = c0.getEntries();
		assertTrue(c0Entities.get(0).getField().compare(RelationalOperator.EQ, new IntField(1)));
		assertTrue(c0Entities.get(1).getField().compare(RelationalOperator.EQ, new IntField(2)));
		
		ArrayList<Entry> c1Entities = c1.getEntries();
		assertTrue(c1Entities.get(0).getField().compare(RelationalOperator.EQ, new IntField(3)));
		assertTrue(c1Entities.get(1).getField().compare(RelationalOperator.EQ, new IntField(7)));
		
		//check values in right node
		ArrayList<Entry> c2Entities = c2.getEntries();
		assertTrue(c2Entities.get(0).getField().compare(RelationalOperator.EQ, new IntField(9)));
		assertTrue(c2Entities.get(1).getField().compare(RelationalOperator.EQ, new IntField(10)));
		assertTrue(c2Entities.get(2).getField().compare(RelationalOperator.EQ, new IntField(12)));

	}

}
