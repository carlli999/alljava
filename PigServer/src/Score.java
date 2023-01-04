import java.util.Enumeration;
import java.util.Hashtable;

public class Score
{
    private Hashtable scores;

    public Score()
    {
        scores = new Hashtable();
    }

    public void set(Desk d)// 设置所有玩家的分数
    {
        boolean b = isWinall(d);
        int s = 0;
        String name = "";
        if (b)
        {
            s = d.getScore(0);
            name = d.playerName[0];
            if (s == 26)
            { // play1 win
                insert(name, 0);
                insert(d.playerName[1], 26);
                insert(d.playerName[2], 26);
                insert(d.playerName[3], 26);
                return;
            }
            s = d.getScore(1);
            name = d.playerName[1];
            if (s == 26) // play2 win all
            {
                insert(name, 0);
                insert(d.playerName[0], 26);
                insert(d.playerName[2], 26);
                insert(d.playerName[3], 26);
                return;
            }
            s = d.getScore(2);
            name = d.playerName[2];
            if (s == 26) // play3 win all
            {
                insert(name, 0);
                insert(d.playerName[0], 26);
                insert(d.playerName[1], 26);
                insert(d.playerName[3], 26);
                return;
            }
            s = d.getScore(3);
            name = d.playerName[3];
            if (s == 26) // play2 win all
            {
                insert(name, 0);
                insert(d.playerName[0], 26);
                insert(d.playerName[1], 26);
                insert(d.playerName[2], 26);
                return;
            }
        }
        else
        {
            // play1
            name = d.playerName[0];
            s = d.getScore(0);
            insert(name, s);

            // play2
            name = d.playerName[1];
            s = d.getScore(1);
            insert(name, s);

            // play3
            name = d.playerName[2];
            s = d.getScore(2);
            insert(name, s);

            // play4
            name = d.playerName[3];
            s = d.getScore(3);
            insert(name, s);
        }
    }

    public int get(String name)// 获得name的分数
    {
        int t = 0;
        try
        {
            t = ((Integer) scores.get(name)).intValue();
        }
        catch (Exception exception)
        {
        }
        return t;
    }

    public void clear()
    {
        scores.clear();
    }

    private void insert(String name, int s)// 插入玩家name的分数+原来的分数
    {
        int t = 0;
        try
        {
            t = ((Integer) scores.get(name)).intValue();
        }
        catch (Exception exception)
        {
        }
        scores.put(name, new Integer(t + s));
    }

    private boolean isWinall(Desk d)// 判断是否有一玩家胜出所有玩家（分数达到26）
    {
        int s = d.getScore(0);
        if (s == 26)
            return true;
        s = d.getScore(1);
        if (s == 26)
            return true;
        s = d.getScore(2);
        if (s == 26)
            return true;
        s = d.getScore(3);
        return s == 26;
    }
}
