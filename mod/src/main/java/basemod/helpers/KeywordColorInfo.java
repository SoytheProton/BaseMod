package basemod.helpers;

import com.badlogic.gdx.graphics.Color;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

@JsonAdapter(KeywordColorInfo.KeywordColorJsonAdapter.class)
public class KeywordColorInfo
{
	public Color color;
	public boolean disableTooltipHeaderColor;

	public static class KeywordColorJsonAdapter extends TypeAdapter<KeywordColorInfo>
	{
		@Override
		public void write(JsonWriter out, KeywordColorInfo value) throws IOException
		{
			if (value == null) {
				out.nullValue();
				return;
			}

			out.beginObject();
			out.name("color");
			if (value.color == null) {
				out.nullValue();
			} else {
				out.value(value.color.toString());
			}
			out.name("disableTooltipHeaderColor");
			out.value(value.disableTooltipHeaderColor);
			out.endObject();
		}

		@Override
		public KeywordColorInfo read(JsonReader in) throws IOException
		{
			if (in.peek() == JsonToken.NULL) {
				in.nextNull();
				return null;
			}

			KeywordColorInfo ret = new KeywordColorInfo();
			if (in.peek() == JsonToken.STRING) {
				ret.color = Color.valueOf(in.nextString());
			} else {
				in.beginObject();
				while (in.hasNext()) {
					String name = in.nextName();
					switch (name) {
						case "color":
							ret.color = Color.valueOf(in.nextString());
							break;
						case "disableTooltipHeaderColor":
							ret.disableTooltipHeaderColor = in.nextBoolean();
							break;
						default:
							in.skipValue();
							break;
					}
				}
				in.endObject();
			}
			return ret;
		}
	}
}
