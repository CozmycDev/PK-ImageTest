package net.doodcraft.cozmyc.imagetest;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import org.bukkit.Location;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class ImageTest extends FireAbility implements AddonAbility {

    private long cooldown;
    private long duration;
    private long startTime;
    private BufferedImage image;
    private ArrayList<ParticleData> particleDataList;
    private int rowsPerTick;
    private int currentRow;

    public ImageTest(Player player) {
        super(player);

        if (!bPlayer.canBend(this) || !bPlayer.canBendIgnoreCooldowns(this)) {
            return;
        }

        setFields();
        start();
    }

    private void setFields() {
        this.cooldown = 1000;
        this.duration = 60000;
        this.startTime = System.currentTimeMillis();

        File file = new File(ProjectKorra.plugin.getDataFolder() + File.separator + "Abilities" + File.separator + "image.png");
        try {
            this.image = ImageIO.read(file);
            this.image = resizeImage(image, 80, 80);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.particleDataList = new ArrayList<>();
        cacheParticlePositions();

        this.rowsPerTick = 16;
        this.currentRow = 0;
    }

    @Override
    public void progress() {
        if (System.currentTimeMillis() - startTime > duration || !bPlayer.getBoundAbilityName().equalsIgnoreCase("ImageTest")) {
            remove();
            return;
        }

        if (image == null) {
            return;
        }

        displayCachedParticles();
    }

    private BufferedImage resizeImage(BufferedImage image, int maxWidth, int maxHeight) {
        int width = image.getWidth();
        int height = image.getHeight();
        double aspectRatio = (double) width / height;

        if (width > maxWidth || height > maxHeight) {
            if (width > height) {
                width = maxWidth;
                height = (int) (width / aspectRatio);
            } else {
                height = maxHeight;
                width = (int) (height * aspectRatio);
            }
        }

        Image tempImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(tempImage, 0, 0, null);
        g2d.dispose();
        return resizedImage;
    }

    private void cacheParticlePositions() {
        double centerXOffset = (image.getWidth() / 2.0) / 10.0;
        double centerZOffset = (image.getHeight() / 2.0) / 10.0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgba = image.getRGB(x, y);
                Color color = new Color(rgba, true);
                if (color.getAlpha() > 0) {
                    double xOffset = (x / 10.0) - centerXOffset;
                    double yOffset = 0;
                    double zOffset = (y / 10.0) - centerZOffset;

                    particleDataList.add(new ParticleData(xOffset, yOffset, zOffset, org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue())));
                }
            }
        }
    }

    private void displayCachedParticles() {
        Location location = player.getLocation();
        World world = location.getWorld();

        int rowsProcessed = 0;

        while (rowsProcessed < rowsPerTick && currentRow < image.getHeight()) {
            for (int x = 0; x < image.getWidth(); x++) {
                int index = currentRow * image.getWidth() + x;
                if (index >= particleDataList.size()) {
                    break;
                }

                ParticleData particleData = particleDataList.get(index);
                double xPos = location.getX() + particleData.getXOffset();
                double yPos = location.getY() + particleData.getYOffset();
                double zPos = location.getZ() + particleData.getZOffset();

                Particle.DustOptions dustOptions = new Particle.DustOptions(particleData.getColor(), 0.8F);
                world.spawnParticle(Particle.REDSTONE, new Location(world, xPos, yPos, zPos), 1, dustOptions);
            }

            currentRow++;
            rowsProcessed++;
        }

        if (currentRow >= image.getHeight()) {
            currentRow = 0;
        }
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public String getName() {
        return "ImageTest";
    }

    @Override
    public Element getElement() {
        return Element.FIRE;
    }

    @Override
    public Location getLocation() {
        return player != null ? player.getLocation() : null;
    }

    @Override
    public boolean isHarmlessAbility() {
        return true;
    }

    @Override
    public boolean isIgniteAbility() {
        return false;
    }

    @Override
    public boolean isExplosiveAbility() {
        return false;
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public void load() {
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new ImageTestListener(), ProjectKorra.plugin);
    }

    @Override
    public void stop() {

    }

    @Override
    public String getAuthor() {
        return "Cozmyc";
    }

    @Override
    public String getVersion() {
        return "1.0.1";
    }

    private static class ParticleData {
        private final double xOffset;
        private final double yOffset;
        private final double zOffset;
        private final org.bukkit.Color color;

        public ParticleData(double xOffset, double yOffset, double zOffset, org.bukkit.Color color) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.zOffset = zOffset;
            this.color = color;
        }

        public double getXOffset() {
            return xOffset;
        }

        public double getYOffset() {
            return yOffset;
        }

        public double getZOffset() {
            return zOffset;
        }

        public org.bukkit.Color getColor() {
            return color;
        }
    }
}