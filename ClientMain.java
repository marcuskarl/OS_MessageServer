//Andrew Robinson
//Marcus Karl

package client;

import Shared.MsgCommObj;
import java.io.*;
import java.net.*;
import java.util.*;

public class ClientMain 
{
    //Function to handle server menu and actions
    public static void userMenu(String uN, ObjectInputStream cStream, ObjectOutputStream sStream)
    {
        int menuChoice = 0;
        int msgCount = 0;
        //Linked list to hold the messages a user has recieved until they are read
        LinkedList <MsgCommObj> msgList = new LinkedList();
        //Exit menu loop when user chooses option 8
        boolean exit = false;
        
        //Inform server of user name and connection
        MsgCommObj msg = new MsgCommObj();
        msg.setFromUserName(uN);
        msg.setUserOption(0);
        
        try
        {
            //Write the user name to the server via the message object
            sStream.writeObject(msg);
        }
        catch(IOException ex)
        {
            System.out.println("ERROR: " + ex);
        }
        
        //Outer main menu loop ****
        do 
        {
            
            msg = new MsgCommObj();
            msg.setFromUserName(uN);
            
            //Print the options menu containing server/client capabilities
            System.out.println("1. Display the names of all known users.");
            System.out.println("2. Display the names of all currently connected users.");
            System.out.println("3. Send a text message to a particular user.");
            System.out.println("4. Send a text message to all currently connected users.");
            System.out.println("5. Send a text message to all known users.");
            System.out.println("6. Get my messages.");
            System.out.println("7. Read my messages.");
            System.out.println("8. Exit.");
            System.out.println();
            
            //Scanner for menu choices
            Scanner menuScan = new Scanner(System.in);
            //Scanner for options within menu choices
            Scanner msgScan = new Scanner(System.in);
            
            //Loop to get user menu input
            do
            {
                //Prompt user for menu choice
                System.out.print("Enter a number from the menu: ");
                //Read input as an integer between 1 and 8, otherwise prompt for input again
                menuChoice = Integer.parseInt(menuScan.nextLine());
                System.out.println();
            }
            while(!(menuChoice <= 8 && menuChoice > 0));
            
            try
            {
                //Determine action based on menu choice
                switch(menuChoice)
                {
                    case 1: //Display names of all known users
                        //Write menu choice to server
                        msg.setUserOption(menuChoice);
                        sStream.writeObject(msg);
                        //Read message from server and display contents
                        msg = (MsgCommObj)cStream.readObject();
                        System.out.println(msg.getUserMsg());
                        break;
                        
                    case 2: //Display names of all connected users
                        //Write menu choice to server
                        msg.setUserOption(menuChoice);
                        sStream.writeObject(msg);
                        //Read message from server and display contents
                        msg = (MsgCommObj)cStream.readObject();
                        System.out.println(msg.getUserMsg());
                        break;
                        
                    case 3: //Send a message to a user
                        //Set choice in message
                        msg.setUserOption(menuChoice);
                        //Prompt user for message recipients user name
                        System.out.print("Enter message recipients name: ");
                        //Set To: field in message
                        msg.setToUserName(msgScan.nextLine());
                        System.out.println();
                        //Prompt user for message to send
                        System.out.print("Enter message for " + msg.getToUserName() + ": ");
                        //Set message data to user input
                        msg.setUserMsg(msgScan.nextLine());
                        System.out.println();
                        //Send message to server for specified recipient
                        sStream.writeObject(msg);
                        break;
                        
                    case 4: //Send a message to all connected users
                        //Set choice in message
                        msg.setUserOption(menuChoice);
                        //Prompt user for message to send
                        System.out.println("Enter message for all connected users: ");
                        System.out.println();
                         //Set message data to user input
                        msg.setUserMsg(msgScan.nextLine());
                        //Send message to server for all connected users 
                        sStream.writeObject(msg);
                        break;
                        
                    case 5: //Send message to all known users
                        //Set choice in message
                        msg.setUserOption(menuChoice);
                        //Prompt user for message to send
                        System.out.println("Enter message for all known users: ");
                        System.out.println();
                        //Set message data to user input
                        msg.setUserMsg(msgScan.nextLine());
                        //Send message to server for all known users
                        sStream.writeObject(msg);
                        break;
                        
                    case 6: //get user messages
                        //Set choice in message
                        msg.setUserOption(menuChoice);
                        
                        //Loop to get messages from server as long as option in message recieved from server is zero 
                        do
                        {
                            //Write menu choice to server
                            sStream.writeObject(msg);
                            //Read message from server
                            msg = (MsgCommObj)cStream.readObject();
                            //Add message data to linked list
                            msgList.addLast(msg);
                            //increment count for total unread messages
                            msgCount++;
                        }
                        while(msg.getUserOption()== 0);
                        
                        //Decrement to account for post increment of messages -- do while last iteration will increment without receiving a new message
                        msgCount--;
                        //Prompt user amount of unread messages they have
                        System.out.print("You have " + msgCount + " unread messages");
                        System.out.println();
                        break;
                        
                    case 7: //read user messages
                        int msgNum = 0;
                        
                        //Loop to read messages while input is valid message menu number
                        do
                        {
                            //Print all unread message from linked list with from user name and timestamp
                            for(int i = 0; i < msgCount; i++)
                            {
                                //Copy message at ith position from linked list
                                msg = msgList.get(i);
                                //Print reference menu number, From: field, and timestamp
                                System.out.print((i+1) + ". From: " + msg.getFromUserName() + " at " + msg.getDateTime());
                                System.out.println();
                            }
                            
                            //Prompt user to input a menu number to read corresponding message
                            System.out.print("Enter number to display message or any other key to return to main menu: ");
                            //Get user input
                            msgNum = msgScan.nextInt();
                            System.out.println();
                            
                            //If valid menu input
                            if(msgNum > 0 && msgNum <= msgCount)
                            {
                                //Print the message requested and delete from linked list
                                msg = msgList.remove(msgNum - 1);
                                System.out.println(msg.getUserMsg());
                                msgCount--;
                            }
                        }
                        while(msgNum > 0 && msgNum <= msgCount);
                        break;

                    case 8: //exit
                        msg.setUserOption(menuChoice);
                        sStream.writeObject(msg);
                        msgScan.close();
                        menuScan.close();
                        exit = true;
                }
            }
            catch(IOException | ClassNotFoundException ex)
            {
                System.out.println("ERROR: " + ex);
            }
        } 
        while (!exit);
         
        System.out.println("Exiting");
    }

