import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public class Client implements Runnable
{
    private MyMidlet midlet;

    private final int slepTime = 1000;

    HttpConnection dc;

    private String port = "port=-1";

    private String baseURL = "http://localhost:8080/PigServer/PigServer";

    public Client(MyMidlet mid)
    {
        midlet = mid;
        sendMessage("register");
        new Thread(this).start();
    }

    String urlReceive;

    String urlSend;

    public void run()
    {
        while (true)
        {
            StringBuffer strbuf = new StringBuffer();
            urlReceive = baseURL + "?message=hello&" + port;

            try
            {
                dc = (HttpConnection) Connector
                        .open(urlReceive, Connector.READ);
                InputStream is = null;
                is = dc.openInputStream();
                int len = (int) dc.getLength();
                DataInputStream dis = new DataInputStream(is);
                if (len > 0)
                {
                    byte[] data = new byte[len];
                    dis.readFully(data);
                    for (int i = 0; i < data.length; i++)
                    {
                        strbuf.append((char) data[i]);
                    }
                }
                else
                {
                    int ch;
                    len = 0;
                    while ((ch = dis.read()) != -1)
                    {
                        strbuf.append((char) ch);
                        len++;
                    }
                }
                dis.close();
                is.close();
                dc.close();
                String s = strbuf.toString().trim();
                if (s != null && !s.equals(""))
                {
                    if (!s.equals("noData"))
                        System.out.println("message is:" + s);
                    if (s.startsWith("desks"))
                    {
                        midlet.setDesks(s);
                    }
                    else if (s.startsWith("takeseat"))
                    {
                        midlet.takeSeat(s);
                    }
                    else if (s.startsWith("exit"))
                    {
                        midlet.tryExit(s);
                    }
                    else if (s.startsWith("updatedesk"))
                    {
                        midlet.updateDesk(s);
                    }
                    else if (s.startsWith("noData"))
                    {
                        // 什么也不做
                    }
                    else if (s.startsWith("port"))
                    {
                        if (port.equals("port=-1"))
                        {
                            System.out.println("post");
                            port = s;
                        }
                    }
                    else if (s.startsWith("<html>"))
                    {
                        System.out
                                .println("send error return <html> is 500 or 404 error");
                    }
                    else
                    {
                        if (midlet.getCanvas() != null)
                            midlet.getCanvas().receiveMessage(s);
                        else
                            System.out.println("Error Canvas is null:::" + s);
                    }
                }
            }

            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            try
            {
                Thread.sleep(slepTime);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message)
    {
        System.out.println("send message:" + message);
        try
        {
            urlSend = baseURL + "?message=" + message + "&" + port;
            HttpConnection dc = (HttpConnection) Connector.open(urlSend,
                    Connector.READ);
            InputStream is = null;
            is = dc.openInputStream();
            is.close();
            dc.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public String getPort()
    {
        return port;
    }
}
