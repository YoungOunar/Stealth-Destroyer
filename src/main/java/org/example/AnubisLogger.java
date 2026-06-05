package org.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import net.minecraft.util.math.BlockPos;

public class AnubisLogger {
    private static final File logFile = new File("AnubisFinder_Koordinatlar.txt");

    public static void logCoordinates(String world, BlockPos pos) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            String logLine = String.format("[%s] Netherite Bulundu -> X: %d | Y: %d | Z: %d",
                    world, pos.getX(), pos.getY(), pos.getZ());
            writer.write(logLine);
            writer.newLine();
            System.out.println("[AnubisFinder] " + logLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}