        public static void main(String[] args) 
        {
            String userName = null;
            String server = "cs1";//args[0]
            int port = 5002;//Integer.parseInt(args[1]);
            InetAddress address;
            Socket serverConnection = null;
            ObjectInputStream cin = null;
            ObjectOutputStream sout = null;

            if(server.equals("cs1"))
                server = "cs1.utdallas.edu";
            else if(server.equals("cs2"))
                server = "cs2.utdallas.edu";

            try
            {
                address = InetAddress.getByName(server);
                System.out.println("Connecting to: " + server + ":" + port + "....");
                serverConnection = new Socket(address, port);
                
                //System.out.println("Connection success!");
                DataInputStream cinPort = new DataInputStream(serverConnection.getInputStream());
                 port = cinPort.readInt();
                cinPort.close();
                
                //System.out.println("Received port: " + port);
                serverConnection.close();
                serverConnection = new Socket(server, port);
                //System.out.println("Connected to new thread.");
                
                sout = new ObjectOutputStream(serverConnection.getOutputStream());
                //System.out.println("Created output stream.");
                
                cin = new ObjectInputStream(serverConnection.getInputStream());
                //System.out.println("Created input stream.");
                
                System.out.println("Connection success!");
                
            }
            catch(IOException ex)
            {
                System.out.println("ERROR: " + ex);
            }

            System.out.print("Please enter a user name: ");
            Scanner scan = new Scanner(System.in);
            userName = scan.nextLine();
            System.out.println();
            
        
            userMenu(userName, cin, sout);
            
            try
            {
                serverConnection.close();
            }
            catch(IOException ex)
            {
                System.out.println("ERROR: " + ex);
            }
        
    }
    
}
