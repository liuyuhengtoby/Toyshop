package mypkg;

/**
 * The class CartItem models an item in the Cart.
 * This class shall not be accessed by the controlling logic directly.
 * Instead Use Cart.add() or Cart.remove() to add or remove an item from the
 * Cart.
 */
public class CartItem {

   private int id;
   private String title;
   private String category;
   private float price;
   private int qtyOrdered;
   private String img_url;

   // Constructor
   public CartItem(int id, String title, String author, float price, int qtyOrdered, String img_url) {
      this.id = id;
      this.title = title;
      this.category = author;
      this.price = price;
      this.qtyOrdered = qtyOrdered;
      this.img_url = img_url;
   }

   public int getId() {
      return id;
   }

   public String getCategory() {
      return category;
   }

   public String getTitle() {
      return title;
   }

   public float getPrice() {
      return price;
   }

   public int getQtyOrdered() {
      return qtyOrdered;
   }

   public String getImg_url() {
      return img_url;
   }

   public void setQtyOrdered(int qtyOrdered) {
      this.qtyOrdered = qtyOrdered;
   }

}