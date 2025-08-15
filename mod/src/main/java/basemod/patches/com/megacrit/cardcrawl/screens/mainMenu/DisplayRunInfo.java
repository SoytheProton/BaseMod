package basemod.patches.com.megacrit.cardcrawl.screens.mainMenu;

import basemod.ReflectionHacks;
import basemod.helpers.ColorMarkupNames;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.saveAndContinue.SaveAndContinue;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.screens.DungeonTransitionScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.screens.mainMenu.MenuButton;
import com.megacrit.cardcrawl.screens.options.ConfirmPopup;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

public class DisplayRunInfo
{
	private static final Color BG_COLOR = Color.BLACK.cpy();
	private static final Color FG_COLOR = Settings.CREAM_COLOR.cpy();
	private static final float PADDING = 15 * Settings.scale;
	private static final float MIN_BOX_WIDTH = 300 * Settings.scale;

	private static final UIStrings strings = CardCrawlGame.languagePack.getUIString("basemod:ContinueRunInfo");

	private static String characterName;
	private static SaveFile save;

	@SpirePatch2(
			clz = MenuButton.class,
			method = SpirePatch.CONSTRUCTOR
	)
	public static class Load
	{
		public static void Postfix(MenuButton __instance)
		{
			if (__instance.result == MenuButton.ClickResult.RESUME_GAME) {
				loadSaveFile();
			}
		}
	}

	@SpirePatch2(
			clz = MainMenuScreen.class,
			method = "render"
	)
	public static class Render
	{
		private static float infoX = 0f;
		private static float infoTargetX = 0f;

		@SpireInsertPatch(
				locator = Locator.class
		)
		public static void Insert(MainMenuScreen __instance, SpriteBatch sb)
		{
			if (save == null) return;

			Stream<MenuButton> buttons = __instance.buttons.stream()
					.filter(x -> x.result == MenuButton.ClickResult.RESUME_GAME || x.result == MenuButton.ClickResult.ABANDON_RUN);

			infoX = MathHelper.uiLerpSnap(infoX, infoTargetX);

			if (buttons.anyMatch(x -> x.hb.hovered)) {
				infoTargetX = 25;
			} else {
				infoTargetX = 0f;
			}

			float alpha = Interpolation.linear.apply(0f, 1f, infoX / 25f);
			if (alpha < 0) {
				alpha = 0f;
			}
			BG_COLOR.a = alpha * 0.6f;
			FG_COLOR.a = alpha;
			ColorMarkupNames.setAlpha(alpha);

			if (alpha > 0f) {
				StringBuilder textBuilder = new StringBuilder();
				textBuilder.append(String.format(
						strings.TEXT[0],
						characterName,
						getGameMode(save)
				)).append('\n');
				textBuilder.append(String.format(
						strings.TEXT[1],
						getActName(save),
						save.floor_num,
						save.ascension_level
				)).append('\n');
				if (save.is_ascension_mode) {
					textBuilder.append(String.format(
							strings.TEXT[2],
							save.ascension_level
					)).append('\n');
				}
				textBuilder.append(String.format(
						strings.TEXT[3],
						save.current_health,
						save.max_health,
						save.gold
				));
				String text = textBuilder.toString();

				MenuButton topButton = CardCrawlGame.mainMenuScreen.buttons.get(CardCrawlGame.mainMenuScreen.buttons.size() - 1);

				float w = FontHelper.getWidth(FontHelper.cardDescFont_L, text, 1f) + PADDING * 2;
				w = Math.max(w, MIN_BOX_WIDTH);
				float h = FontHelper.getHeight(FontHelper.cardDescFont_L, text, 1f) + PADDING * 2;
				float x = infoX * Settings.scale + MenuButton.FONT_X + (-179f + 120f + 32f) * Settings.scale;
				float y = topButton.hb.y + topButton.hb.height + 10 * Settings.scale;

				sb.setColor(BG_COLOR);
				sb.draw(
						ImageMaster.WHITE_SQUARE_IMG,
						x,
						y,
						w,
						h
				);

				FontHelper.renderFont(
						sb,
						FontHelper.cardDescFont_L,
						text,
						x + PADDING,
						y + h - PADDING,
						FG_COLOR
				);
			}

			ColorMarkupNames.setAlpha(1f);
		}

		public static class Locator extends SpireInsertLocator
		{
			@Override
			public int[] Locate(CtBehavior ctBehavior) throws Exception {
				Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(ConfirmPopup.class, "render");
				return LineFinder.findInOrder(ctBehavior, matcher);
			}
		}
	}

	private static void loadSaveFile()
	{
		for (AbstractPlayer character : CardCrawlGame.characterManager.getAllCharacters()) {
			String filepath = SaveAndContinue.getPlayerSavePath(character.chosenClass);

			try {
				save = ReflectionHacks.privateStaticMethod(
						SaveAndContinue.class,
						"loadSaveFile",
						String.class
				).invoke(new Object[] { filepath });
				characterName = character.getLocalizedCharacterName();
				return;
			} catch (Exception ignore) {}
		}
	}

	private static final String GAMEMODE_FORMAT = "- %s";
	private static String getGameMode(SaveFile save)
	{
		if (save.is_daily) {
			return String.format(GAMEMODE_FORMAT, String.format(strings.EXTRA_TEXT[1], dateFormat(save.daily_date)));
		} else if (save.is_endless_mode) {
			return String.format(GAMEMODE_FORMAT, strings.EXTRA_TEXT[2]);
		} else if (save.is_trial) {
			return String.format(GAMEMODE_FORMAT, strings.EXTRA_TEXT[3]);
		}
		return strings.EXTRA_TEXT[0];
	}

	private static final DateFormat dateFormat =
			new SimpleDateFormat(CardCrawlGame.languagePack.getUIString("DailyScreen").TEXT[17]);
	private static String dateFormat(long dailyDate)
	{
		return dateFormat.format(new Date(dailyDate * 86400L * 1000L));
	}

	private static String getActName(SaveFile save)
	{
		String origName = AbstractDungeon.name;
		String origLevelNum = AbstractDungeon.levelNum;
		FixForSayTheSpire.disablePopup = true;
		new DungeonTransitionScreen(save.level_name);
		FixForSayTheSpire.disablePopup = false;
		String ret = AbstractDungeon.name;
		AbstractDungeon.name = origName;
		AbstractDungeon.levelNum = origLevelNum;
		return ret;
	}

	@SpirePatch2(
			clz = DungeonTransitionScreen.class,
			method = SpirePatch.CONSTRUCTOR
	)
	private static class FixForSayTheSpire
	{
		public static boolean disablePopup = false;

		private static ExprEditor Instrument()
		{
			return new ExprEditor() {
				@Override
				public void edit(NewExpr e) throws CannotCompileException
				{
					if (e.getClassName().equals(ConfirmPopup.class.getName())) {
						e.replace("if (" + FixForSayTheSpire.class.getName() + ".disablePopup) {" +
								"$_ = null;" +
								"} else {" +
								"$_ = $proceed($$);" +
								"}");
					}
				}

				@Override
				public void edit(MethodCall m) throws CannotCompileException
				{
					if (m.getClassName().equals(ConfirmPopup.class.getName()) && m.getMethodName().equals("show")) {
						m.replace("if ($0 != null) { $proceed($$); }");
					}
				}
			};
		}
	}
}
