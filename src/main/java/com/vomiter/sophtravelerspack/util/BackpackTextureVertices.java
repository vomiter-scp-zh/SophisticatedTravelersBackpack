package com.vomiter.sophtravelerspack.util;

import com.vomiter.sophtravelerspack.util.GeneratedSpriteUtil.Vertices;

public final class BackpackTextureVertices {
    private BackpackTextureVertices() {
    }

    /** Blockbench cube bounds, in model-space units. */
    public record Cube(float fromX, float fromY, float fromZ,
                       float toX, float toY, float toZ) {
    }

    // Traveler's Backpack: MainBody
    public static final Vertices TRAVELERS_MAIN_BODY_NORTH = new Vertices(5, 14, 14, 22);
    public static final Vertices TRAVELERS_MAIN_BODY_EAST = new Vertices(0, 14, 4, 22);
    public static final Vertices TRAVELERS_MAIN_BODY_SOUTH = new Vertices(20, 14, 29, 22);
    public static final Vertices TRAVELERS_MAIN_BODY_WEST = new Vertices(15, 14, 19, 22);

    /**
     * The source model uses UV [15, 9.02, 25, 14.02] for this face. This is its
     * nearest whole-pixel representation; the 0.02 UV offset cannot be stored
     * in {@link Vertices}.
     */
    public static final Vertices TRAVELERS_MAIN_BODY_DOWN = new Vertices(15, 9, 24, 13);

    // MainBody.up is a 1x1 placeholder UV [0, 0, 1, 1], so it is intentionally omitted.

    // Traveler's Backpack: Top
    // The source model contains two identical Top cubes; these constants apply to both.
    public static final Vertices TRAVELERS_TOP_NORTH = new Vertices(5, 5, 14, 7);
    public static final Vertices TRAVELERS_TOP_EAST = new Vertices(0, 5, 4, 7);
    public static final Vertices TRAVELERS_TOP_SOUTH = new Vertices(20, 5, 29, 7);
    public static final Vertices TRAVELERS_TOP_WEST = new Vertices(15, 5, 19, 7);
    public static final Vertices TRAVELERS_TOP_UP = new Vertices(14, 4, 5, 0);

    // Top.down is a 1x1 placeholder UV [0, 0, 1, 1], so it is intentionally omitted.

    // Traveler's Backpack: Bottom
    public static final Vertices TRAVELERS_BOTTOM_NORTH = new Vertices(4, 38, 13, 38);
    public static final Vertices TRAVELERS_BOTTOM_EAST = new Vertices(0, 38, 3, 38);
    public static final Vertices TRAVELERS_BOTTOM_SOUTH = new Vertices(18, 38, 27, 38);
    public static final Vertices TRAVELERS_BOTTOM_WEST = new Vertices(14, 38, 17, 38);
    public static final Vertices TRAVELERS_BOTTOM_DOWN = new Vertices(14, 34, 23, 37);

    // Bottom.up is a 1x1 placeholder UV [0, 0, 1, 1], so it is intentionally omitted.

    // Traveler's Backpack: PocketFace
    // The source model contains two identical PocketFace cubes; these constants apply to both.
    public static final Vertices TRAVELERS_POCKET_FACE_EAST = new Vertices(0, 26, 1, 31);
    public static final Vertices TRAVELERS_POCKET_FACE_SOUTH = new Vertices(12, 26, 19, 31);
    public static final Vertices TRAVELERS_POCKET_FACE_WEST = new Vertices(10, 26, 11, 31);
    public static final Vertices TRAVELERS_POCKET_FACE_UP = new Vertices(2, 24, 9, 25);
    public static final Vertices TRAVELERS_POCKET_FACE_DOWN = new Vertices(17, 24, 10, 25);

    // PocketFace.north is a 1x1 placeholder UV [0, 0, 1, 1], so it is intentionally omitted.

    /**
     * Sophisticated Backpack's {@code body} cube using {@code backpack_cloth.png}.
     *
     * <p>Its six face constants below all belong to this single cube; unlike the
     * Traveler's model, these are the complete set of meaningful cloth faces.</p>
     */
    public static final Cube SOPH_BACKPACK_CLOTH_BODY_CUBE = new Cube(
            3.0F, 0.0F, 5.0F,
            13.0F, 13.0F, 11.0F
    );

    public static final Vertices SOPH_BACKPACK_CLOTH_BODY_NORTH = new Vertices(0, 0, 9, 12);
    public static final Vertices SOPH_BACKPACK_CLOTH_BODY_EAST = new Vertices(10, 0, 15, 12);
    public static final Vertices SOPH_BACKPACK_CLOTH_BODY_SOUTH = new Vertices(0, 13, 9, 25);
    public static final Vertices SOPH_BACKPACK_CLOTH_BODY_WEST = new Vertices(10, 13, 15, 25);
    public static final Vertices SOPH_BACKPACK_CLOTH_BODY_UP = new Vertices(0, 26, 9, 31);
    public static final Vertices SOPH_BACKPACK_CLOTH_BODY_DOWN = new Vertices(10, 26, 19, 31);

    /**
     * Front pouch lid ({@code top} in the source model) using
     * {@code backpack_cloth.png}.
     */
    public static final Cube SOPH_BACKPACK_CLOTH_FRONT_POUCH_TOP_CUBE = new Cube(
            4.0F, 2.0F, 3.0F,
            12.0F, 6.0F, 5.0F
    );

