package basemod.patches.com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;

import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToDiscardEffect;

@SpirePatch(
        clz = MakeTempCardInDiscardAction.class,
        method = "update"
)
public class MakeTempCardInDiscardPatch {
    public static void Prefix(MakeTempCardInDiscardAction __instance, int ___numCards, AbstractCard ___c, float ___startDuration, boolean ___sameUUID, @ByRef float[] ___duration) {
        if (___numCards >= 6 && ___duration[0] == ___startDuration && BaseMod.fixesEnabled) {
            for (int i = 0; i < ___numCards; i++) {
                AbstractDungeon.effectList.add(new ShowCardAndAddToDiscardEffect(makeNewCard(___c,___sameUUID)));
            }
            ___duration[0] -= Gdx.graphics.getDeltaTime();
        }
    }

    private static AbstractCard makeNewCard(AbstractCard card, boolean sameUUID) {
        if(sameUUID) return card.makeSameInstanceOf();
        return card.makeStatEquivalentCopy();
    }
}
