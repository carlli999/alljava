import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class MyMidlet extends MIDlet implements CommandListener
{
    private Client client;
    public static Display display;
    private GameCanvas canvas;
    private List playerList;
    private int[][] desks;
    private int trySeat, tryDesk;
    private Command okCommand;

    public MyMidlet()
    {
        display = Display.getDisplay(this);
        playerList = new List("Online player", Choice.EXCLUSIVE);
        Command exitCommand = new Command("Exit", Command.EXIT, 0);
        playerList.addCommand(exitCommand);
        okCommand = new Command("Seat", Command.OK, 0);
        playerList.addCommand(okCommand);
        playerList.setCommandListener(this);

        display.setCurrent(playerList);
        client = new Client(this);
    }

    public void quit()
    {
        try
        {
            destroyApp(false);
            notifyDestroyed();

        }
        catch (Exception exception)
        {
        }
    }

    protected void startApp() throws MIDletStateChangeException
    {
    }

    protected void pauseApp()
    {
    }

    protected void destroyApp(boolean p0) throws MIDletStateChangeException
    {
        client.sendMessage("exitGame");
        display.setCurrent(null);
    }

    public void commandAction(Command c, Displayable s)
    {
        if (c.getCommandType() == Command.EXIT)
        {
            client.sendMessage("exit");
            try
            {
                destroyApp(false);
                notifyDestroyed();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (c == okCommand)
        {
            if (playerList.getSelectedIndex() >= 0)
            {
                try
                {
                    String info = playerList.getString(playerList
                            .getSelectedIndex());
                    int index0 = info.indexOf("-");
                    int d = Integer.parseInt(info.substring(0, index0));
                    tryDesk = d;
                    client.sendMessage("take," + d);
                }
                catch (Exception exc)
                {
                    System.out.println("Error parseInt");
                    exc.printStackTrace();
                }
            }
        }
    }

    public GameCanvas getCanvas()
    {
        return canvas;
    }

    public void initialize()
    {
        canvas = null;
    }

    public void takeSeat(String message)
    { 
        if (canvas == null)
            canvas = new GameCanvas(this, client);
        else
            canvas.init();
        int index0 = message.indexOf(":");
        trySeat = Integer.parseInt(message.substring(index0 + 1));
        canvas.setSeatPos(trySeat);
        canvas.setDeskIndex(tryDesk);
        display.setCurrent(canvas);
    }

    public void tryExit(String message)
    {
        if (canvas == null)
            return;
        else
            canvas.init();
    }

    public void updateDesk(String str)
    {
        int index1 = str.indexOf(",");
        int index2 = str.indexOf(":", index1 + 1);
        int index3 = str.indexOf(",", index2 + 1);
        int index4 = str.indexOf(",", index3 + 1);
        int index5 = str.indexOf(",", index4 + 1);
        int d = -1;
        try
        {
            d = Integer.parseInt(str.substring(index1 + 1, index2));
            desks[d][0] = Integer.parseInt(str.substring(index2 + 1, index3));
            desks[d][1] = Integer.parseInt(str.substring(index3 + 1, index4));
            desks[d][2] = Integer.parseInt(str.substring(index4 + 1, index5));
            desks[d][3] = Integer.parseInt(str.substring(index5));

        }
        catch (Exception exc)
        {
        }
        playerList.set(d, d + "-" + desks[d][0] + "-" + desks[d][1] + "-"
                + desks[d][2] + "-" + desks[d][3], null);
    }

    public List getPlayerList()
    {
        return playerList;
    }

    public void setDesks(String string)
    {
        for (int i = 0; i < playerList.size(); i++)
            playerList.delete(i);
        int index1, index2, index3, index4, index0;
        index1 = string.indexOf(",");
        index2 = string.indexOf(":", index1 + 1);
        int desknum = Integer.parseInt(string.substring(index1 + 1, index2));
        desks = new int[desknum][4];

        index0 = index2;
        int counter = 0;
        while (counter < desknum)
        {
            index1 = string.indexOf(",", index0 + 1);
            index2 = string.indexOf(",", index1 + 1);
            index3 = string.indexOf(",", index2 + 1);
            index4 = string.indexOf(":", index3 + 1);

            desks[counter][0] = Integer.parseInt(string.substring(index0 + 1,
                    index1));
            desks[counter][1] = Integer.parseInt(string.substring(index1 + 1,
                    index2));
            desks[counter][2] = Integer.parseInt(string.substring(index2 + 1,
                    index3));
            if (index4 > 0)
                desks[counter][3] = Integer.parseInt(string.substring(
                        index3 + 1, index4));
            else
            {
                string = string.trim();
                desks[counter][3] = Integer.parseInt(string
                        .substring(index3 + 1));
            }
            playerList.append(counter + "-" + desks[counter][0] + "-"
                    + desks[counter][1] + "-" + desks[counter][2] + "-"
                    + desks[counter][3], null);
            index0 = index4;
            counter++;
        }
    }
}
