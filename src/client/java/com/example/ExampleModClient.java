package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.glfw.GLFW;

public class ExampleModClient implements ClientModInitializer {
    private static boolean espEnabled = true;

    @Override
    public void onInitializeClient() {
        // Register ESP render event
        WorldRenderEvents.AFTER_TRANSLUCENT.register(ExampleModClient::renderESP);

        // Register tick for toggle (R key)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.currentScreen == null &&
                InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_R)) {
                espEnabled = !espEnabled;
                // Optional: client.player.sendMessage(Text.literal("ESP: " + (espEnabled ? "ON" : "OFF")), false);
            }
        });
    }

    private static void renderESP(WorldRenderContext context) {
        if (!espEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate consumers = context.consumers();
        VertexConsumer vertexConsumer = consumers.getBuffer(RenderLayer.LINES);

        matrices.push();

        // Setup render state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(3.0f);

        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player || !player.isAlive()) continue;

            double x = MathHelper.lerp(context.tickDelta(), player.lastRenderX, player.getX()) - camPos.x;
            double y = MathHelper.lerp(context.tickDelta(), player.lastRenderY, player.getY()) - camPos.y;
            double z = MathHelper.lerp(context.tickDelta(), player.lastRenderZ, player.getZ()) - camPos.z;

            matrices.push();
            matrices.translate(x, y, z);

            float w = player.getWidth() / 2.0f;
            float h = player.getHeight();
            Box box = new Box(-w, 0, -w, w, h, w);

            // Render box (red)
            renderBox(matrices, vertexConsumer, box, 1.0f, 0.0f, 0.0f, 0.8f);

            matrices.pop();
        }

        // Draw and cleanup
        consumers.draw();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    private static void renderBox(MatrixStack matrices, VertexConsumer vc, Box box, float r, float g, float b, float a) {
        MatrixStack.Entry entry = matrices.peek();
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        // Bottom face edges
        vc.vertex(entry.getPositionMatrix(), x1, y1, z1).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x2, y1, z1).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x2, y1, z1).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x2, y1, z2).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x2, y1, z2).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x1, y1, z2).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x1, y1, z2).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x1, y1, z1).color(r,g,b,a).next();

        // Top face edges
        vc.vertex(entry.getPositionMatrix(), x1, y2, z1).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x2, y2, z1).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x2, y2, z1).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x2, y2, z2).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x2, y2, z2).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x1, y2, z2).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x1, y2, z2).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x1, y2, z1).color(r,g,b,a).next();

        // Vertical edges
        vc.vertex(entry.getPositionMatrix(), x1, y1, z1).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x1, y2, z1).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x2, y1, z1).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x2, y2, z1).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x2, y1, z2).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x2, y2, z2).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x1, y1, z2).color(r,g,b,a).next();
        vc.vertex(entry.getPositionMatrix(), x1, y2, z2).color(r,g,b,a).next();
    }
}