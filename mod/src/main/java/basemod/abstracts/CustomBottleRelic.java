package basemod.abstracts;

import com.megacrit.cardcrawl.cards.AbstractCard;

import java.util.function.Predicate;

public interface CustomBottleRelic
{
	// Utilized for making relics appear on the Top Right of cards. Does nothing else in terms of "Bottles"
	Predicate<AbstractCard> isOnCard();

	// Base game bottles do not show in combat. It is set to default true however.
	default boolean showInCombat(AbstractCard card) {
 		return true;
	}
}
