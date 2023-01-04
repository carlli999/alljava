import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class GameCanvas extends Canvas implements CommandListener
{
    private MyMidlet game;
    private boolean dinfo = true;
    private Image LRCardImage = null;
    private Image UPCardImage = null;
    private Image backImage = null;
    private Image backGround = null;
    private Image messageBak = null;
    private Image messageImage = null;
    private Image showButtond;
    private Image tempButtonImg = null;
    private Image panel;
    private Image scoreImage;
    private Image scoreGround;
    private Image infoImage;
    private Image[] playerImg = new Image[2];
    private Graphics bufferg;
    private Graphics messageBuffer;
    private Graphics scoreBuffer;
    private final int NUM = 4;
    private final int cardWidth;
    private final int cardHeight;
    private Vector cardsRound;
    private int roundNum;// 局数
    private int roundKey;
    private final int WIDTH = getWidth();
    private final int HEIGHT = getHeight();
    private int messageWidth;
    private int messageHeight;
    private int selectIndex = -1;
    private int cardCount = 13;// 牌数
    private int LCount = 13;// 左面玩家的牌数
    private int RCount = 13;// 右面玩家的牌数
    private int UCount = 13;// 对面玩家的牌数
    private boolean myTurn = false;// 是不是到我出牌
    private Card[] myCard = new Card[cardCount];
    private Card[][] scores = new Card[NUM][14];
    private String[] playerName = new String[NUM];
    private Hashtable scoreAmount = new Hashtable();
    private int desknum = -1; // 桌子序号
    private int seatPos = -1; // 座位序号
    private boolean banker = false;// 判断自己是不是庄
    private Client client;
    protected Command exitCmd, start;
    private boolean keyPress = true;
    private boolean gameOver = false;

    public void init()
    {
        banker = false;
        myTurn = false;
        this.addCommand(start);
    }

    public void reset()
    {
        cardCount = 13;
        scoreAmount.clear();
        cardsRound.removeAllElements();
        roundKey = 0;
        roundNum = 0;
        cls();
        scoreCls();
        for (int i = 0; i < NUM; i++)
            for (int j = 0; j < 14; j++)
                scores[i][j] = new Card(-1);

        keyPress = true;
        gameOver = false;
        banker = false;
        myTurn = false;
    }

    public GameCanvas(MyMidlet game, Client client)// 构造函数
    {
        try
        {
            infoImage = Image.createImage("/message/bg_playerinfo.png");
            playerImg[0] = Image.createImage("/message/nan.png");
            playerImg[1] = Image.createImage("/message/nv.png");
            LRCardImage = Image.createImage("/cards/h.png");
            UPCardImage = Image.createImage("/cards/0.png");
            messageImage = Image.createImage("/message.png");
            showButtond = Image.createImage("/button/show.png");
            if (WIDTH <= 180)
            {
                scoreGround = Image.createImage("/score180.png");
                backGround = Image.createImage("/back.png");
            }
            else if (WIDTH >= 240)
            {
                scoreGround = Image.createImage("/score240.png");
                backGround = Image.createImage("/bigBack.png");
            }
        }
        catch (Exception e)
        {
            System.out.println("error image...");
            e.printStackTrace();
        }
        if (messageImage != null)
        {
            messageWidth = messageImage.getWidth();
            messageHeight = messageImage.getHeight();
        }
        else
        {
            messageWidth = -1;
            messageHeight = -1;
        }
        cardWidth = UPCardImage.getWidth();
        cardHeight = UPCardImage.getHeight();
        panel = Image.createImage(WIDTH, HEIGHT);
        messageBak = Image.createImage(messageWidth, messageHeight);
        scoreImage = Image.createImage(scoreGround.getWidth(), scoreGround
                .getHeight());
        scoreBuffer = scoreImage.getGraphics();
        scoreCls();
        messageBuffer = messageBak.getGraphics();
        bufferg = panel.getGraphics();
        cls();
        cardsRound = new Vector();
        roundKey = 0;
        roundNum = 0;

        for (int i = 0; i < NUM; i++)
        {
            playerName[i] = "P" + i;
            for (int j = 0; j < 14; j++)
                scores[i][j] = new Card(-1);
        }
        this.game = game;
        this.client = client;
        exitCmd = new Command("退出", Command.EXIT, 0);
        start = new Command("开始", Command.OK, 1);
        addCommand(start);
        addCommand(exitCmd);
        setCommandListener(this);
    }

    public int getDeskIndex()
    {
        return desknum;
    }

    public void setDeskIndex(int i)
    {
        desknum = i;
    }

    public void setSeatPos(int i)
    {
        seatPos = i;
    }

    public int getSeatPos()
    {
        return seatPos;
    }

    private void scoreCls()
    {
        scoreBuffer.drawImage(scoreGround, 0, 0, Graphics.LEFT | Graphics.TOP);
    }

    private void cls()
    {
        bufferg.drawImage(backGround, WIDTH / 2, HEIGHT / 2, Graphics.HCENTER
                | Graphics.VCENTER);
    }

    protected void paint(Graphics g)
    {
        drawPlayer(gameOver);
        g.drawImage(panel, 0, 0, Graphics.LEFT | Graphics.TOP);
        if (dinfo)
            drawInfo(g);
    }

    public void commandAction(Command c, Displayable d)
    {
        if (c == exitCmd)
        {
            client.sendMessage("exit");
            seatPos = -1;
            desknum = -1;
            MyMidlet.display.setCurrent(game.getPlayerList());
        }
        else if (c == start)
        {
            banker = false;
            reset();
            this.removeCommand(start);
            client.sendMessage("start");
        }
    }

    private void drawPlayer(boolean show)
    {
        if (!gameOver)
        {
            for (int i = 0; i < NUM; i++)
            {
                char[] playerChar = playerName[i].toCharArray();
                int wid = Font.getDefaultFont().charsWidth(playerChar, 0,
                        playerChar.length - 1);
                if (i == seatPos)
                {
                    bufferg.drawString(playerName[i], (WIDTH - wid) / 2, HEIGHT
                            - Font.getDefaultFont().getHeight() - cardHeight
                            - 10, Graphics.LEFT | Graphics.TOP);
                }
                else if (i == (seatPos + 1) % NUM)
                {
                    bufferg.drawString(playerName[i], WIDTH - wid - 25,
                            HEIGHT / 2 - 10, Graphics.LEFT | Graphics.TOP);
                }
                else if (i == (seatPos + 2) % NUM)
                {
                    bufferg.drawString(playerName[i], (WIDTH - wid) / 2, 10,
                            Graphics.LEFT | Graphics.TOP);
                }
                else if (i == (seatPos + 3) % NUM)
                {
                    bufferg.drawString(playerName[i], 25, HEIGHT / 2 - 10,
                            Graphics.LEFT | Graphics.TOP);
                }
            }
        }
    }

    private void drawUBackCard()
    {
        int px = WIDTH / 2 - 13 * 10 / 2;
        int x = WIDTH / 2 - cardCount * 10 / 2;
        int y = 5;

        int oldx = bufferg.getClipX();
        int oldy = bufferg.getClipY();
        int oldw = bufferg.getClipWidth();
        int oldh = bufferg.getClipHeight();
        bufferg.setClip(px, y, WIDTH - px, UPCardImage.getHeight());
        cls();
        try
        {
            for (int i = 0; i < UCount; i++)
            {
                bufferg.drawImage(UPCardImage, x, y, Graphics.LEFT
                        | Graphics.TOP);
                x += 10;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        repaint();
        bufferg.setClip(oldx, oldy, oldw, oldh);
    }

    private void drawLBackCard()
    {
        int py = HEIGHT / 2 - 13 * 7 / 2 - 8;
        int y = HEIGHT / 2 - UCount * 7 / 2 - 8;
        int x = 10;

        int oldx = bufferg.getClipX();
        int oldy = bufferg.getClipY();
        int oldw = bufferg.getClipWidth();
        int oldh = bufferg.getClipHeight();
        bufferg.setClip(x, py, LRCardImage.getWidth(), 13 * 7 + 7);
        cls();
        try
        {
            for (int i = 0; i < LCount; i++)
            {
                bufferg.drawImage(LRCardImage, x, y, Graphics.LEFT
                        | Graphics.TOP);
                y += 7;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        repaint();
        bufferg.setClip(oldx, oldy, oldw, oldh);
    }

    private void drawRBackCard()
    {
        int py = HEIGHT / 2 - 13 * 7 / 2 - 8;
        int y = HEIGHT / 2 - cardCount * 7 / 2 - 8;
        int x = WIDTH - LRCardImage.getWidth() - 10;

        int oldx = bufferg.getClipX();
        int oldy = bufferg.getClipY();
        int oldw = bufferg.getClipWidth();
        int oldh = bufferg.getClipHeight();
        bufferg.setClip(x, py, LRCardImage.getWidth(), 13 * 7 + 7);
        cls();
        try
        {
            for (int i = 0; i < RCount; i++)
            {
                bufferg.drawImage(LRCardImage, x, y, Graphics.LEFT
                        | Graphics.TOP);
                y += 7;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        repaint();
        bufferg.setClip(oldx, oldy, oldw, oldh);
    }

    protected synchronized void keyPressed(int keyCode) // 处理按键
    {
        int action = getGameAction(keyCode);
        if (action == Canvas.LEFT)
        {
            // 如果按键是左键
            if (getSelectIndex() <= 0)
            {
                setSelectIndex(cardCount - 1);
            }
            else
            {
                setSelectIndex(getSelectIndex() - 1);
            }
            drawCards();
        }
        else if (action == Canvas.RIGHT)
        {
            // 如果按键是右键
            if (getSelectIndex() >= cardCount - 1)
            {
                setSelectIndex(0);
            }
            else
            {
                setSelectIndex(getSelectIndex() + 1);
            }
            drawCards();
        }
        else if (action == Canvas.UP)
        {
            // 如果按键是上键
            dinfo = !dinfo;
            repaint();

        }
        else if (action == Canvas.DOWN)
        {
            // 如果按键是下键
            dinfo = !dinfo;
            repaint();
        }
        else if (action == Canvas.FIRE && myTurn && keyPress && !gameOver)
        {
            // 如果按键是select（开火）键
            deal();
            drawCards();
        }
    }

    private void showButton(boolean b)
    {
        if (tempButtonImg == null)
            tempButtonImg = Image.createImage(panel);
        int x = (WIDTH - showButtond.getWidth()) / 2;
        int y = HEIGHT - cardHeight - showButtond.getHeight() - 10 - 8;
        int oldx = bufferg.getClipX();
        int oldy = bufferg.getClipY();
        int oldw = bufferg.getClipWidth();
        int oldh = bufferg.getClipHeight();
        bufferg.setClip(x, y, showButtond.getWidth(), showButtond.getHeight());
        if (b)
        {
            bufferg.drawImage(showButtond, x, y, Graphics.LEFT | Graphics.TOP);
        }
        else
        {
            if (tempButtonImg != null)
                bufferg.drawImage(tempButtonImg, 0, 0, Graphics.LEFT
                        | Graphics.TOP);
            tempButtonImg = null;
        }
        repaint(x, y, showButtond.getWidth(), showButtond.getHeight());
        bufferg.setClip(oldx, oldy, oldw, oldh);
    }

    private boolean haveSuit(int key)// 是否有相应的花牌
    {
        for (int i = 0; i < cardCount; i++)
        {
            if ((myCard[i] != null) && (myCard[i].getSuitIndex() == key))
            {
                return true;
            }
        }
        return false;
    }

    public Card show(int index, Vector v)// 出牌规则
    {
        boolean isFirst = false;
        if ((v.size() >= 4) || (v.size() == 0))
        {
            isFirst = true;
            roundKey = myCard[index].getSuitIndex();
        }
        try
        {
            Card c = null;
            int rec = -1;
            if (myCard[index] != null)
            {
                c = myCard[index];
                rec = index;
            }
            if ((roundNum == 0) && (c != null) && isFirst
                    && (c.getValue() != 0))// 第一张必须梅花2
            {
                alert();
                return null;
            }
            if ((c != null) && !isFirst && haveSuit(roundKey) && // 如果有适合花色的牌没出，发出警告
                    (c.getSuitIndex() != roundKey))
            {
                alert();
                return null;
            }
            if (c != null)
            {
                myCard[rec] = null;
                // 排序
                for (int i = rec + 1; i < cardCount; i++)
                {
                    myCard[i - 1] = myCard[i];
                }
                myCard[cardCount - 1] = null;
                cardCount--;
            }
            return c;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private void alert()// 警告信息
    {
        keyPress = false;
        backImage = Image.createImage(panel);
        messageBuffer.drawImage(messageImage, 0, 0, Graphics.LEFT
                | Graphics.TOP);

        bufferg.drawImage(messageBak, WIDTH / 2, HEIGHT / 2, Graphics.HCENTER
                | Graphics.VCENTER);
        repaint();
        new Thread()
        {
            public void run()
            {
                try
                {
                    sleep(1000);
                    bufferg.drawImage(backImage, WIDTH / 2, HEIGHT / 2,
                            Graphics.HCENTER | Graphics.VCENTER);
                    keyPress = true;
                    repaint();
                    backImage = null;
                }
                catch (Exception e)
                {
                }
            }
        }.start();
    }

    public void deal()// 处理FIRE按键和出牌等。。
    {
        if (selectIndex < 0)
        {
            return;
        }
        Card c = show(selectIndex, cardsRound);
        if (c == null)
        {
            return;
        }
        else
        {
            put(c);
            showButton(false);
            drawOtherCard(seatPos, c.getValue());
            client.sendMessage("show:" + seatPos + "," + c.getValue());
            myTurn = false;
            selectIndex = -1;
            return;
        }
    }

    private void put(Card c)// 把出牌放入每轮临时牌里
    {
        roundNum++;
        if (cardsRound.size() == 0)
            roundKey = c.getSuitIndex();
        else if (cardsRound.size() == 4)
        {
            roundKey = c.getSuitIndex();
            cardsRound.removeAllElements();
            clearDesk();
        }
        cardsRound.addElement(c);
    }

    private void clearDesk()// 清除玩家桌面上的牌，以便重新出牌
    {
        int x = WIDTH / 2 - cardWidth - cardWidth / 2;
        int y = HEIGHT / 2 - cardHeight - cardHeight / 2;
        int w = cardWidth * 3;
        int h = cardHeight * 2;
        int oldx = bufferg.getClipX();
        int oldy = bufferg.getClipY();
        int oldw = bufferg.getClipWidth();
        int oldh = bufferg.getClipHeight();

        bufferg.setClip(x, y, w, h);
        cls();
        repaint(x, y, w, h);
        bufferg.setClip(oldx, oldy, oldw, oldh);
    }

    public void receiveMessage(String message)// 接受信息
    {
        if (message.startsWith("turn"))
        {
            showButton(true);
            myTurn = true;
            return;
        }
        else if (message.startsWith("deliver"))
        {// 发牌的信息
            dinfo = false;
            gameOver = false;
            receiveCards(message);
            drawUBackCard();
            drawRBackCard();
            drawLBackCard();
            drawCards();
            return;
        }
        else if (message.startsWith("banker"))
        {// 庄的信息
            receiveBanker(message);
            return;
        }
        else if (message.startsWith("show"))
        {// //出牌信息
            receiveShow(message);
            return;
        }
        else if (message.startsWith("GameOver"))
        {// 结束的信息
            receiveScores(message);
            gameOver = true;
            gameOver();
            return;
        }
        else if (message.startsWith("exitgame"))
        {// 退出信息
            game.initialize();
            MyMidlet.display.setCurrent(game.getPlayerList());
            return;
        }
    }

    public void receiveShow(String message)// 出牌信息
    {
        int index0 = message.indexOf(":");
        int index1 = message.indexOf(",", index0 + 1);
        int seat = Integer.parseInt(message.substring(index0 + 1, index1));
        int card = Integer.parseInt(message.substring(index1 + 1));
        Card temp = new Card(card);
        put(temp);
        drawOtherCard(seat, card);
    }

    private void drawOtherCard(int seat, int card)// 从绘玩家出的牌
    {
        int x = -1, y = -1;
        if (seat == seatPos)
        {
            x = WIDTH / 2 - cardWidth / 2;
            y = HEIGHT / 2 - cardHeight / 2;
        }
        else if (seat == (seatPos + 1) % NUM)
        {
            RCount--;
            drawRBackCard();
            x = WIDTH / 2 + cardWidth / 2;
            y = HEIGHT / 2 - cardHeight;
        }
        else if (seat == (seatPos + 2) % NUM)
        {
            UCount--;
            drawUBackCard();
            x = WIDTH / 2 - cardWidth / 2;
            y = HEIGHT / 2 - cardHeight - cardHeight / 2;
        }
        else if (seat == (seatPos + 3) % NUM)
        {
            LCount--;
            drawLBackCard();
            x = WIDTH / 2 - cardWidth - cardWidth / 2;
            y = HEIGHT / 2 - cardHeight;
        }
        else
            return;

        try
        {
            bufferg.drawImage(new Card(card).getImage(), x, y, Graphics.LEFT
                    | Graphics.TOP);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        repaint();
    }

    private void drawCards()// 绘制自己的牌
    {
        int px = WIDTH / 2 - 13 * 10 / 2;
        int x = WIDTH / 2 - cardCount * 10 / 2;
        int y = HEIGHT - cardHeight - 10;

        int oldx = bufferg.getClipX();
        int oldy = bufferg.getClipY();
        int oldw = bufferg.getClipWidth();
        int oldh = bufferg.getClipHeight();
        bufferg.setClip(px, y - 8, 10 * 13 + 5, HEIGHT - 10);
        cls();
        try
        {
            int n = 0;
            for (int i = 0; i < cardCount; i++)
            {
                if (myCard[i] != null)
                {
                    int y2 = y;

                    if (n == getSelectIndex())
                    {
                        y2 -= 8;
                    }
                    bufferg.drawImage(myCard[i].getImage(), x, y2,
                            Graphics.LEFT | Graphics.TOP);
                    x += 10;
                    n++;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        repaint();
        bufferg.setClip(oldx, oldy, oldw, oldh);
    }

    private void sortCard()
    {
        Card temp = null;
        for (int i = myCard.length - 1; i > 0; i--)
        {
            for (int j = 0; j < i; j++)
            {
                if (myCard[j].getSuitIndex() > myCard[i].getSuitIndex())
                {
                    temp = myCard[i];
                    myCard[i] = myCard[j];
                    myCard[j] = temp;
                }
                else if (myCard[i].getSuitIndex() == myCard[j].getSuitIndex()
                        && myCard[j].getCardIndex() < myCard[i].getCardIndex())
                {
                    temp = myCard[i];
                    myCard[i] = myCard[j];
                    myCard[j] = temp;
                }
            }
        }

    }

    private void receiveCards(String str)
    {
        String card = null;
        int count = 0, index1;
        int index0 = str.indexOf(",");
        while (count < myCard.length)
        {
            index1 = str.indexOf(",", index0 + 1);
            if (index1 > 0)
                card = str.substring(index0 + 1, index1);
            else
                card = str.substring(index0 + 1);
            myCard[count] = new Card(Integer.parseInt(card));
            index0 = index1;
            count++;
        }
        LCount = 13;
        RCount = 13;
        UCount = 13;
        sortCard();
    }

    private void receiveBanker(String str)
    {
        int index0 = str.indexOf(":");
        if (Integer.parseInt(str.substring(index0 + 1)) == seatPos)
        {
            banker = true;
        }
    }

    private void receiveScores(String str)
    {
        String card = null;
        int count = 0, index0 = 0, index1 = 0, indexs;
        index0 = str.indexOf(",");
        indexs = str.indexOf(":");
        for (int i = 0; i < NUM; i++)
        {
            count = 0;
            while (count < scores[i].length)
            {
                index1 = str.indexOf(",", index0 + 1);
                if (index1 > 0)
                    card = str.substring(index0 + 1, index1);
                else
                    card = str.substring(index0 + 1, indexs);
                if (!card.equals("-1"))
                    scores[i][count] = new Card(Integer.parseInt(card));
                else
                    scores[i][count] = null;
                index0 = index1;
                count++;
            }
        }
        index0 = indexs;
        String pscore = null;
        for (int i = 0; i < NUM; i++)
        {
            pscore = "";
            index1 = str.indexOf(":", index0 + 1);
            if (index1 > 0)
                pscore = str.substring(index0 + 1, index1);
            else
                pscore = str.substring(index0 + 1);
            scoreAmount.put(playerName[i], pscore);
            index0 = index1;
        }
    }

    public int getSelectIndex()
    {
        return selectIndex;
    }

    public void setSelectIndex(int selectIndex)
    {
        this.selectIndex = selectIndex;
    }

    private void drawTitle(Graphics g)
    {
        String t = "得分:本局得分/总分";
        String n = null;
        if (n == null)
        {
            g.setColor(0x000000);
            g.drawString(t, 40, 30, 20);
            return;
        }
        else
        {
            g.setColor(0x000000);
            t = String.valueOf(String.valueOf(n)).concat("失败，游戏结束！");
            g.drawString(t, 40, 30, 20);
            return;
        }
    }

    private void drawScore(Graphics buffer, int seat, Card[] cards,
            String name, int x1, int y1)
    {
        buffer.setColor(0x000000);
        String str = name + ":";
        buffer.drawString(str, x1, y1, 20);
        Font font = Font.getDefaultFont();
        int x = x1 + font.charsWidth(str.toCharArray(), 0, str.length());
        int y = y1;
        for (int i = 0; i < cards.length; i++)
        {
            if (cards[i] != null)
            {
                buffer.drawImage(cards[i].getImage(), x, y, 20);
                x += 8;
            }
        }
        buffer.drawString(String.valueOf(String.valueOf((new StringBuffer(""))
                .append(getScore(seat)).append("/").append(
                        scoreAmount.get(name)))), x + 7, y, 20);
    }

    public void gameOver()
    {
        int[] SCORE1 =
        { 10, 50 };
        int[] SCORE2 =
        { 10, 78 };
        int[] SCORE3 =
        { 10, 106 };
        int[] SCORE4 =
        { 10, 134 };
        backImage = Image.createImage(panel);
        drawTitle(scoreBuffer);
        drawScore(scoreBuffer, 0, scores[0], playerName[0], SCORE1[0],
                SCORE1[1]);
        drawScore(scoreBuffer, 1, scores[1], playerName[1], SCORE2[0],
                SCORE2[1]);
        drawScore(scoreBuffer, 2, scores[2], playerName[2], SCORE3[0],
                SCORE3[1]);
        drawScore(scoreBuffer, 3, scores[3], playerName[3], SCORE4[0],
                SCORE4[1]);

        bufferg.drawImage(scoreImage, WIDTH / 2, HEIGHT / 2, Graphics.HCENTER
                | Graphics.VCENTER);
        gameOver = true;

        repaint();
        addCommand(start);
        addCommand(exitCmd);
        return;
    }

    public int getScore(int seat)
    {
        int rtn = 0;

        for (int i = 0; i < scores[seat].length; i++)
        {
            if (scores[seat][i] != null)
            {
                rtn = (i >= 13) ? (rtn + 13) : rtn + 1;
            }
        }
        return rtn;
    }

    private void drawInfo(Graphics g)
    {
        int w = infoImage.getWidth();
        int h = infoImage.getHeight();
        int x = (WIDTH - w) / 2;
        int y = (HEIGHT - h) / 2;

        g.drawImage(infoImage, WIDTH / 2, HEIGHT / 2, Graphics.HCENTER
                | Graphics.VCENTER);
        g.drawImage(playerImg[seatPos % 2], x + 5, y + 5, Graphics.LEFT
                | Graphics.TOP);
        Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_ITALIC,
                Font.SIZE_MEDIUM);
        g.setFont(f);
        g.drawString("昵  称：" + "player" + seatPos, x + 65, y + 30,
                Graphics.LEFT | Graphics.TOP);
        g.drawString("端  口：" + client.getPort(), x + 65, y + 60, Graphics.LEFT
                | Graphics.TOP);
        g.drawString("桌  号：" + desknum, x + 65, y + 90, Graphics.LEFT
                | Graphics.TOP);
        g.drawString("座  号：" + seatPos, x + 65, y + 120, Graphics.LEFT
                | Graphics.TOP);
    }
}
