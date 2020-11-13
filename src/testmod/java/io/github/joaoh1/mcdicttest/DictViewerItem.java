package io.github.joaoh1.mcdicttest;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;

public class DictViewerItem extends Item {
    public DictViewerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (!player.isSneaking()) {
            Block block = context.getPlayer().world.getBlockState(context.getBlockPos()).getBlock();
            if (MCDictTestMod.dict.contains(block)) {
                context.getPlayer().sendMessage(new LiteralText("This block is in the dict! Value: " + MCDictTestMod.dict.get(block)), true);
            } else {
                context.getPlayer().sendMessage(new LiteralText("This block isn't in the dict"), true);
            }
        } else {
            System.out.println(MCDictTestMod.dict.values());
        }
        return super.useOnBlock(context);
    }
}
