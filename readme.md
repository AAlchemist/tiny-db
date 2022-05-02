# Tiny DB
## Lab1
- Tuples: the basic building block of a table (row).
- TupleDesc: specifies a schema for a tuple (types and names of fields).
- Catalog: keeps track of what tables are currently in the database and provides a way to access them.
- HeapFiles: the physical representation of the data in our database (table). A HeapFile consists of many HeapPages.
- HeapPage: Manages the various tuples. A HeapPage consists of a header (bitmap) + some slots (tuples).
   - Each slot is assigned one bit in the header. 1: that slot is occupied; 0: that slot is empty and can have new data written to it.
   - #tuples_per_page = floor(8 * page_size / (8 * tuple_size + 1)).
   - #header_bytes = ceil(tuples_per_page / 8)

+ Adding a tuple: Find a page with an empty slot, or create a new empty heap page and pass the new tuple to it, then write the page to the disk (.dat file).
+ Deleting a tuple: get the page id and tuple id, then set the bit in header to 0 (logical deletion).