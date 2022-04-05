package listElements;

import java.io.Serializable;

public class Item implements Serializable {
    private String name;
    private String category;

    public Item(String name, String category){
        this.name = name;
        this.category = category;

    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }



    @Override
    public String toString() {
        return category.substring(0,Math.min(category.length(),3)) + " - " + name;

    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
