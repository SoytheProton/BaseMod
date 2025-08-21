package basemod.patches.com.megacrit.cardcrawl.cards.AbstractCard;

import basemod.BaseMod;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.TipHelper;
import javassist.CtBehavior;

import java.util.ArrayList;
import java.util.List;

public class MultiwordKeywords
{
	@SpirePatch(
			clz=AbstractCard.class,
			method="initializeDescription"
	)
	public static class InitializeDescription
	{
		@SpireInsertPatch(
				locator=Locator.class,
				localvars={"word", "keywordTmp"}
		)
		public static void Insert(AbstractCard __instance, @ByRef String[] word, String keywordTmp)
		{
			if (word[0].contains("_") && !keywordTmp.contains("_")) {
				String tmp = word[0].replace('_', ' ');
				StringBuilder builder = new StringBuilder();
				boolean firstWord = true;
				for (String w : tmp.split(" ")) {
					builder.append('*');
					if (!firstWord) {
						// Make colored multiword keywords work
						ColoredKeywords.InitializeDescription.appendTempColorHex(builder, keywordTmp);
					}
					builder.append(w).append(' ');
					firstWord = false;
				}
				// Trim to removing ending space
				// substring to remove starting *
				word[0] = builder.toString().trim().substring(1);
			}
		}

		private static class Locator extends SpireInsertLocator
		{
			@Override
			public int[] Locate(CtBehavior ctBehavior) throws Exception
			{
				Matcher matcher = new Matcher.MethodCallMatcher(StringBuilder.class, "append");
				List<Matcher> matchers = new ArrayList<>();
				matchers.add(matcher);
				matchers.add(matcher);
				matchers.add(matcher);
				matchers.add(matcher);
				matchers.add(matcher);
				return LineFinder.findInOrder(ctBehavior, matchers, matcher);
			}
		}
	}

	@SpirePatch(
			clz=TipHelper.class,
			method="capitalize"
	)
	public static class BetterCapitalize
	{
		public static String Replace(String input)
		{
			String tmp = BaseMod.getKeywordProper(input);
			if (tmp != null) {
				return tmp;
			}

			if (BaseMod.keywordIsUnique(input)) {
				input = BaseMod.getKeywordUnique(input);
			}

			// Capitalize each word
			StringBuilder builder = new StringBuilder();
			for (String w : input.split(" ")) {
				builder.append(w.substring(0, 1).toUpperCase()).append(w.substring(1).toLowerCase()).append(' ');
			}
			return builder.toString().trim();
		}
	}
}
