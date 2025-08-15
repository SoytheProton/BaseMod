package basemod.patches.com.megacrit.cardcrawl.cards.AbstractCard;

import basemod.helpers.ColorMarkupNames;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;

public class NamedColorsAlpha
{
	@SpirePatch2(
			clz = AbstractCard.class,
			method = "render",
			paramtypez = {SpriteBatch.class}
	)
	@SpirePatch2(
			clz = AbstractCard.class,
			method = "renderInLibrary"
	)
	public static class Begin
	{
		@SpirePrefixPatch
		public static void SetMarkupAlpha(AbstractCard __instance)
		{
			ColorMarkupNames.setAlpha(__instance.transparency);
		}

		@SpirePostfixPatch
		public static void ResetMarkupAlpha()
		{
			ColorMarkupNames.setAlpha(1f);
		}
	}
}
