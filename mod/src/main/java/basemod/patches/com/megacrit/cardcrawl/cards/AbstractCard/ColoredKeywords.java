package basemod.patches.com.megacrit.cardcrawl.cards.AbstractCard;

import basemod.BaseMod;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.screens.SingleCardViewPopup;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColoredKeywords
{
	@SpirePatch2(
			clz = AbstractCard.class,
			method = "initializeDescription"
	)
	public static class InitializeDescription
	{
		@SpireInstrumentPatch
		public static ExprEditor addTempColorHex()
		{
			return new ExprEditor()
			{
				@Override
				public void edit(MethodCall m) throws CannotCompileException
				{
					if (m.getClassName().equals(StringBuilder.class.getName()) && m.getMethodName().equals("append")) {
						try {
							CtClass[] params = m.getMethod().getParameterTypes();
							if (params.length == 1 && params[0] == CtClass.charType) {
								m.replace("$_ = " + InitializeDescription.class.getName() + ".appendTempColorHex($proceed($$), keywordTmp);");
							}
						} catch (NotFoundException e) {
							throw new RuntimeException(e);
						}
					}
				}
			};
		}

		public static StringBuilder appendTempColorHex(StringBuilder builder, String keywordTmp)
		{
			Color keywordColor = BaseMod.getKeywordColor(keywordTmp);
			if (keywordColor != null) {
				keywordColor.a = 1f;
				builder.append("{#").append(keywordColor).append('}');
			}

			return builder;
		}
	}

	@SpirePatch2(
			clz = AbstractCard.class,
			method = "renderDescription"
	)
	@SpirePatch2(
			clz = AbstractCard.class,
			method = "renderDescriptionCN"
	)
	@SpirePatch2(
			clz = SingleCardViewPopup.class,
			method = "renderDescription"
	)
	public static class Render
	{
		private static final Pattern pattern = Pattern.compile("^\\{#([0-9a-fA-F]{8})}(.*)$");
		private static Color lastFoundKeywordColor = null;

		@SpireInsertPatch(
				locator = Locator.class,
				localvars = {"tmp"}
		)
		public static void removeTempColorHex(@ByRef String[] tmp)
		{
			Matcher m = pattern.matcher(tmp[0]);
			if (m.matches()) {
				String hex = m.group(1);
				lastFoundKeywordColor = Color.valueOf(hex);
				tmp[0] = m.group(2);
			}
		}

		public static Color getColor(String keyword, Color goldColor)
		{
			if (lastFoundKeywordColor != null) {
				Color keywordColor = lastFoundKeywordColor;
				lastFoundKeywordColor = null;
				keywordColor.a = goldColor.a;
				return keywordColor;
			}
			return goldColor;
		}

		@SpireInstrumentPatch
		public static ExprEditor replaceKeywordColor()
		{
			return new ExprEditor()
			{
				@Override
				public void edit(FieldAccess f) throws CannotCompileException
				{
					if (f.getClassName().equals(AbstractCard.class.getName()) && f.getFieldName().equals("goldColor")
							|| f.getClassName().equals(Settings.class.getName()) && f.getFieldName().equals("GOLD_COLOR")
					) {
						f.replace("$_ = " + ColoredKeywords.Render.class.getName() + ".getColor(tmp, $proceed($$));");
					}
				}
			};
		}

		private static class Locator extends SpireInsertLocator
		{
			public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException
			{
				com.evacipated.cardcrawl.modthespire.lib.Matcher finalMatcher = new com.evacipated.cardcrawl.modthespire.lib.Matcher.MethodCallMatcher(GlyphLayout.class, "setText");
				return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
			}
		}
	}
}
