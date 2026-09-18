package com.vomiter.sophtravelerspack.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;

public final class GeneratedSpriteUtil {
    /**
     * 座標採閉區間 [x0, x1] × [y0, y1]。
     *
     * <p>verticesBase 必須遞增。verticesAdd 可遞增或遞減；
     * 任一軸遞減時，代表該軸鏡像。</p>
     *
     * <p>例如 16×16 的完整圖片範圍為 {@code (0, 0, 15, 15)}；
     * 水平鏡像則為 {@code (15, 0, 0, 15)}。</p>
     */
    public record Vertices(int x0, int y0, int x1, int y1) {
        public int width() {
            return Math.abs(x1 - x0) + 1;
        }

        public int height() {
            return Math.abs(y1 - y0) + 1;
        }
    }

    public static MutableSpriteHolder holder(
            ResourceLocation id,
            SpriteContents base
    ) {
        return new MutableSpriteHolder(id, base);
    }

    /**
     * 外部傳入的 base、addition 由呼叫端管理。
     *
     * <p>holder 只負責釋放自己產生、且尚未透過 build() 或 sprite()
     * 交出的 SpriteContents。</p>
     *
     * <p>已交出的結果在後續 write() 或 close() 後仍然有效；
     * 其生命週期改由呼叫端或接收它的 atlas 管理。</p>
     */
    public static final class MutableSpriteHolder implements AutoCloseable {
        private final ResourceLocation id;
        private SpriteContents sprite;

        @Nullable
        private SpriteContents addition;

        private boolean ownsSprite;
        private boolean closed;

        public MutableSpriteHolder(ResourceLocation id, SpriteContents base) {
            this.id = Objects.requireNonNull(id, "id");
            this.sprite = Objects.requireNonNull(base, "base");
        }

        public MutableSpriteHolder clearWhereTransparent(
                SpriteContents mask
        ) {
            Objects.requireNonNull(mask, "mask");

            NativeImage currentImage = this.sprite.getOriginalImage();
            NativeImage maskImage = mask.getOriginalImage();

            int width = currentImage.getWidth();
            int height = currentImage.getHeight();

            if (maskImage.getWidth() != width || maskImage.getHeight() != height) {
                throw new IllegalArgumentException(
                        "Sprite size mismatch: holder is "
                                + width + "x" + height
                                + ", mask is "
                                + maskImage.getWidth() + "x" + maskImage.getHeight()
                );
            }

            NativeImage result = new NativeImage(width, height, true);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int maskPixel = maskImage.getPixelRGBA(x, y);
                    int maskAlpha = maskPixel >>> 24;

                    if (maskAlpha == 0) {
                        result.setPixelRGBA(x, y, 0x00000000);
                    } else {
                        result.setPixelRGBA(
                                x,
                                y,
                                currentImage.getPixelRGBA(x, y)
                        );
                    }
                }
            }

            this.replaceSprite(new SpriteContents(
                    this.id,
                    new FrameSize(width, height),
                    result,
                    AnimationMetadataSection.EMPTY
            ));

