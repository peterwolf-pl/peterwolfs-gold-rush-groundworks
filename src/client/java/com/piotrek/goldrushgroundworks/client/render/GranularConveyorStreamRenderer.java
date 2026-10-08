package com.piotrek.goldrushgroundworks.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.piotrek.groundworks.api.material.GranularMaterial;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

/**
 * Renders volumetric, animated granular material streams on the conveyor belts
 * and vibrating screen deck of the gold rush gradation plant.
 */
public final class GranularConveyorStreamRenderer {

    private static final Identifier FALLBACK_TEXTURE =
            Identifier.withDefaultNamespace("textures/block/gravel.png");

    private GranularConveyorStreamRenderer() {}

    public static Identifier textureFor(GranularMaterial material) {
        if (material == null || material.id() <= 0 || material.sourceBlock() == null) {
            return FALLBACK_TEXTURE;
        }

        Identifier blockId = BuiltInRegistries.BLOCK.getKey(material.sourceBlock());
        if (blockId == null) {
            return FALLBACK_TEXTURE;
        }

        return Identifier.fromNamespaceAndPath(
                blockId.getNamespace(),
                "textures/block/" + blockId.getPath() + ".png"
        );
    }

    /**
     * Renders material travelling along the selected conveyor (+X direction, tilted -18 deg around Z).
     */
    public static void renderSelectedStream(
            SubmitNodeCollector collector,
            PoseStack stack,
            Identifier texture,
            float beltPhase,
            boolean active,
            int light
    ) {
        collector.submitCustomGeometry(
                stack,
                RenderTypes.entityCutout(texture != null ? texture : FALLBACK_TEXTURE),
                (pose, consumer) -> emitSelectedStream(consumer, pose, beltPhase, active, light)
        );
    }

    /**
     * Renders material travelling along the remainder conveyor (-Z direction, tilted -18 deg around X).
     */
    public static void renderRemainderStream(
            SubmitNodeCollector collector,
            PoseStack stack,
            Identifier texture,
            float beltPhase,
            boolean active,
            int light
    ) {
        collector.submitCustomGeometry(
                stack,
                RenderTypes.entityCutout(texture != null ? texture : FALLBACK_TEXTURE),
                (pose, consumer) -> emitRemainderStream(consumer, pose, beltPhase, active, light)
        );
    }

    /**
     * Renders a vibrating layer of aggregate on the screen deck (-5 deg around X).
     */
    public static void renderScreenDeckMaterial(
            SubmitNodeCollector collector,
            PoseStack stack,
            Identifier texture,
            float beltPhase,
            boolean active,
            int light
    ) {
        collector.submitCustomGeometry(
                stack,
                RenderTypes.entityCutout(texture != null ? texture : FALLBACK_TEXTURE),
                (pose, consumer) -> emitScreenDeck(consumer, pose, beltPhase, active, light)
        );
    }

    private static void emitSelectedStream(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float beltPhase,
            boolean active,
            int light
    ) {
        float xStart = 11.0F;
        float xEnd = 114.0F;
        float step = 4.0F;

        float zLeft = -5.8F;
        float zCenter = 0.0F;
        float zRight = 5.8F;
        float yEdge = -10.05F;

        for (float x = xStart; x < xEnd; x += step) {
            float x0 = x;
            float x1 = Math.min(x + step, xEnd);

            float lump0 = active ? 0.38F * (float) Math.sin((x0 * 0.42F) - beltPhase * 2.2F) : 0.0F;
            float lump1 = active ? 0.38F * (float) Math.sin((x1 * 0.42F) - beltPhase * 2.2F) : 0.0F;

            float ramp0 = computeRamp(x0, xStart, xEnd, 8.0F);
            float ramp1 = computeRamp(x1, xStart, xEnd, 8.0F);

            float yCrown0 = -10.1F - (3.4F + lump0) * ramp0;
            float yCrown1 = -10.1F - (3.4F + lump1) * ramp1;

            float u0 = (x0 / 16.0F) - (active ? beltPhase * 0.28F : 0.0F);
            float u1 = (x1 / 16.0F) - (active ? beltPhase * 0.28F : 0.0F);

            // Left slope
            Vector3f p0 = px(x0, yEdge, zLeft);
            Vector3f p1 = px(x1, yEdge, zLeft);
            Vector3f p2 = px(x1, yCrown1, zCenter);
            Vector3f p3 = px(x0, yCrown0, zCenter);
            emitQuad(consumer, pose, p0, p1, p2, p3, u0, 0.05F, u1, 0.05F, u1, 0.50F, u0, 0.50F, light);

            // Right slope
            Vector3f q0 = px(x0, yCrown0, zCenter);
            Vector3f q1 = px(x1, yCrown1, zCenter);
            Vector3f q2 = px(x1, yEdge, zRight);
            Vector3f q3 = px(x0, yEdge, zRight);
            emitQuad(consumer, pose, q0, q1, q2, q3, u0, 0.50F, u1, 0.50F, u1, 0.95F, u0, 0.95F, light);
        }

        // Discharge cap at end
        float endRamp = computeRamp(xEnd, xStart, xEnd, 8.0F);
        float endCrown = -10.1F - 3.4F * endRamp;
        Vector3f c0 = px(xEnd, yEdge, zLeft);
        Vector3f c1 = px(xEnd, yEdge, zRight);
        Vector3f c2 = px(xEnd, endCrown, zCenter);
        Vector3f normal = new Vector3f(1.0F, 0.0F, 0.0F);
        float uEnd = xEnd / 16.0F;
        emitVertex(consumer, pose, c0, normal, uEnd, 0.05F, light);
        emitVertex(consumer, pose, c1, normal, uEnd, 0.95F, light);
        emitVertex(consumer, pose, c2, normal, uEnd, 0.50F, light);
        emitVertex(consumer, pose, c2, normal, uEnd, 0.50F, light);
    }

