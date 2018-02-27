package com.shalu.mykitchendiaries.provider;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

public class RecipeContract {
    @DataType(INTEGER) @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";

    @DataType(TEXT) @NotNull
    public static final String TITLE = "title";

    @DataType(TEXT)
    public static final String IMAGE_PATH = "image";

    @DataType(TEXT)
    @NotNull
    public static final String COLUMN_DATE = "date";
}




