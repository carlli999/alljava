import javax.microedition.lcdui.Image;

public class Card
{
    private int card_nbr;

    private int card_index;

    private int suit_index;

    private Image image;

    public Card(int nbr)
    {
        card_nbr = nbr;
        card_index = card_nbr % 13;
        suit_index = card_nbr / 13;
    }

    public int getCardIndex()
    {
        return card_index;
    }

    public int getSuitIndex()
    {
        return suit_index;
    }

    public int getValue()
    {
        return card_nbr;
    }

    public Image getImage()
    {
        if (image == null)
        {
            try
            {
                image = Image.createImage("/cards/" + (card_nbr + 1) + ".png");
            }
            catch (Exception exception)
            {
            }
        }
        return image;
    }
}