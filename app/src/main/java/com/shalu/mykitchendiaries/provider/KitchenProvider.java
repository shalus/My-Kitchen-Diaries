package com.shalu.mykitchendiaries.provider;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(
        authority = KitchenProvider.AUTHORITY,
        database = KitchenDatabase.class)
public class KitchenProvider {

        public static final String AUTHORITY = "com.shalu.kitchendiaries.provider";


        @TableEndpoint(table = KitchenDatabase.RECIPES)
        public static class Recipes {

            @ContentUri(
                    path = "recipes",
                    type = "vnd.android.cursor.dir/recipes",
                    defaultSort = RecipeContract.COLUMN_DATE + " DESC")
            public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/recipes");
        }

    @TableEndpoint(table = KitchenDatabase.INGREDIENTS)
    public static class Ingredients {

        @ContentUri(
                path = "ingredients",
                type = "vnd.android.cursor.dir/ingredients")
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/ingredients");
    }

    @TableEndpoint(table = KitchenDatabase.STEPS)
    public static class Steps {

        @ContentUri(
                path = "steps",
                type = "vnd.android.cursor.dir/steps")
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/steps");
    }

    @TableEndpoint(table = KitchenDatabase.LOCATION)
    public static class Location {

        @ContentUri(
                path = "location",
                type = "vnd.android.cursor.dir/location")
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/location");
    }

    @TableEndpoint(table = KitchenDatabase.SHOPPING)
    public static class Shopping {

        @ContentUri(
                path = "shopping",
                type = "vnd.android.cursor.dir/shopping")
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/shopping");
    }
}
