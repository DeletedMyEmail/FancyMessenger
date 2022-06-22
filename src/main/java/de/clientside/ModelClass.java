package de.clientside;

import DataStructures.DBMS.Queue;

public class ModelClass
{
    private static Queue<String> commands = new Queue<>();

    protected static void addCommand(String command)
    {
        commands.enqueue(command);
    }

    protected static String getCommand()
    {
        String front = commands.front();
        commands.dequeue();
        return front;
    }

    protected static void setController()
    {

    }
}

