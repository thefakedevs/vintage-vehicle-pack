package tech.vvp.vvp.client.renderer.entity.vehicle;

import com.atsuishio.superbwarfare.client.renderer.entity.VehicleRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import tech.vvp.vvp.client.model.Bk16Model;
import tech.vvp.vvp.entity.vehicle.Bk16Entity;

public class Bk16Renderer extends VehicleRenderer<Bk16Entity> {
    public Bk16Renderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Bk16Model());
    }

    @Override
    public ResourceLocation getTextureLocation(Bk16Entity entity) {
        ResourceLocation[] textures = entity.getCamoTextures();
        int camoType = entity.getCamoType();
        
        if (camoType >= 0 && camoType < textures.length) {
            return textures[camoType];
        }
        
        return textures[0];
    }
}
