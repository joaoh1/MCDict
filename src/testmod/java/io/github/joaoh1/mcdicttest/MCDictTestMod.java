package io.github.joaoh1.mcdicttest;

import io.github.cottonmc.mcdict.api.Dict;
import io.github.cottonmc.mcdict.api.DictManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MCDictTestMod implements ModInitializer {
    public static Dict<Block, Integer> dict = DictManager.DATA_PACK.registerBlockDict(new Identifier("mcdicttest", "testdict"), Integer.class);
    public static Item DICT_VIEWER_ITEM = new DictViewerItem(new FabricItemSettings().group(ItemGroup.MISC));

    @Override
    public void onInitialize() {
        System.out.println("The test mod is working!");
        Registry.register(Registry.ITEM, new Identifier("mcdicttest:dict_viewer"), DICT_VIEWER_ITEM);
    }
}
