package org.example;

import net.minecraft.client.MinecraftClient;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class AnubisMixin {

    private int tickCounter = 0;

    // Oyun her yenilendiğinde (tick) çalışan en kararlı ana kök döngü
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickInject(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Bilgisayarı yormamak için her 20 tick'te bir (saniyede 1 kez) tarama yapıyoruz
        if (client.player != null && client.world != null && tickCounter++ % 20 == 0) {
            String worldName = client.world.getRegistryKey().getValue().getPath();

            // Sadece Nether dünyasındaysak oyuncunun etrafını tara
            if (worldName.contains("nether")) {
                BlockPos playerPos = client.player.getBlockPos();

                // Oyuncunun etrafındaki 32x32'lik alanı canlı olarak tarıyoruz
                for (int x = -16; x < 16; x++) {
                    for (int z = -16; z < 16; z++) {
                        for (int y = 8; y < 110; y++) {
                            BlockPos targetPos = new BlockPos(playerPos.getX() + x, y, playerPos.getZ() + z);

                            if (client.world.getBlockState(targetPos).getBlock() == Blocks.ANCIENT_DEBRIS) {
                                AnubisLogger.logCoordinates(worldName, targetPos);
                            }
                        }
                    }
                }
            }
        }
    }
}