    public static final Vertices SOPH_BACKPACK_CLOTH_FRONT_POUCH_TOP_NORTH = new Vertices(24, 0, 31, 3);
    public static final Vertices SOPH_BACKPACK_CLOTH_FRONT_POUCH_TOP_EAST = new Vertices(24, 6, 25, 9);
    public static final Vertices SOPH_BACKPACK_CLOTH_FRONT_POUCH_TOP_WEST = new Vertices(26, 6, 27, 9);
    public static final Vertices SOPH_BACKPACK_CLOTH_FRONT_POUCH_TOP_UP = new Vertices(24, 4, 31, 5);

    // Front pouch top.south and top.down have no backpack_cloth texture in the source model.

    /**
     * Lower front pouch ({@code bottom} in the source model) using
     * {@code backpack_cloth.png}.
     */
    public static final Cube SOPH_BACKPACK_CLOTH_FRONT_POUCH_BOTTOM_CUBE = new Cube(
            4.0F, 0.0F, 3.0F,
            12.0F, 1.0F, 5.0F
    );

    public static final Vertices SOPH_BACKPACK_CLOTH_FRONT_POUCH_BOTTOM_NORTH = new Vertices(24, 10, 31, 10);
    public static final Vertices SOPH_BACKPACK_CLOTH_FRONT_POUCH_BOTTOM_EAST = new Vertices(28, 6, 29, 6);
    public static final Vertices SOPH_BACKPACK_CLOTH_FRONT_POUCH_BOTTOM_WEST = new Vertices(30, 6, 31, 6);
    public static final Vertices SOPH_BACKPACK_CLOTH_FRONT_POUCH_BOTTOM_DOWN = new Vertices(24, 11, 31, 12);

    // Front pouch bottom.south and bottom.up have no backpack_cloth texture in the source model.

    /**
     * Sophisticated Backpack's upper {@code lip} cube using
     * {@code backpack_cloth.png}.
     */
    public static final Cube SOPH_BACKPACK_CLOTH_TOP_LIP_CUBE = new Cube(
            4.25F, 9.25F, 4.75F,
            11.75F, 13.25F, 10.75F
    );

    public static final Vertices SOPH_BACKPACK_CLOTH_TOP_LIP_NORTH = new Vertices(16, 0, 23, 3);

    public static final Vertices SOPH_BACKPACK_CLOTH_TOP_LIP_SOUTH = new Vertices(16, 6, 23, 9);

    public static final Vertices SOPH_BACKPACK_CLOTH_TOP_LIP_UP = new Vertices(16, 4, 23, 9);

    public static final Cube SOPH_BACKPACK_CLOTH_LEFT_POUCH_CUBE = new Cube(
            13.0F, 7.0F, 5.5F,
            14.0F, 11.0F, 10.5F
    );

    public static final Vertices SOPH_BACKPACK_CLOTH_LEFT_POUCH_NORTH = new Vertices(27, 13, 27, 16);
    public static final Vertices SOPH_BACKPACK_CLOTH_LEFT_POUCH_EAST = new Vertices(31, 13, 27, 16);
    public static final Vertices SOPH_BACKPACK_CLOTH_LEFT_POUCH_SOUTH = new Vertices(31, 13, 31, 16);
    public static final Vertices SOPH_BACKPACK_CLOTH_LEFT_POUCH_UP = new Vertices(31, 13, 27, 13);
    public static final Vertices SOPH_BACKPACK_CLOTH_LEFT_POUCH_DOWN = new Vertices(31, 16, 27, 16);

    // Left pouch.west has no backpack_cloth texture in the source model.

    public static final Cube SOPH_BACKPACK_CLOTH_RIGHT_POUCH_CUBE = new Cube(
            2.0F, 7.0F, 5.5F,
            3.0F, 11.0F, 10.5F
    );

    public static final Vertices SOPH_BACKPACK_CLOTH_RIGHT_POUCH_NORTH = new Vertices(27, 13, 27, 16);
    public static final Vertices SOPH_BACKPACK_CLOTH_RIGHT_POUCH_SOUTH = new Vertices(31, 13, 31, 16);
    public static final Vertices SOPH_BACKPACK_CLOTH_RIGHT_POUCH_WEST = new Vertices(27, 13, 31, 16);
    public static final Vertices SOPH_BACKPACK_CLOTH_RIGHT_POUCH_UP = new Vertices(27, 13, 31, 13);
    public static final Vertices SOPH_BACKPACK_CLOTH_RIGHT_POUCH_DOWN = new Vertices(27, 16, 31, 16);

    public static final Cube SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_TOP_CUBE = new Cube(
            1.0F, 2.0F, 5.5F,
            3.0F, 6.0F, 10.5F
    );
    public static final Vertices SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_TOP_NORTH = new Vertices(16, 15, 17, 18);
    public static final Vertices SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_TOP_SOUTH = new Vertices(16, 19, 17, 22);
    public static final Vertices SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_TOP_WEST = new Vertices(18, 10, 22, 13);
    public static final Vertices SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_TOP_UP = new Vertices(16, 10, 17, 14);

    public static final Cube SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_BOTTOM_CUBE = new Cube(
            1.0F, 0.0F, 5.5F,
            3.0F, 1.0F, 10.5F
    );
    public static final Vertices SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_BOTTOM_NORTH = new Vertices(18, 15, 19, 15);
    public static final Vertices SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_BOTTOM_SOUTH = new Vertices(20, 15, 21, 15);
    public static final Vertices SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_BOTTOM_WEST = new Vertices(18, 14, 22, 14);
    public static final Vertices SOPH_BACKPACK_CLOTH_RIGHT_BOTTOM_POUCH_BOTTOM_DOWN = new Vertices(18, 16, 19, 20);

}
