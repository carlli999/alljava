import java.util.Vector;

public class Desk
{
    private int NUM = 4;// �������
    private int ID;// ����ID
    private Player[] players;// �������
    private Score scoreBanker = null;
    private Vector deskCards;
    private Vector tempRound;
    private Card[][] cards;
    private Card[][] scores;
    public String[] playerName = new String[NUM];
    private Player banker = null; // ׯ��
    private int bankerID = 0; // ׯ��ID
    private int game = 0; // ��Ϸ����
    private int roundNum = 0;// ����
    private int score = 0;// ���� ��ʱû�õ�
    Server server;

    public Desk()
    {
        game = 0;
        roundNum = 0;
        scoreBanker = new Score();
        deskCards = new Vector();
        tempRound = new Vector(4);
        server = new Server();
        players = new Player[NUM];

        for (int i = 0; i < NUM; i++)
        {
            playerName[i] = "p" + i;
            players[i] = null;
        }
        banker = null;
        bankerID = 0;
        cards = new Card[NUM][13];
        scores = new Card[NUM][14];
        init();
    }

    /**
     * ��ʼ��***********
     */
    public void init()
    {
        banker = null; // ׯ��
        bankerID = 0;
        game = 0;
        reset();
    }

    public void reset()
    {
        deskCards.clear();
        tempRound.clear();
        for (int i = 0; i < 52; i++)
        {
            deskCards.add(i + "");
        }
        for (int i = 0; i < NUM; i++)
        {
            if (players[i] != null)
                players[i].reset();
        }
        cards = new Card[NUM][13];
        scores = new Card[NUM][14];
        roundNum = 0;
    }

    public void start()
    {// ����ȫ�ֿ�ʼ
        reset();
        sendCards();
        startBanker();
    }

    /** **************************������ҵ�񷽷�************************************** */

    public void sendCards()// ����
    {
        String message = "";
        int rand = -1;
        int cardNum = -1;
        for (int i = 0; i < NUM; i++)// ����
        {
            message = "";
            for (int j = 0; j < 13; j++)
            {
                rand = (int) (Math.random() * deskCards.size() % deskCards
                        .size());
                cardNum = Integer.parseInt((String) deskCards.remove(rand));
                if (cardNum == 0)
                {
                    banker = players[i];
                    bankerID = i;
                }
                cards[i][j] = new Card(cardNum);
                message += "," + cards[i][j].getValue();
            }
            sendMessage(players[i], "deliver" + message);
        }
    }

    public void startBanker()
    {
        sendMessageToAll("banker:" + bankerID);
        sendMessage(banker, "turn");
    }

    public void tryShow(String message)
    {
        int nextID;
        int index0 = message.indexOf(":");
        int index1 = message.indexOf(",");
        int seat = Integer.parseInt(message.substring(index0 + 1, index1));
        int card = Integer.parseInt(message.substring(index1 + 1));
        tempRound.add(new Card(card));

        for (int i = 0; i < cards[seat].length; i++)
        {
            if (cards[seat][i] != null && cards[seat][i].getValue() == card)
            {
                cards[seat][i] = null;
                break;
            }
        }
        sendMessageToOther(players[seat], message);
        if ((nextID = check(seat)) == -1)
            sendMessage(players[(seat + 1) % NUM], "turn");
        else if (nextID != -2)
            sendMessage(players[(nextID + seat + 1) % NUM], "turn");
    }

    private int check(int seat)
    {
        int maxID = -1;
        if (tempRound.size() < 4)
        {
            return -1;
        }

        Card max = null;

        for (int i = 0; i < tempRound.size(); i++)
        {
            Card c = (Card) tempRound.elementAt(i);
            if (i == 0)
            {
                max = c;
                maxID = i;
            }
            if ((c.getSuitIndex() == max.getSuitIndex())
                    && (c.getCardIndex() > max.getCardIndex()))
            {
                max = c;
                maxID = i;
            }
        }

        for (int i = 0; i < tempRound.size(); i++)
        {
            Card c = (Card) tempRound.elementAt(i);

            if ((c.getSuitIndex() == 3)
                    || ((c.getSuitIndex() == 2) && (c.getCardIndex() == 10)))
            {
                score(c, (maxID + seat + 1) % NUM);
            }
        }
        roundNum++;
        if (roundNum >= 13)
        {
            roundNum = 0;
            gameover();
            maxID = -2;
        }
        tempRound.clear();
        return maxID;
    }

    public void score(Card c, int ID)
    {
        if ((c.getSuitIndex() == 2) && (c.getCardIndex() == 10))
        {
            scores[ID][13] = c;
        }
        else
        {
            scores[ID][c.getCardIndex()] = c;
        }
    }

    private void gameover()
    {
        scoreBanker.set(this);
        String message = "GameOver";
        for (int i = 0; i < NUM; i++)
        {
            for (int j = 0; j < scores[i].length; j++)
            {
                if (scores[i][j] != null)
                    message += "," + scores[i][j].getValue();
                else
                    message += ",-1";
            }
        }
        for (int i = 0; i < NUM; i++)
            message += ":" + scoreBanker.get(playerName[i]);
        sendMessageToAll(message);
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

    /** **************************������ҵ�񷽷�************************************** */
    public int getID()
    {
        return ID;
    }

    public void setID(int id)
    {
        ID = id;
    }

    public Player getBanker()
    {
        return banker;
    }

    public void setBanker(Player banker)
    {
        this.banker = banker;
    }

    public int getGame()
    {
        return game;
    }

    public boolean isReady() // �����Ƿ����������Ҷ���ʼ
    {
        for (int i = 0; i < NUM; i++)
        {
            if (players[i] == null)
                return false;
            else if (!players[i].isStart())
                return false;
        }
        return true;
    }

    public boolean isEmpty(int pos)
    {// ����pos�Ƿ��
        if (pos >= NUM)
            return false;
        return players[pos] == null;
    }

    public int getPlayerSeat(Player p) // ���������λ
    {
        for (int i = 0; i < NUM; i++)
        {
            if (players[i] == null)
                continue;
            if (players[i].equals(p))
                return i;
        }
        return -1;
    }

    public void setPlayer(int pos, Player n)
    {// �趨���n����pos��λ��
        if (pos >= NUM)
            return;
        for (int i = 0; i < NUM; i++)
            if (players[i]!=null&&players[i].equals(n))
                return;
        players[pos] = n;
    }
    
    public int getBankerID()
    {
        return bankerID;
    }

    public int getPlayersCounter()
    {// ����������
        return NUM;
    }

    public Player[] getPlayers()
    {
        return players;
    }

    public void removePlayer(Player p)
    {// �Ƴ����p
        for (int i = 0; i < NUM; i++)
        {
            if (players[i] == null)
                continue;
            else if (players[i].equals(p))
                players[i] = null;
        }
    }

    public void sendMessageToAll(String mes)
    {
        for (int i = 0; i < NUM; i++)
            if (players[i] != null)
                sendMessage(players[i], mes);
    }

    public void sendMessageToOther(Player player, String message)
    {
        for (int i = 0; i < NUM; i++)
        {
            if (players[i] != null && !players[i].equals(player))
                sendMessage(players[i], message);
        }
    }

    public void sendMessage(Player p, String m)
    {
        server.sendMessage(p, m);
    }

    public void sendBankerInfo()
    {
        sendMessageToAll("bankerInfo:" + bankerID);
    }

    public void sendScoreInfo()
    {
        sendMessageToAll("scoreInfo:" + score);
    }
}