            return this;
        }

        public MutableSpriteHolder write(
                SpriteContents addition,
                Vertices verticesAdd,
                Vertices verticesBase
        ) {
            ensureOpen();

            SpriteContents result = writeInternal(
                    sprite,
                    addition,
                    id,
                    verticesAdd,
                    verticesBase,
                    false
            );

            replaceSprite(result);
            this.addition = addition;
            return this;
        }

        public MutableSpriteHolder write(
                Vertices verticesAdd,
                Vertices verticesBase
        ) {
            return write(requireAddition(), verticesAdd, verticesBase);
        }

        /**
         * 將 addition 以中心對齊 nearest-neighbor 縮放後覆寫。
         */
        public MutableSpriteHolder writeScaled(
                SpriteContents addition,
                Vertices verticesAdd,
                Vertices verticesBase
        ) {
            ensureOpen();

            SpriteContents result = writeInternal(
                    sprite,
                    addition,
                    id,
                    verticesAdd,
                    verticesBase,
                    true
            );

            replaceSprite(result);
            this.addition = addition;
            return this;
        }

        public MutableSpriteHolder writeScaled(
                Vertices verticesAdd,
                Vertices verticesBase
        ) {
            return writeScaled(
                    requireAddition(),
                    verticesAdd,
                    verticesBase
            );
        }

        /**
         * 交出目前結果，holder 不再負責關閉它。
         */
        public SpriteContents build() {
            ensureOpen();
            ownsSprite = false;
            return sprite;
        }

        /**
         * 釋放尚未交出的產物，不關閉外部輸入或已交出的結果。
         */
        @Override
        public void close() {
            if (closed) {
                return;
            }

            closed = true;
            addition = null;

            if (ownsSprite) {
                ownsSprite = false;
                sprite.close();
            }
        }

        private void replaceSprite(SpriteContents result) {
            SpriteContents previous = sprite;
            boolean closePrevious = ownsSprite;

            sprite = result;
            ownsSprite = true;

            if (closePrevious) {
                previous.close();
            }
        }

        private SpriteContents requireAddition() {
            ensureOpen();

            if (addition == null) {
                throw new IllegalStateException(
                        "No addition SpriteContents has been supplied yet."
                );
            }

            return addition;
        }

        private void ensureOpen() {
            if (closed) {
                throw new IllegalStateException(
                        "MutableSpriteHolder has already been closed."
                );
            }
        }
    }

    /**
     * 中心對齊的 nearest-neighbor 縮放。
     */
    public static NativeImage resizeNearest(
            NativeImage source,
            int targetWidth,
            int targetHeight
    ) {
        if (targetWidth <= 0 || targetHeight <= 0) {
            throw new IllegalArgumentException(
                    "Target dimensions must be positive."
            );
        }

        NativeImage result = new NativeImage(
                targetWidth,
                targetHeight,
                true
        );

        boolean success = false;
        try {
            for (int y = 0; y < targetHeight; y++) {
                int sourceY = nearestSourceOffset(
                        y,
                        source.getHeight(),
                        targetHeight
                );

                for (int x = 0; x < targetWidth; x++) {
                    int sourceX = nearestSourceOffset(
                            x,
                            source.getWidth(),
                            targetWidth
                    );

                    result.setPixelRGBA(
                            x,
                            y,
                            source.getPixelRGBA(sourceX, sourceY)
                    );
                }
            }

            success = true;
            return result;
        } finally {
            if (!success) {
                result.close();
            }
        }
    }

    public static NativeImage scaleNearest(NativeImage source, int scale) {
        if (scale <= 0) {
            throw new IllegalArgumentException("Scale must be positive.");
        }

        return resizeNearest(
                source,
                Math.multiplyExact(source.getWidth(), scale),
                Math.multiplyExact(source.getHeight(), scale)
        );
    }

    private static SpriteContents writeInternal(
            SpriteContents base,
            SpriteContents addition,
            ResourceLocation id,
            Vertices verticesAdd,
            Vertices verticesBase,
            boolean scale
    ) {
        validateBaseVertices(verticesBase);

        NativeImage baseImage = base.getOriginalImage();
        NativeImage additionImage = addition.getOriginalImage();

        validateBounds(baseImage, verticesBase, false, "base");
        validateBounds(additionImage, verticesAdd, true, "addition");

        if (!scale) {
            validateEqualSize(verticesAdd, verticesBase);
        }

        NativeImage result = copyImage(baseImage);

        boolean success = false;
        try {
            int targetWidth = verticesBase.width();
            int targetHeight = verticesBase.height();
            int sourceWidth = verticesAdd.width();
            int sourceHeight = verticesAdd.height();

            for (int y = 0; y < targetHeight; y++) {
                int additionOffsetY = scale
                        ? nearestSourceOffset(y, sourceHeight, targetHeight)
                        : y;

                int sourceY = sourceCoordinate(
                        verticesAdd.y0(),
                        verticesAdd.y1(),
                        additionOffsetY
                );

                for (int x = 0; x < targetWidth; x++) {
                    int additionOffsetX = scale
                            ? nearestSourceOffset(x, sourceWidth, targetWidth)
                            : x;

                    int sourceX = sourceCoordinate(
                            verticesAdd.x0(),
                            verticesAdd.x1(),
                            additionOffsetX
                    );

                    result.setPixelRGBA(
                            verticesBase.x0() + x,
                            verticesBase.y0() + y,
                            additionImage.getPixelRGBA(sourceX, sourceY)
                    );
                }
            }

            SpriteContents contents = new SpriteContents(
                    id,
                    new FrameSize(result.getWidth(), result.getHeight()),
                    result,
                    AnimationMetadataSection.EMPTY
            );

            success = true;
            return contents;
        } finally {
            if (!success) {
                result.close();
            }
        }
    }

    private static NativeImage copyImage(NativeImage source) {
        NativeImage result = new NativeImage(
                source.getWidth(),
                source.getHeight(),
                true
        );

        boolean success = false;
        try {
            for (int y = 0; y < source.getHeight(); y++) {
                for (int x = 0; x < source.getWidth(); x++) {
                    result.setPixelRGBA(
                            x,
                            y,
                            source.getPixelRGBA(x, y)
                    );
                }
            }

            success = true;
            return result;
        } finally {
            if (!success) {
                result.close();
            }
        }
    }

    private static int nearestSourceOffset(
            int targetOffset,
            int sourceSize,
            int targetSize
    ) {
        return Math.min(
                sourceSize - 1,
                (int) ((targetOffset + 0.5F) * sourceSize / targetSize)
        );
    }

    private static int sourceCoordinate(int start, int end, int offset) {
        return end >= start ? start + offset : start - offset;
    }

    private static void validateBaseVertices(Vertices vertices) {
        if (vertices.x0() > vertices.x1() || vertices.y0() > vertices.y1()) {
            throw new IllegalArgumentException(
                    "verticesBase must use ascending coordinates: " + vertices
            );
        }
    }

    private static void validateEqualSize(Vertices addition, Vertices base) {
        if (addition.width() != base.width()
                || addition.height() != base.height()) {
            throw new IllegalArgumentException(
                    "Source and target rectangles must have identical dimensions; "
                            + "addition=" + addition + ", base=" + base
            );
        }
    }

    private static void validateBounds(
            NativeImage image,
            Vertices vertices,
            boolean allowReverse,
            String name
    ) {
        if (!allowReverse
                && (vertices.x0() > vertices.x1()
                || vertices.y0() > vertices.y1())) {
            throw new IllegalArgumentException(
                    name + " vertices must be ascending: " + vertices
            );
        }

        int minX = Math.min(vertices.x0(), vertices.x1());
        int maxX = Math.max(vertices.x0(), vertices.x1());
        int minY = Math.min(vertices.y0(), vertices.y1());
        int maxY = Math.max(vertices.y0(), vertices.y1());

        if (minX < 0 || minY < 0
                || maxX >= image.getWidth()
                || maxY >= image.getHeight()) {
            throw new IllegalArgumentException(
                    name + " vertices outside "
                            + image.getWidth() + "x" + image.getHeight()
                            + ": " + vertices
            );
        }
    }

}