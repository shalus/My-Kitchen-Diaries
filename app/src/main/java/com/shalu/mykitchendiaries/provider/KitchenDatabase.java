package com.shalu.mykitchendiaries.provider;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.ExecOnCreate;
import net.simonvt.schematic.annotation.Table;

@Database(version = KitchenDatabase.VERSION)
public class KitchenDatabase {



        public static final int VERSION = 1;

        @Table(RecipeContract.class)
        public static final String RECIPES = "recipes";

        @Table(IngredientContract.class)
        public static final String INGREDIENTS = "ingredients";

        @Table(StepContract.class)
        public static final String STEPS = "steps";

        @Table(LocationContract.class)
        public static final String LOCATION = "location";

        @Table(ShoppingContract.class)
        public static final String SHOPPING = "shopping";

        @ExecOnCreate
        public static final String EXEC_ON_CREATE = "PRAGMA foreign_keys = ON;";
}
