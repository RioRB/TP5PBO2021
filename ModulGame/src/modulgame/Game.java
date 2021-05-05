/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulgame;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.Random;

/**
 *
 * @author Fauzan
 */
public class Game extends Canvas implements Runnable{
    Window window;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    
    private int score = 0; // penghitung score
    
    private int time = 10;
    
    private int timeScore = 0; // penghitung time play player
    
    private String nama;
    
    private Thread thread;
    private boolean running = false;
    
    private Handler handler;
    
    Clip clip;
    AudioInputStream audioIn;
    
    public enum STATE{
        Game,
        GameOver
    };
    
    public STATE gameState = STATE.Game;
    
    public Game(String username){
        window = new Window(WIDTH, HEIGHT, "TP5 DPBO", this);
        
        handler = new Handler();
        
        this.addKeyListener(new KeyInput(handler, this));
        
        this.nama = username;
        
        if(gameState == STATE.Game){
            handler.addObject(new Items(100,150, ID.Item));
            handler.addObject(new Items(200,350, ID.Item));
            handler.addObject(new Player(200,200, ID.Player));
            handler.addObject(new Bot(500,400, ID.Bot));
        }
    }

    public synchronized void start(){
        thread = new Thread(this);
        thread.start();
        running = true;
    }
    
    public synchronized void stop(){
        try{
            thread.join();
            running = false;
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        
        // play BGM song
        playSound("/party.wav", "BGM");
        
        while(running){
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            while(delta >= 1){
                tick();
                delta--;
            }
            if(running){
                render();
                frames++;
            }
            
            if(System.currentTimeMillis() - timer > 1000){
                timer += 1000;
                timeScore++;
                //System.out.println("FPS: " + frames);
                frames = 0;
                if(gameState == STATE.Game){
                    
                    if(time>0){
                        time--;
                        
                        // Pembuatan movement bot (musuh) menjadi random
                        for(int i=0;i< handler.object.size(); i++){
                            if(handler.object.get(i).getId() == ID.Bot){
                                // apabila menyentuh batas sisi game
                                if(handler.object.get(i).getX() <= 100){
                                    //nggk boleh ke kanan
                                    handler.object.get(i).setVel_x(+5);
                                }
                                else if(handler.object.get(i).getX() <= 600){
                                    handler.object.get(i).setVel_x(-5);
                                }
                                else if(handler.object.get(i).getY() <= 100){
                                    handler.object.get(i).setVel_y(+5);
                                }
                                else if(handler.object.get(i).getY() <= 400){
                                    handler.object.get(i).setVel_y(-5);
                                }
                                
                                // memulai pergerakan random berdasarkan angka yg didapat
                                Random r = new Random();
                                int randomMove = r.nextInt(100);
                            //    int randomY = r.nextInt(400);
                                if(randomMove <= 25){
                                    handler.object.get(i).setVel_y(-5);
                                }

                                if(randomMove > 25 && randomMove <= 50){
                                    handler.object.get(i).setVel_y(+5);
                                }

                                if(randomMove > 50 && randomMove <= 75){
                                    handler.object.get(i).setVel_x(-5);
                                }

                                if(randomMove > 75 && randomMove <= 100){
                                    handler.object.get(i).setVel_x(+5);
                                }
                            }
                        }
                        
                    }else{
                        gameState = STATE.GameOver;
                        dbConnection dbcon = new dbConnection();
                        score = score + timeScore;
                        dbcon.addData(nama, score);
                    }
                }
            }
            
            // Menghitung object item di ame
            int ItemObject = 0;
            for(int i=0;i< handler.object.size(); i++){
                if(handler.object.get(i).getId() == ID.Item){
                   ItemObject++;
                }
            }
            
            if(ItemObject == 0){ // apbila object item habis/kosong memulai random item
                Random r = new Random();
                int randomX = r.nextInt(600);
                int randomY = r.nextInt(400);
                handler.addObject(new Items(randomX,randomY, ID.Item));
            }
            
        }
        
        stop();
    }
    
    private void tick(){
        handler.tick();
        if(gameState == STATE.Game){
        //    playSound("/party.wav");
            GameObject playerObject = null;
            for(int i=0;i< handler.object.size(); i++){
                if(handler.object.get(i).getId() == ID.Player){
                   playerObject = handler.object.get(i);
                }
            }
            if(playerObject != null){ // kondisi ketika player bertemu dengan item
                for(int i=0;i< handler.object.size(); i++){
                    if(handler.object.get(i).getId() == ID.Item){
                        if(checkCollision(playerObject, handler.object.get(i))){
                            playSound("/Eat.wav", "ITEM");
                            handler.removeObject(handler.object.get(i));
                            Random value = new Random();
                            int randomScore = value.nextInt(50);
                            int randomTime = value.nextInt(10);
                            score = score + randomScore;
                            time = time + randomTime;
                           // time = time+1;
                            break;
                        }
                    }
                    else if(handler.object.get(i).getId() == ID.Bot){ // kondisi ketika player bertemu dengan bot (musuh)
                        if(checkMate(playerObject, handler.object.get(i))){
                            gameState = STATE.GameOver;
                            dbConnection dbcon = new dbConnection();
                            score = score + timeScore;
                            dbcon.addData(nama, score);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public static boolean checkCollision(GameObject player, GameObject item){
        boolean result = false;
        
        int sizePlayer = 50;
        int sizeItem = 20;
        
        int playerLeft = player.x;
        int playerRight = player.x + sizePlayer;
        int playerTop = player.y;
        int playerBottom = player.y + sizePlayer;
        
        int itemLeft = item.x;
        int itemRight = item.x + sizeItem;
        int itemTop = item.y;
        int itemBottom = item.y + sizeItem;
        
        if((playerRight > itemLeft ) &&
        (playerLeft < itemRight) &&
        (itemBottom > playerTop) &&
        (itemTop < playerBottom)
        ){
            result = true;
        }
        
        return result;
    }
    
    public static boolean checkMate(GameObject player, GameObject bot){
        boolean result = false;
        
        int sizePlayer = 50;
        int sizeBot = 50;
        
        int playerLeft = player.x;
        int playerRight = player.x + sizePlayer;
        int playerTop = player.y;
        int playerBottom = player.y + sizePlayer;
        
        int botLeft = bot.x;
        int botRight = bot.x + sizeBot;
        int botTop = bot.y;
        int botBottom = bot.y + sizeBot;
        
        if((playerRight > botLeft ) &&
        (playerLeft < botRight) &&
        (botBottom > playerTop) &&
        (botTop < playerBottom)
        ){
            result = true;
        }
        
        return result;
    }
    
    private void render(){
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null){
            this.createBufferStrategy(3);
            return;
        }
        
        Graphics g = bs.getDrawGraphics();
        
        g.setColor(Color.decode("#F1f3f3"));
        g.fillRect(0, 0, WIDTH, HEIGHT);
                
        
        
        if(gameState ==  STATE.Game){
            handler.render(g);
            
            Font currentFont = g.getFont();
            Font newFont = currentFont.deriveFont(currentFont.getSize() * 1.4F);
            g.setFont(newFont);

            g.setColor(Color.BLACK);
            g.drawString("Score: " +Integer.toString(score), 20, 20);

            g.setColor(Color.BLACK);
            g.drawString("Time: " +Integer.toString(time), WIDTH-120, 20);
        }else{
            Font currentFont = g.getFont();
            Font newFont = currentFont.deriveFont(currentFont.getSize() * 3F);
            g.setFont(newFont);

            g.setColor(Color.BLACK);
            g.drawString("GAME OVER", WIDTH/2 - 120, HEIGHT/2 - 30);

            currentFont = g.getFont();
            Font newScoreFont = currentFont.deriveFont(currentFont.getSize() * 0.5F);
            g.setFont(newScoreFont);

            g.setColor(Color.BLACK);
            g.drawString("Score: " +Integer.toString(score), WIDTH/2 - 50, HEIGHT/2 - 10);
            
            g.setColor(Color.BLACK);
            g.drawString("Press Space to Continue", WIDTH/2 - 100, HEIGHT/2 + 30);
        }
                
        g.dispose();
        bs.show();
    }
    
    public static int clamp(int var, int min, int max){
        if(var >= max){
            return var = max;
        }else if(var <= min){
            return var = min;
        }else{
            return var;
        }
    }
    
    public void close(){
        window.CloseWindow();
    }
    
    public void playSound(String filename, String jenis){
        try {
            // Open an audio input stream.
            URL url = this.getClass().getResource(filename);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            // Get a sound clip resource.
            clip = AudioSystem.getClip();
            // Open audio clip and load samples from the audio input stream.
            clip.open(audioIn);
            
            // play along if bgm played
            if(jenis.equals("BGM")){ // jika lagu yg dipilih adalah BGM maka akan looping lagu BGM
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            clip.start();
        } catch (UnsupportedAudioFileException e) {
           e.printStackTrace();
        } catch (IOException e) {
           e.printStackTrace();
        } catch (LineUnavailableException e) {
           e.printStackTrace();
        }
    }
    
}
