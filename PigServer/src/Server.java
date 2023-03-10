import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Server extends HttpServlet
{
    private Hashtable players = new Hashtable();
    private final int DESKNUM = 6; // 定义桌子数量
    private final int NUM = 4;
    private Desk[] desks = new Desk[DESKNUM];
    private int counter = 1; // 玩家计数器
    private Player player;
    PrintWriter out;

    /*
     * 初始化 对players(Hashtable)、desks(所有桌子)初始化
     */
    public void init(ServletConfig config) throws ServletException
    { // 初始化
        super.init(config);
        players.clear();
        for (int i = 0; i < DESKNUM; i++)
        {
            desks[i] = new Desk();
            desks[i].setID(i);
        }
    }

    // 接受信息主方法 接受各种信息，然后调用相应的处理方法
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        out = response.getWriter();
        String message = request.getParameter("message");
        String port = request.getParameter("port");
        port = port.trim();
        if (!message.startsWith("hello"))
            System.out.println(message);
        if (message.startsWith("register"))
        {// 登陆信息
            System.out.println("register is start");
            denglu(request);
            return;
        }
        if (!port.equals("-1"))
            player = (Player) players.get(request.getRemoteAddr() + ":" + port);
        else
            player = (Player) players.get(request.getRemoteAddr() + ":"
                    + request.getRemotePort());
        if (message.startsWith("take"))
        {// 落座信息
            luozuo(message, player);
            return;
        }
        else if (message.startsWith("start"))
        {// 开始信息move:
            tryStart(player);
            return;
        }
        else if (message.startsWith("hello"))
        {
            if (!player.data.empty())
                out.println((String) player.data.pop());
            else
                out.println("noData");
            return;
        }
        else if (message.startsWith("show"))
        {// 出牌信息move:
            tryShow(message);
            return;
        }
        else if (message.startsWith("exitGame"))
        {
            tryExitGame(players, player, request);
            return;
        }
        else if (message.startsWith("exit"))
        {// 退出信息
            tryExit(players, player);
            return;
        }
        else
        {
            System.out.println("No desk exist");
        }
    }

    public void tryShow(String message)
    {
        Desk d1 = player.getDesk();
        d1.tryShow(message);
    }

    public void sendMessage(Player p, String str)
    { // 发送信息，等待用户读取
        System.out.println("sendMessage is:" + str);
        p.data.push(new String(str));
    }

    public void updateClientsDesk(int deskid)
    { // 更新所有客户桌面
        for (Enumeration en = players.elements(); en.hasMoreElements();)
        {
            Player player = (Player) en.nextElement();
            if (player != null)
                updateDesk(player, deskid);
        }
    }

    public void updateDesk(Player isa, int deskid)
    { // 更新单个桌面
        String message = "updatedesk," + deskid;
        String str = "";
        for (int i = 0; i < desks[deskid].getPlayersCounter(); i++)
        {
            if (i == 0)
            {
                if (desks[deskid].isEmpty(i))
                    str = "0";
                else
                    str = "1";
            }
            else
            {
                if (desks[deskid].isEmpty(i))
                    str = str + ",0";
                else
                    str = str + ",1";
            }
        }
        message = message + ":" + str;
        sendMessage(isa, message);
    }

    public void sendDeskList(Player player)
    { // 获得桌面列表
        String message = "desks," + DESKNUM;
        for (int i = 0; i < DESKNUM; i++)
        {
            String str = "";
            for (int j = 0; j < desks[i].getPlayersCounter(); j++)
            {
                if (j == 0)
                {
                    if (desks[i].isEmpty(j))
                        str = "0";
                    else
                        str = "1";
                }
                else
                {
                    if (desks[i].isEmpty(j))
                        str = str + ",0";
                    else
                        str = str + ",1";
                }
            }
            message = message + ":" + str;
        }
        sendMessage(player, message);
    }

    public void denglu(HttpServletRequest request)
    { // 处理登陆信息
        Player player = new Player(request.getRemoteAddr(), request
                .getRemotePort());
        player.setID(counter);

        counter++;
        players.put(request.getRemoteAddr() + ":" + request.getRemotePort(),
                player);

        sendMessage(player, "port=" + request.getRemotePort());
        sendDeskList(player); // 发送大厅信息
    }

    public void luozuo(String message, Player player)
    {// 处理落座信息
        try
        {
            int index1 = message.indexOf(",");
            int dindex = Integer.parseInt(message.substring(index1 + 1));
            if (dindex < DESKNUM && dindex >= 0)
            {
                for (int i = 0; i < NUM; i++)
                {
                    if (desks[dindex].isEmpty(i))
                    {
                        desks[dindex].setPlayer(i, player);
                        player.setDesk(desks[dindex]);
                        sendMessage(player, "takeseat:"
                                + player.getDesk().getPlayerSeat(player));
                        updateClientsDesk(dindex);
                        break;
                    }
                }
            }
        }
        catch (Exception exc)
        {
        }
    }

    public void tryStart(Player player)
    {// 处理开始信息
        player.start();
        Desk d1 = player.getDesk();
        if (d1.isReady())
        {
            System.out.println("ok");
            d1.start();
        }
    }

    public void tryExit(Hashtable players, Player player)
    {
        Desk de = player.getDesk();
        de.removePlayer(player);
        de.sendMessageToOther(player,"exit");
        updateClientsDesk(de.getID());
    }

    public void tryExitGame(Hashtable players, Player player,
            HttpServletRequest request)
    {
        Desk de = player.getDesk();
        de.sendMessageToOther(player,"exit");
        players.remove(player.getIP() + ":" + player.getPort());
        de.removePlayer(player);
        player = null;
        updateClientsDesk(de.getID());
    }
}