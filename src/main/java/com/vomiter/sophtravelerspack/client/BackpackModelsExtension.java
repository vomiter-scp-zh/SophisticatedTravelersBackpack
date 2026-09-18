package com.vomiter.sophtravelerspack.client;

import com.vomiter.sophtravelerspack.traveler.TravelerTypeInstance;
import net.minecraft.client.resources.model.BakedModel;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackDynamicModel;

public interface BackpackModelsExtension {
    void stbp$replaceModel(
            String partName,
            BakedModel replacement
    );

    BackpackDynamicModel.BackpackBakedModel stbp$get(
            TravelerTypeInstance travelerType
    );

    Boolean stbp$isTravelerType();

    void stbp$copy(BackpackDynamicModel.BackpackBakedModel other);
}