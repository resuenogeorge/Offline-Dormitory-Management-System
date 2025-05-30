/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */

/**
 *
 * @author Jemuel
 */
public class TestGson {
    public static void main(String[] args) {
        Gson gson = new Gson();
        String json = gson.toJson("Hello Bhie!");
        System.out.println(json);  // Should print: "Hello Bhie!"
    }
}

