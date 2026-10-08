package com.piotrek.goldrushgroundworks.client.model;

import com.piotrek.goldrushgroundworks.client.render.GoldRushScreenRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class GoldRushScreenModel extends EntityModel<GoldRushScreenRenderState> {

    private final ModelPart selectedRoller;
    private final ModelPart remainderRoller;
    private final ModelPart eccentricWeight;

    public GoldRushScreenModel(ModelPart root) {
        super(root);
        this.selectedRoller = root.getChild("selected_roller");
        this.remainderRoller = root.getChild("remainder_roller");
        this.eccentricWeight = root.getChild("eccentric_weight");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "base",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-17.0F, 8.0F, -25.0F, 34.0F, 5.0F, 50.0F)
                        .texOffs(0, 0)
                        .addBox(-16.0F, -22.0F, -22.0F, 4.0F, 30.0F, 4.0F)
                        .texOffs(0, 0)
                        .addBox(12.0F, -22.0F, -22.0F, 4.0F, 30.0F, 4.0F)
                        .texOffs(0, 0)
                        .addBox(-16.0F, -22.0F, 18.0F, 4.0F, 30.0F, 4.0F)
                        .texOffs(0, 0)
                        .addBox(12.0F, -22.0F, 18.0F, 4.0F, 30.0F, 4.0F),
                PartPose.ZERO
        );

        root.addOrReplaceChild(
                "screen_body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-15.0F, -25.0F, -20.0F, 30.0F, 23.0F, 40.0F)
                        .texOffs(0, 0)
                        .addBox(-18.0F, -7.0F, -23.0F, 36.0F, 5.0F, 46.0F)
                        .texOffs(0, 0)
                        .addBox(-13.0F, -31.0F, -18.0F, 26.0F, 6.0F, 36.0F),
                PartPose.ZERO
        );

        PartDefinition hopper = root.addOrReplaceChild(
                "hopper",
                CubeListBuilder.create(),
                PartPose.ZERO
        );

        // Wide-flared industrial hopper walls (all 4 walls flared outwards at ~38 deg)
        hopper.addOrReplaceChild(
                "left_wall",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.0F, -20.0F, -34.0F, 3.0F, 20.0F, 68.0F),
                PartPose.offsetAndRotation(-15.0F, -29.0F, 0.0F, 0.0F, 0.0F, -0.66F)
        );
        hopper.addOrReplaceChild(
                "right_wall",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(0.0F, -20.0F, -34.0F, 3.0F, 20.0F, 68.0F),
                PartPose.offsetAndRotation(15.0F, -29.0F, 0.0F, 0.0F, 0.0F, 0.66F)
        );
        hopper.addOrReplaceChild(
                "front_wall",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-28.0F, -20.0F, -3.0F, 56.0F, 20.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -29.0F, -20.0F, 0.66F, 0.0F, 0.0F)
        );
        hopper.addOrReplaceChild(
                "back_wall",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-28.0F, -20.0F, 0.0F, 56.0F, 20.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -29.0F, 20.0F, -0.66F, 0.0F, 0.0F)
        );

        root.addOrReplaceChild(
                "screen_deck",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-12.0F, -29.0F, -16.0F, 24.0F, 2.0F, 32.0F),
                PartPose.rotation((float) Math.toRadians(-5.0F), 0.0F, 0.0F)
        );

        // Extended by 3 blocks (+48px, 108px total) and inclined upwards (-18 deg around Z)
        root.addOrReplaceChild(
                "selected_conveyor",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(11.0F, -10.0F, -8.0F, 108.0F, 4.0F, 16.0F)
                        .texOffs(0, 0)
                        .addBox(11.0F, -16.0F, -10.0F, 108.0F, 6.0F, 2.0F)
                        .texOffs(0, 0)
                        .addBox(11.0F, -16.0F, 8.0F, 108.0F, 6.0F, 2.0F),
                PartPose.rotation(
                        0.0F,
                        0.0F,
                        (float) Math.toRadians(-18.0F)
                )
        );

        // Extended by 3 blocks (+48px, 116px total) and inclined upwards (-18 deg around X)
        root.addOrReplaceChild(
                "remainder_conveyor",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, -10.0F, -132.0F, 16.0F, 4.0F, 116.0F)
                        .texOffs(0, 0)
                        .addBox(-10.0F, -16.0F, -132.0F, 2.0F, 6.0F, 116.0F)
                        .texOffs(0, 0)
                        .addBox(8.0F, -16.0F, -132.0F, 2.0F, 6.0F, 116.0F),
                PartPose.rotation(
                        (float) Math.toRadians(-18.0F),
                        0.0F,
                        0.0F
                )
        );

        // Gold sluice box / recovery chute mounted under the side conveyor
        root.addOrReplaceChild(
                "gold_sluice",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(38.0F, -6.0F, -7.0F, 12.0F, 8.0F, 14.0F)
                        .texOffs(0, 0)
                        .addBox(41.0F, 2.0F, -4.0F, 6.0F, 6.0F, 8.0F),
                PartPose.rotation(
                        0.0F,
                        0.0F,
                        (float) Math.toRadians(-18.0F)
                )
        );

        // Steel truss support legs anchoring the elevated selected conveyor to the ground
        root.addOrReplaceChild(
                "selected_support",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(50.0F, -26.0F, -6.0F, 3.0F, 39.0F, 3.0F)
                        .texOffs(0, 0)
                        .addBox(50.0F, -26.0F, 3.0F, 3.0F, 39.0F, 3.0F)
                        .texOffs(0, 0)
                        .addBox(50.0F, -6.0F, -6.0F, 3.0F, 3.0F, 12.0F)
                        .texOffs(0, 0)
                        .addBox(94.0F, -40.0F, -6.0F, 3.0F, 53.0F, 3.0F)
                        .texOffs(0, 0)
                        .addBox(94.0F, -40.0F, 3.0F, 3.0F, 53.0F, 3.0F)
                        .texOffs(0, 0)
                        .addBox(94.0F, -15.0F, -6.0F, 3.0F, 3.0F, 12.0F),
                PartPose.ZERO
        );

        // Steel truss support legs anchoring the elevated remainder conveyor to the ground
        root.addOrReplaceChild(
                "remainder_support",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-6.0F, -26.0F, -54.0F, 3.0F, 39.0F, 3.0F)
                        .texOffs(0, 0)
                        .addBox(3.0F, -26.0F, -54.0F, 3.0F, 39.0F, 3.0F)
                        .texOffs(0, 0)
                        .addBox(-6.0F, -6.0F, -54.0F, 12.0F, 3.0F, 3.0F)
                        .texOffs(0, 0)
                        .addBox(-6.0F, -40.0F, -98.0F, 3.0F, 53.0F, 3.0F)
                        .texOffs(0, 0)
                        .addBox(3.0F, -40.0F, -98.0F, 3.0F, 53.0F, 3.0F)
                        .texOffs(0, 0)
                        .addBox(-6.0F, -15.0F, -98.0F, 12.0F, 3.0F, 3.0F),
                PartPose.ZERO
        );

        root.addOrReplaceChild(
                "selected_roller",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0F, -8.0F, -8.0F, 4.0F, 16.0F, 16.0F),
                PartPose.offset(115.0F, -46.0F, 0.0F)
        );

        root.addOrReplaceChild(
                "remainder_roller",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, -8.0F, -2.0F, 16.0F, 16.0F, 4.0F),
                PartPose.offset(0.0F, -49.0F, -119.0F)
        );

        root.addOrReplaceChild(
                "eccentric_weight",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, -12.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(GoldRushScreenRenderState state) {
        selectedRoller.zRot = state.active ? -state.beltPhase : 0.0F;
        remainderRoller.xRot = state.active ? -state.beltPhase : 0.0F;
        eccentricWeight.zRot = state.active
                ? state.beltPhase * 1.75F
                : 0.0F;
    }
}
