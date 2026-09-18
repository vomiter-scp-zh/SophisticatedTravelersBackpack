package com.vomiter.sophtravelerspack.util;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.ResourceLocation;

import static com.vomiter.sophtravelerspack.util.BackpackTextureVertices.*;

/**
 * Builds a Sophisticated Backpack cloth sprite with the relevant portions of a
 * Traveler's Backpack texture composited onto it.
 */
public final class TravelerSophSpriteComposer {
    private static final GeneratedSpriteUtil.Vertices TRAVELERS_MAIN_NORTH_LOWER_HALF =
            new GeneratedSpriteUtil.Vertices(5, 19, 14, 22);
    private static final GeneratedSpriteUtil.Vertices TRAVELERS_MAIN_NORTH_LAST_ROW =
            new GeneratedSpriteUtil.Vertices(5, 22, 14, 22);

    private static final GeneratedSpriteUtil.Vertices TRAVELERS_MAIN_EAST_QUARTER_TO_HALF =
            new GeneratedSpriteUtil.Vertices(0, 16, 4, 18);
    private static final GeneratedSpriteUtil.Vertices TRAVELERS_MAIN_EAST_QUARTER_ROW =
            new GeneratedSpriteUtil.Vertices(0, 16, 4, 16);
    private static final GeneratedSpriteUtil.Vertices TRAVELERS_MAIN_EAST_LOWER_HALF =
            new GeneratedSpriteUtil.Vertices(0, 19, 4, 22);
    private static final GeneratedSpriteUtil.Vertices TRAVELERS_MAIN_EAST_LAST_ROW =
            new GeneratedSpriteUtil.Vertices(0, 22, 4, 22);
    private static final GeneratedSpriteUtil.Vertices TRAVELERS_MAIN_WEST_LOWER_HALF =
            new GeneratedSpriteUtil.Vertices(15, 19, 19, 22);

    private static final GeneratedSpriteUtil.Vertices SOPH_TOP_LIP_NORTH_HALF =
            new GeneratedSpriteUtil.Vertices(16, 4, 23, 6);
    private static final GeneratedSpriteUtil.Vertices SOPH_TOP_LIP_SOUTH_HALF =
            new GeneratedSpriteUtil.Vertices(16, 7, 23, 9);

    public static SpriteContents compose(
            SpriteContents sophisticatedBase,
            SpriteContents travelersAddition,
            ResourceLocation id
    ) {
        var holder = GeneratedSpriteUtil.holder(id, sophisticatedBase)
                // Main body
                .writeScaled(travelersAddition, TRAVELERS_MAIN_BODY_SOUTH,
                        SOPH_BACKPACK_CLOTH_BODY_NORTH)
                .writeScaled(TRAVELERS_MAIN_BODY_NORTH,
                        SOPH_BACKPACK_CLOTH_BODY_SOUTH)
                .writeScaled(TRAVELERS_MAIN_BODY_WEST,
                        SOPH_BACKPACK_CLOTH_BODY_EAST)
                .writeScaled(TRAVELERS_MAIN_BODY_EAST,
                        SOPH_BACKPACK_CLOTH_BODY_WEST)
                .writeScaled(TRAVELERS_MAIN_BODY_DOWN,
                        SOPH_BACKPACK_CLOTH_BODY_DOWN)
                .writeScaled(TRAVELERS_TOP_UP,
                        SOPH_BACKPACK_CLOTH_BODY_UP)

                // Front pouch. Its outward face is north in the model.
                .writeScaled(TRAVELERS_MAIN_NORTH_LOWER_HALF,
                        SOPH_BACKPACK_CLOTH_FRONT_POUCH_TOP_NORTH)
                .writeScaled(TRAVELERS_MAIN_EAST_LOWER_HALF,
                        SOPH_BACKPACK_CLOTH_FRONT_POUCH_TOP_WEST)
                .writeScaled(TRAVELERS_MAIN_WEST_LOWER_HALF,
                        SOPH_BACKPACK_CLOTH_FRONT_POUCH_TOP_EAST)
                .writeScaled(TRAVELERS_POCKET_FACE_UP,
                        SOPH_BACKPACK_CLOTH_FRONT_POUCH_TOP_UP)
                .writeScaled(TRAVELERS_MAIN_NORTH_LAST_ROW,
                        SOPH_BACKPACK_CLOTH_FRONT_POUCH_BOTTOM_NORTH)
                .writeScaled(TRAVELERS_MAIN_BODY_DOWN,
                        SOPH_BACKPACK_CLOTH_FRONT_POUCH_BOTTOM_DOWN)

                // Right upper pouch, viewed from the rear.
                .writeScaled(TRAVELERS_MAIN_EAST_QUARTER_TO_HALF,
                        SOPH_BACKPACK_CLOTH_RIGHT_POUCH_WEST)

                // Right lower pouch: top cube, then bottom cube.
                .writeScaled(TRAVELERS_TOP_UP,
                        SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_TOP_UP)
                .writeScaled(TRAVELERS_MAIN_EAST_LOWER_HALF,
                        SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_TOP_WEST)
                .writeScaled(TRAVELERS_MAIN_EAST_LOWER_HALF,
                        SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_TOP_SOUTH)
                .writeScaled(TRAVELERS_MAIN_EAST_LOWER_HALF,
                        SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_TOP_NORTH)


                .writeScaled(TRAVELERS_MAIN_EAST_LOWER_HALF,
                        SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_BOTTOM_SOUTH)
                .writeScaled(TRAVELERS_MAIN_EAST_LOWER_HALF,
                        SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_BOTTOM_NORTH)
                .writeScaled(TRAVELERS_MAIN_EAST_LAST_ROW,
                        SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_BOTTOM_WEST)
                .writeScaled(TRAVELERS_MAIN_BODY_DOWN,
                        SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_BOTTOM_DOWN)


                // Top lip: write the pocket face
                .writeScaled(
                        TRAVELERS_POCKET_FACE_SOUTH,
                        SOPH_BACKPACK_CLOTH_TOP_LIP_NORTH
                )
                .writeScaled(
                        TRAVELERS_POCKET_FACE_UP,
                        SOPH_TOP_LIP_NORTH_HALF
                )
                .writeScaled(
                        TRAVELERS_TOP_UP,
                        SOPH_TOP_LIP_SOUTH_HALF
                )
                ;

        holder.clearWhereTransparent(sophisticatedBase);
        return holder.build();
    }
}
