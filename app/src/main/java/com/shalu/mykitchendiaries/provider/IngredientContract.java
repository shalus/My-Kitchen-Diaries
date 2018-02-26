package com.shalu.mykitchendiaries.provider;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

public class IngredientContract {
    @DataType(INTEGER) @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";

    @DataType(TEXT)
    public static final String ING = "ing";

    @DataType(TEXT) @NotNull @References(table = KitchenDatabase.RECIPES, column = RecipeContract.TITLE)
    public static final String RECIPE = "recipe";
}
