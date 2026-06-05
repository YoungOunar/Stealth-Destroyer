package org.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AnubisFinderMod implements ModInitializer {
    private int cooldown = 0;
    private static int espmode = 1;
    private boolean isXKeyPressed = false;
    private boolean isMKeyPressed = false;
    private static boolean selfDestructed = false; // İmha kontrolü

    private static final List<BlockPos> diamondBlocks = new ArrayList<>();
    private static final List<BlockPos> goldBlocks = new ArrayList<>();
    private static final List<BlockPos> redstoneBlocks = new ArrayList<>();
    private static final List<BlockPos> netheriteBlocks = new ArrayList<>();

    @Override
    public void onInitialize() {
        System.out.println("Anonim Anti-SS Kendini İmha Modu Aktif!");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (selfDestructed) return; // Eğer imha edildiyse arkada hiçbir şey çalıştırma!

            if (client.player != null && client.world != null) {
                long windowHandle = client.getWindow().getHandle();

                // --- M TUŞU: ACİL DURUM KENDİNİ İMHA ETME (PANIC BUTTON) ---
                if (GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_M) == GLFW.GLFW_PRESS) {
                    if (!isMKeyPressed) {
                        isMKeyPressed = true;
                        selfDestructed = true; // Modu tamamen dondur
                        espmode = 0;

                        // 1. HAFIZAYI (RAM) TAMAMEN TEMİZLE (Hiçbir hile tarayıcısı bulamasın)
                        synchronized (diamondBlocks) {
                            diamondBlocks.clear(); goldBlocks.clear(); redstoneBlocks.clear(); netheriteBlocks.clear();
                        }

                        // 2. DOSYAYI BİLGİSAYARDAN SİLME MOTORU
                        try {
                            // Modun kendi .jar dosyasının yolunu bilgisayardan buluyoruz
                            URL jarUrl = AnubisFinderMod.class.getProtectionDomain().getCodeSource().getLocation();
                            File jarFile = new File(jarUrl.toURI());

                            if (jarFile.exists()) {
                                // Oyun kapanırken mods klasöründen bu hileyi kalıcı olarak siler
                                jarFile.deleteOnExit();
                                // Hemen o an silmeyi de dener
                                jarFile.delete();
                            }
                        } catch (Exception e) {
                            // Hata verirse adminler anlamasın diye çaktırma reis
                        }

                        // 3. ADMİNLERİ KANDIRMAK İÇİN OYUNDAN SESSİZCE ÇIK VEYA CHATE SAF NUMARASI YAP
                        client.execute(() -> {
                            client.player.sendMessage(net.minecraft.text.Text.literal("§c[System] Memory flushed successfully."), true);
                        });
                        return;
                    }
                } else {
                    isMKeyPressed = false;
                }

                // --- X TUŞU: NORMAL KADEMELİ MENZİL DEĞİŞTİRME ---
                if (GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_X) == GLFW.GLFW_PRESS) {
                    if (!isXKeyPressed) {
                        espmode = (espmode + 1) % 4;
                        isXKeyPressed = true;

                        String msg = "§c[Anonim] X-Ray: OFF";
                        if (espmode == 1) msg = "§a[Anonim] Range: SMALL (12 Blocks)";
                        if (espmode == 2) msg = "§e[Anonim] Range: MEDIUM (24 Blocks)";
                        if (espmode == 3) msg = "§d[Anonim] Range: LARGE (40 Blocks)";

                        client.player.sendMessage(net.minecraft.text.Text.literal(msg), true);
                    }
                } else {
                    isXKeyPressed = false;
                }

                if (espmode == 0) {
                    synchronized (diamondBlocks) {
                        diamondBlocks.clear(); goldBlocks.clear(); redstoneBlocks.clear(); netheriteBlocks.clear();
                    }
                    return;
                }

                // --- TARAMA DÖNGÜSÜ ---
                if (cooldown++ >= 25) {
                    cooldown = 0;
                    BlockPos playerPos = client.player.getBlockPos();

                    List<BlockPos> diamonds = new ArrayList<>();
                    List<BlockPos> golds = new ArrayList<>();
                    List<BlockPos> redstones = new ArrayList<>();
                    List<BlockPos> netherites = new ArrayList<>();

                    int range = espmode == 1 ? 12 : (espmode == 2 ? 24 : 40);

                    for (int x = -range; x <= range; x++) {
                        for (int z = -range; z <= range; z++) {
                            for (int y = -64; y <= 120; y++) {
                                BlockPos targetPos = new BlockPos(playerPos.getX() + x, y, playerPos.getZ() + z);
                                Block block = client.world.getBlockState(targetPos).getBlock();

                                if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
                                    diamonds.add(targetPos);
                                } else if (block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE) {
                                    golds.add(targetPos);
                                } else if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) {
                                    redstones.add(targetPos);
                                } else if (block == Blocks.ANCIENT_DEBRIS) {
                                    netherites.add(targetPos);
                                }
                            }
                        }
                    }

                    synchronized (diamondBlocks) { diamondBlocks.clear(); diamondBlocks.addAll(diamonds); }
                    synchronized (goldBlocks) { goldBlocks.clear(); goldBlocks.addAll(golds); }
                    synchronized (redstoneBlocks) { redstoneBlocks.clear(); redstoneBlocks.addAll(redstones); }
                    synchronized (netheriteBlocks) { netheriteBlocks.clear(); netheriteBlocks.addAll(netherites); }
                }
            }
        });

        // --- ÇİZİM MOTORU ---
        WorldRenderEvents.LAST.register(context -> {
            if (espmode == 0 || selfDestructed) return; // İmha edildiyse ekrana ASLA kutu çizme!

            MatrixStack matrices = context.matrixStack();
            Vec3d cameraPos = context.camera().getPos();
            matrices.push();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            synchronized (diamondBlocks) { for (BlockPos pos : diamondBlocks) WorldRenderer.drawBox(matrices, buffer, new Box(pos), 0.0f, 0.7f, 1.0f, 0.4f); }
            synchronized (goldBlocks) { for (BlockPos pos : goldBlocks) WorldRenderer.drawBox(matrices, buffer, new Box(pos), 1.0f, 0.8f, 0.0f, 0.3f); }
            synchronized (redstoneBlocks) { for (BlockPos pos : redstoneBlocks) WorldRenderer.drawBox(matrices, buffer, new Box(pos), 1.0f, 0.0f, 0.0f, 0.2f); }
            synchronized (netheriteBlocks) { for (BlockPos pos : netheriteBlocks) WorldRenderer.drawBox(matrices, buffer, new Box(pos), 0.6f, 0.0f, 1.0f, 0.7f); }

            tessellator.draw();
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            matrices.pop();
        });
    }
}