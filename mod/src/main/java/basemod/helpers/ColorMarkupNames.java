package basemod.helpers;

import basemod.BaseMod;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.utils.ObjectMap;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.Settings;

import java.util.HashMap;
import java.util.Map;

public class ColorMarkupNames
{
	private static final Map<String, Color> colors = new HashMap<>();

	static {
		addNamedColor(Settings.CREAM_COLOR, "sts:white", "sts:cream");
		addNamedColor(Settings.RED_TEXT_COLOR, "sts:red");
		addNamedColor(Settings.GREEN_TEXT_COLOR, "sts:green");
		addNamedColor(Settings.BLUE_TEXT_COLOR, "sts:blue");
		addNamedColor(Settings.GOLD_COLOR, "sts:gold", "sts:yellow");
		addNamedColor(Settings.PURPLE_COLOR, "sts:purple");
	}

	public static Color addNamedColor(Color color, String... names)
	{
		Color copy = color.cpy();
		for (String name : names) {
			if (colors.containsKey(name)) {
				BaseMod.logger.warn("Named color [" + name + "] already exists, overriding");
			}
			colors.put(name, copy);
		}
		return copy;
	}

	public static void setAlpha(float a)
	{
		for (Color color : colors.values()) {
			color.a = a;
		}
	}

	@SpirePatch2(
			clz = Colors.class,
			method = "reset"
	)
	private static class ResetPatch
	{
		@SpirePostfixPatch
		private static void Postfix(ObjectMap<String, Color> ___map)
		{
			for (Map.Entry<String, Color> entry : colors.entrySet()) {
				___map.put(entry.getKey(), entry.getValue());
			}
		}
	}
}
