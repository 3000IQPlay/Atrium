package me._3000IQPlay.atrium.mixin.mixins;

import me._3000IQPlay.atrium.Atrium;
import me._3000IQPlay.atrium.event.events.RenderItemEvent;
import me._3000IQPlay.atrium.features.modules.render.ViewModel;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={RenderItem.class})
public class MixinRenderItem {
    @Shadow
    private void renderModel(IBakedModel model, int color, ItemStack stack) {
    }

    @Inject(method={"renderItemModel"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/RenderItem;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V", shift=At.Shift.BEFORE)})
    private void renderItemModel(ItemStack stack, IBakedModel bakedModel, ItemCameraTransforms.TransformType transform, boolean leftHanded, CallbackInfo ci) {
        RenderItemEvent event = new RenderItemEvent(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
        MinecraftForge.EVENT_BUS.post((Event)event);
        if (ViewModel.getInstance().isEnabled()) {
            if (!leftHanded) {
                GlStateManager.scale((double)event.getMainHandScaleX(), (double)event.getMainHandScaleY(), (double)event.getMainHandScaleZ());
            } else {
                GlStateManager.scale((double)event.getOffHandScaleX(), (double)event.getOffHandScaleY(), (double)event.getOffHandScaleZ());
            }
        }
    }
}

