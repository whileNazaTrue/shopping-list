package com.example.shoppinglist;

import static listElements.itemCategory.*;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import java.util.Comparator;

import listElements.Item;

public class MainActivity extends AppCompatActivity implements Serializable {
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private AlertDialog.Builder dialogClearBuilder;
    private AlertDialog dialogClear;
    private EditText modifyName, modifyCategory;
    private Button setName, setCategory, deleteItem, clearList, confirmClear;
    private ListView elementsList;
    private ArrayList<Item> items;
    private ArrayAdapter<Item> adapter;
    private EditText inputName;
    private EditText inputCategory;
    private Button enterButton;
    private ImageView infoButton;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputName = findViewById(R.id.enterName);
        inputCategory = findViewById(R.id.enterCategory);
        enterButton = findViewById(R.id.createItemButton);
        infoButton = findViewById(R.id.infoButton);
        clearList = findViewById(R.id.clearList);
        confirmClear = findViewById(R.id.confirmClearButton);

        items = new ArrayList<>();
        loadContent();


        //HARDCODED DEFAULTS
        loadList();
        items.sort(Comparator.comparing(Item::getCategory).thenComparing(Item::getName));




        elementsList = findViewById(R.id.itemList);
        //simple_list_item_1,items
        //layout.simple_list_item_multiple_choice
        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice,items);
        elementsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        elementsList.setAdapter(adapter);
        for (int i = 0; i < items.size(); i++) {
            loadPreferences(i);
        }



        elementsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                SharedPreferences pref = getSharedPreferences("credentials",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean(items.get(i).getName(),elementsList.isItemChecked(i));
                editor.apply();
            }
        });


        elementsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                //dialog.setCancelable(false);
                final View contactPopupView = getLayoutInflater().inflate(R.layout.dialog_modify_item,null);
                modifyName = (EditText) contactPopupView.findViewById(R.id.changeItemNameInput);
                modifyCategory = (EditText) contactPopupView.findViewById(R.id.changeCategoryInput);

                setName = (Button) contactPopupView.findViewById(R.id.buttonSetNewName);
                setCategory = (Button) contactPopupView.findViewById(R.id.buttonSetNewCategory);
                deleteItem = (Button) contactPopupView.findViewById(R.id.buttonDeleteItem);

                dialogBuilder.setView(contactPopupView);
                dialog = dialogBuilder.create();
                dialog.show();

                setName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String renameName = modifyName.getText().toString().toUpperCase();
                        if (renameName == null || renameName.length() == 0){
                            Toast.makeText(MainActivity.this, "The name can't be blank", Toast.LENGTH_SHORT).show();
                        }
                        else if (checkIfExists(renameName)){
                            Toast.makeText(MainActivity.this, "Name already exists!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            items.get(i).setName(renameName);

                            Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            items.sort(Comparator.comparing(Item::getCategory).thenComparing(Item::getName));
                        }
                    }
                });
                setCategory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newCategoryName = modifyCategory.getText().toString().toUpperCase();
                        if (newCategoryName.equals("")){
                            newCategoryName = "*empty";
                        }
                        items.get(i).setCategory(newCategoryName);

                        Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        items.sort(Comparator.comparing(Item::getCategory).thenComparing(Item::getName));
                    }

                });
                deleteItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(MainActivity.this, "Removing "+items.get(i).getName(), Toast.LENGTH_SHORT).show();

                        items.remove(i);
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();

                        items.sort(Comparator.comparing(Item::getCategory).thenComparing(Item::getName));
                    }
                });

                return false;

            }
        });




        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String itemName = inputName.getText().toString().toUpperCase();
                String itemCategory = inputCategory.getText().toString().toUpperCase();
                if (itemName == null || itemName.length() == 0){
                    Toast.makeText(MainActivity.this, "Name can't be empty valid!", Toast.LENGTH_SHORT).show();
                }
                else if (checkIfExists(itemName)){
                    Toast.makeText(MainActivity.this, "Name already exists!", Toast.LENGTH_SHORT).show();
                }
                else{
                    if (inputCategory.getText().toString().equals("")) {
                        itemCategory = "*empty*";
                    }
                    Toast.makeText(MainActivity.this, "Added " + itemName, Toast.LENGTH_SHORT).show();
                    loadElement(itemName,itemCategory);

                }
            }
        });

        clearList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogClearBuilder = new AlertDialog.Builder(MainActivity.this);
                final View popupView = getLayoutInflater().inflate(R.layout.clear_all_dialog,null);
                confirmClear = popupView.findViewById(R.id.confirmClearButton);

                dialogClearBuilder.setView(popupView);
                dialogClear = dialogClearBuilder.create();
                dialogClear.show();

                confirmClear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        items.clear();
                        adapter.notifyDataSetChanged();
                        dialogClear.dismiss();
                        Toast.makeText(MainActivity.this, "Cleared successfully", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, infoActivity.class);
                ArrayList<String> aux = new ArrayList<>();
                loadArrayListOfString(aux,items);
                intent.putExtra("ARRAYLIST_SENDER",aux);
                startActivity(intent);
            }
        });

    }

    void loadArrayListOfString(ArrayList<String> strings, ArrayList<Item> items){
        for (int i = 0; i < items.size(); i++) {
            if (strings.contains(items.get(i).getCategory())==false){
                strings.add(items.get(i).getCategory());


            }
        }
    }

    public void loadPreferences(int i){
        SharedPreferences preferences = getSharedPreferences("credentials",Context.MODE_PRIVATE);
        Boolean aux = preferences.getBoolean(items.get(i).getName(),elementsList.isItemChecked(i));

        elementsList.setItemChecked(i,aux);
    }

    private boolean checkIfExists(String name){
        for (int i = 0; i < items.size(); i++) {
            if (name.equals(items.get(i).getName())){

                return true;
            }
            System.out.println(items.get(i).getName());
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadElement(String name, String category){
        Item item = new Item(name,category);
        items.add(item);
        items.sort(Comparator.comparing(Item::getCategory).thenComparing(Item::getName));
        elementsList.setAdapter(adapter);
        for (int i = 0; i < items.size(); i++) {
            loadPreferences(i);
        }


    }




    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadList(){
        if (items.isEmpty() || items.equals(null)){
            items.add(new Item("CANNED CORN",CANNED));
            items.add(new Item("CANNED PEAS",CANNED));
            items.add(new Item("CANNED TOMATOES",CANNED));
            items.add(new Item("FOOD DETERGENT",CLEANING));
            items.add(new Item("LAUNDRY DETERGENT",CLEANING));
            items.add(new Item("SOAP",CLEANING));
            items.add(new Item("BLEACH",CLEANING));
            items.add(new Item("CHIMICHURRI",CONDIMENTS_OR_SPICES));
            items.add(new Item("SALT",CONDIMENTS_OR_SPICES));
            items.add(new Item("PEPPER",CONDIMENTS_OR_SPICES));
            items.add(new Item("NUTMEG",CONDIMENTS_OR_SPICES));
            items.add(new Item("MUSTARD",CONDIMENTS_OR_SPICES));
            items.add(new Item("BBQ SAUCE",CONDIMENTS_OR_SPICES));
            items.add(new Item("KETCHUP",CONDIMENTS_OR_SPICES));
            items.add(new Item("MAYONNAISE",CONDIMENTS_OR_SPICES));
            items.add(new Item("MILK",DAIRY));
            items.add(new Item("CHEESE",DAIRY));
            items.add(new Item("CREAM",DAIRY));
            items.add(new Item("GRATED CHEESE",DAIRY));
            items.add(new Item("YOGURT",DAIRY));
            items.add(new Item("BUTTER",DAIRY));
            items.add(new Item("ICE CREAm",FROZEN));
            items.add(new Item("SOY MILANESA",FROZEN));
            items.add(new Item("APPLE",FRUIT));
            items.add(new Item("BANANA",FRUIT));
            items.add(new Item("ORANGE",FRUIT));
            items.add(new Item("TANGERINE",FRUIT));
            items.add(new Item("GRAPEFRUIT",FRUIT));
            items.add(new Item("LEMON",FRUIT));
            items.add(new Item("GRAPES",FRUIT));
            items.add(new Item("CHERRIES",FRUIT));
            items.add(new Item("AVOCADO",FRUIT));
            items.add(new Item("PEAR",FRUIT));
            items.add(new Item("CHOCOLATES",GROCERY));
            items.add(new Item("BUBBLE GUM",GROCERY));
            items.add(new Item("CEREAL",GROCERY));
            items.add(new Item("COOKIES",GROCERY));
            items.add(new Item("OIL",GROCERY));
            items.add(new Item("COFFEE",GROCERY));
            items.add(new Item("YERBA",GROCERY));
            items.add(new Item("SLICED HAM",GROCERY));
            items.add(new Item("SLICED CHEESE",GROCERY));
            items.add(new Item("SLICED BACON",GROCERY));
            items.add(new Item("RICE",GROCERY));
            items.add(new Item("NOODLES",GROCERY));
            items.add(new Item("COCOA POWDER",GROCERY));
            items.add(new Item("EGGS",GROCERY));
            items.add(new Item("GROUND BEEF",MEAT));
            items.add(new Item("FISH",MEAT));
            items.add(new Item("PORK",MEAT));
            items.add(new Item("SHRIMP",MEAT));
            items.add(new Item("CHICKEN",MEAT));
            items.add(new Item("SAUSAGE",MEAT));
            items.add(new Item("EYE OF ROUND",MEAT));
            items.add(new Item("RIB",MEAT));
            items.add(new Item("DEODORANT",PERFUMERY));
            items.add(new Item("ANTIPERSPIRANT",PERFUMERY));
            items.add(new Item("SHAMPOO",PERFUMERY));
            items.add(new Item("PICKLES",PICKLES));
            items.add(new Item("OLIVES",PICKLES));
            items.add(new Item("NACHOS",SNACKS));
            items.add(new Item("CHEESE PUFFS",SNACKS));
            items.add(new Item("POTATO CHIPS",SNACKS));
            items.add(new Item("POTATO",VEGETABLE));
            items.add(new Item("TOMATO",VEGETABLE));
            items.add(new Item("ONION",VEGETABLE));
            items.add(new Item("GREEN ONION",VEGETABLE));
            items.add(new Item("CORN",VEGETABLE));
            items.add(new Item("CAULIFLOWER",VEGETABLE));
            items.add(new Item("MUSHROOMS",VEGETABLE));
            items.add(new Item("CABBAGE",VEGETABLE));
            items.add(new Item("ARUGULA",VEGETABLE));
            items.add(new Item("CUCUMBER",VEGETABLE));
            items.add(new Item("ZUCCHINI",VEGETABLE));
            items.add(new Item("LETTUCE",VEGETABLE));
            items.add(new Item("BROCCOLI",VEGETABLE));
            items.add(new Item("BELL PEPPER",VEGETABLE));
            items.add(new Item("BEETROOT",VEGETABLE));
            items.add(new Item("CARROT",VEGETABLE));
            items.add(new Item("EGGPLANT",VEGETABLE));
            items.add(new Item("CELERY",VEGETABLE));

            items.sort(Comparator.comparing(Item::getCategory).thenComparing(Item::getName));
        }
    }

    public void loadContent(){

        File path = getApplicationContext().getFilesDir();
        File readFile = new File(path,"output.txt");
        //byte[] content = new byte[(int)readFile.length()];

        try {

            FileInputStream readData = new FileInputStream(readFile);
            ObjectInputStream readStream = new ObjectInputStream(readData);
            ArrayList<Item> aux = (ArrayList<Item>) readStream.readObject();
            readStream.close();
            items = aux;

            adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_multiple_choice,items);

            elementsList.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    protected void onStop () {

        String path = getApplicationContext().getFilesDir().toString() + "/output.txt";
        File f = new File(path);
        try {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(items);
            oos.flush();
            oos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        super.onStop();

    }

}