    private static void emitRemainderStream(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float beltPhase,
            boolean active,
            int light
    ) {
        float zStart = -16.0F;
        float zEnd = -128.0F;
        float step = -4.0F;

        float xLeft = -5.8F;
        float xCenter = 0.0F;
        float xRight = 5.8F;
        float yEdge = -10.05F;

        for (float z = zStart; z > zEnd; z += step) {
            float z0 = z;
            float z1 = Math.max(z + step, zEnd);

            float lump0 = active ? 0.38F * (float) Math.sin((z0 * 0.42F) + beltPhase * 2.2F) : 0.0F;
            float lump1 = active ? 0.38F * (float) Math.sin((z1 * 0.42F) + beltPhase * 2.2F) : 0.0F;

            float ramp0 = computeRamp(-z0, -zStart, -zEnd, 8.0F);
            float ramp1 = computeRamp(-z1, -zStart, -zEnd, 8.0F);

            float yCrown0 = -10.1F - (3.4F + lump0) * ramp0;
            float yCrown1 = -10.1F - (3.4F + lump1) * ramp1;

            float v0 = (-z0 / 16.0F) - (active ? beltPhase * 0.28F : 0.0F);
            float v1 = (-z1 / 16.0F) - (active ? beltPhase * 0.28F : 0.0F);

            // Left slope
            Vector3f p0 = px(xLeft, yEdge, z0);
            Vector3f p1 = px(xLeft, yEdge, z1);
            Vector3f p2 = px(xCenter, yCrown1, z1);
            Vector3f p3 = px(xCenter, yCrown0, z0);
            emitQuad(consumer, pose, p0, p1, p2, p3, 0.05F, v0, 0.05F, v1, 0.50F, v1, 0.50F, v0, light);

            // Right slope
            Vector3f q0 = px(xCenter, yCrown0, z0);
            Vector3f q1 = px(xCenter, yCrown1, z1);
            Vector3f q2 = px(xRight, yEdge, z1);
            Vector3f q3 = px(xRight, yEdge, z0);
            emitQuad(consumer, pose, q0, q1, q2, q3, 0.50F, v0, 0.50F, v1, 0.95F, v1, 0.95F, v0, light);
        }

        // Discharge cap at end
        float endRamp = computeRamp(-zEnd, -zStart, -zEnd, 8.0F);
        float endCrown = -10.1F - 3.4F * endRamp;
        Vector3f c0 = px(xLeft, yEdge, zEnd);
        Vector3f c1 = px(xRight, yEdge, zEnd);
        Vector3f c2 = px(xCenter, endCrown, zEnd);
        Vector3f normal = new Vector3f(0.0F, 0.0F, -1.0F);
        float vEnd = -zEnd / 16.0F;
        emitVertex(consumer, pose, c0, normal, 0.05F, vEnd, light);
        emitVertex(consumer, pose, c1, normal, 0.95F, vEnd, light);
        emitVertex(consumer, pose, c2, normal, 0.50F, vEnd, light);
        emitVertex(consumer, pose, c2, normal, 0.50F, vEnd, light);
    }

    private static void emitScreenDeck(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float beltPhase,
            boolean active,
            int light
    ) {
        float shakeY = active ? 0.35F * (float) Math.sin(beltPhase * 5.5F) : 0.0F;
        float y = -28.2F + shakeY;

        float x0 = -12.0F;
        float x1 = 12.0F;
        float z0 = -16.0F;
        float z1 = 16.0F;

        float u0 = 0.0F;
        float u1 = 1.5F;
        float v0 = active ? beltPhase * 0.15F : 0.0F;
        float v1 = v0 + 2.0F;

        Vector3f p0 = px(x0, y, z0);
        Vector3f p1 = px(x0, y, z1);
        Vector3f p2 = px(x1, y, z1);
        Vector3f p3 = px(x1, y, z0);

        Vector3f normal = new Vector3f(0.0F, -1.0F, 0.0F);
        emitQuad(consumer, pose, p0, p1, p2, p3, u0, v0, u0, v1, u1, v1, u1, v0, light);
    }

    private static float computeRamp(float pos, float start, float end, float rampLen) {
        float fromStart = Math.max(0.0F, pos - start);
        float fromEnd = Math.max(0.0F, end - pos);
        return Math.min(1.0F, Math.min(fromStart / rampLen, fromEnd / rampLen));
    }

    private static void emitQuad(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3,
            float u0, float v0,
            float u1, float v1,
            float u2, float v2,
            float u3, float v3,
            int light
    ) {
        Vector3f normal = faceNormal(p0, p1, p2);
        emitVertex(consumer, pose, p0, normal, u0, v0, light);
        emitVertex(consumer, pose, p1, normal, u1, v1, light);
        emitVertex(consumer, pose, p2, normal, u2, v2, light);
        emitVertex(consumer, pose, p3, normal, u3, v3, light);
    }

    private static void emitVertex(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            Vector3f vertex,
            Vector3f normal,
            float u,
            float v,
            int packedLight
    ) {
        consumer.addVertex(pose, vertex.x, vertex.y, vertex.z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, normal.x, normal.y, normal.z);
    }

    private static Vector3f faceNormal(Vector3f a, Vector3f b, Vector3f c) {
        Vector3f ab = new Vector3f(b).sub(a);
        Vector3f ac = new Vector3f(c).sub(a);
        return ab.cross(ac).normalize();
    }

    private static Vector3f px(float x, float y, float z) {
        return new Vector3f(x / 16.0F, y / 16.0F, z / 16.0F);
    }
}
