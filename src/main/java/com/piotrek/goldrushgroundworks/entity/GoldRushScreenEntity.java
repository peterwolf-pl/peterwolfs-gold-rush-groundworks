package com.piotrek.goldrushgroundworks.entity;

import com.piotrek.goldrushgroundworks.GoldRushGroundworksMod;
import com.piotrek.goldrushgroundworks.config.GoldRushConfig;
import com.piotrek.goldrushgroundworks.logic.GoldYieldLogic;
import com.piotrek.goldrushgroundworks.logic.GradationRouting;
import com.piotrek.groundworks.api.GroundworksApi;
import com.piotrek.groundworks.api.container.IWorldGranularContainer;
import com.piotrek.groundworks.api.deposit.DepositResult;
import com.piotrek.groundworks.api.excavation.ExcavationResult;
import com.piotrek.groundworks.api.material.GranularComposition;
import com.piotrek.groundworks.api.material.GranularMaterial;
import com.piotrek.groundworks.api.material.GranularMaterialRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GoldRushScreenEntity extends Entity implements IWorldGranularContainer {
    public static final int CAPACITY = 8 * 512;
    public static final int OUTPUT_UNITS_PER_TICK = 32;

    /** Material id 5 is dedicated Gold screening mode ("Złoto"). */
    public static final int GOLD_MATERIAL_ID = 5;
    public static final int DEFAULT_SELECTED_MATERIAL_ID = GOLD_MATERIAL_ID;

    private static final int OUTPUT_SURFACE_SEARCH_DEPTH = 16;
    private static final int MAX_NUGGET_STACK = 64;

    private static final EntityDataAccessor<Integer> SELECTED_MATERIAL_ID =
            SynchedEntityData.defineId(GoldRushScreenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> REMAINDER_MATERIAL_ID =
            SynchedEntityData.defineId(GoldRushScreenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STORED_UNITS =
            SynchedEntityData.defineId(GoldRushScreenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SELECTED_UNITS =
            SynchedEntityData.defineId(GoldRushScreenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ACTIVE =
            SynchedEntityData.defineId(GoldRushScreenEntity.class, EntityDataSerializers.BOOLEAN);

    private final GranularComposition buffer = new GranularComposition();
    private long goldYieldWeightedCarry = 0L;

    public GoldRushScreenEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SELECTED_MATERIAL_ID, DEFAULT_SELECTED_MATERIAL_ID);
        builder.define(REMAINDER_MATERIAL_ID, GranularMaterialRegistry.DIRT != null ? GranularMaterialRegistry.DIRT.id() : 1);
        builder.define(STORED_UNITS, 0);
        builder.define(SELECTED_UNITS, 0);
        builder.define(ACTIVE, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level();
        ingestHopperTerrain(serverLevel);
        boolean moved = processMaterial(serverLevel);
        entityData.set(ACTIVE, moved);

        if (moved && tickCount % 6 == 0) {
            serverLevel.playSound(
                    null,
                    getX(),
                    getY() + 1.2D,
                    getZ(),
                    SoundEvents.GRAVEL_STEP,
                    SoundSource.BLOCKS,
                    0.55F,
                    0.80F
            );
        }
    }

    private boolean processMaterial(ServerLevel level) {
        if (buffer.isEmpty()) {
            syncState();
            return false;
        }

        int selectedId = getSelectedMaterialId();
        int routingSelectedId;
        if (selectedId == GOLD_MATERIAL_ID) {
            int gravelId = GranularMaterialRegistry.GRAVEL != null ? GranularMaterialRegistry.GRAVEL.id() : 3;
            if (buffer.unitsOf(gravelId) > 0) {
                routingSelectedId = gravelId;
            } else {
                int sandId = GranularMaterialRegistry.SAND != null ? GranularMaterialRegistry.SAND.id() : 2;
                if (buffer.unitsOf(sandId) > 0) {
                    routingSelectedId = sandId;
                } else {
                    routingSelectedId = buffer.dominantMaterialId();
                }
            }
        } else {
            routingSelectedId = selectedId;
        }

        GradationRouting.Batch batch = GradationRouting.plan(
                buffer.toArray(),
                routingSelectedId,
                OUTPUT_UNITS_PER_TICK
        );

        int moved = 0;

        if (batch.selectedUnits() > 0) {
            GranularMaterial selected = GranularMaterialRegistry.byId(routingSelectedId);
            moved += transferMaterial(
                    level,
                    selectedOutputPoint(),
                    selected,
                    batch.selectedUnits()
            );
        }

        int[] remainder = batch.remainderUnits();
        for (int id = 1; id < remainder.length; id++) {
            int amount = remainder[id];
            if (amount <= 0) {
                continue;
            }

            GranularMaterial material = GranularMaterialRegistry.byId(id);
            moved += transferMaterial(
                    level,
                    remainderOutputPoint(),
                    material,
                    amount
            );
        }

        if (moved > 0) {
            syncState();
        }

        return moved > 0;
    }

    private int transferMaterial(
            ServerLevel level,
            Vec3 outputPoint,
            GranularMaterial material,
            int requested
    ) {
        if (requested <= 0 || material == null || material == GranularMaterial.EMPTY) {
            return 0;
        }

        int available = buffer.unitsOf(material);
        int request = Math.min(requested, available);
        if (request <= 0) {
            return 0;
        }

        IWorldGranularContainer receiver = findConveyorReceiver(level, outputPoint);

        int consumed = 0;

        if (receiver != null) {
            consumed = Mth.clamp(
                    receiver.receiveMaterialAt(level, outputPoint, material, request),
                    0,
                    request
            );
        } else {
            BlockPos target = findDepositSurface(level, outputPoint);
            if (target != null) {
                DepositResult result = GroundworksApi.depositWithOverflow(
                        level,
                        target,
                        material,
                        request
                );
                consumed = Mth.clamp(result.unitsDeposited(), 0, request);

                for (BlockPos affected : result.affectedCells()) {
                    GroundworksApi.markForSimulation(level, affected);
                }
            }
        }

        if (consumed > 0) {
            buffer.remove(material.id(), consumed);
            Vec3 targetVec = receiver instanceof Entity e
                    ? e.position().add(0.0D, 1.8D, 0.0D)
                    : null;
            spawnFallingStreamParticles(level, outputPoint, targetVec, material);

            if (receiver instanceof Entity e && tickCount % 6 == 0) {
                level.playSound(
                        null,
                        e.getX(),
                        e.getY() + 1.5D,
                        e.getZ(),
                        SoundEvents.GRAVEL_PLACE,
                        SoundSource.BLOCKS,
                        0.70F,
                        0.95F
                );
            }

            if (material == GranularMaterialRegistry.GRAVEL) {
                processGoldYield(level, consumed);
            }
        }

        return consumed;
    }

    private void processGoldYield(ServerLevel level, int processedUnits) {
        GoldYieldLogic.YieldResult result = GoldYieldLogic.accrue(
                goldYieldWeightedCarry,
                processedUnits,
                GoldRushConfig.getNuggetsPerTenBlocks()
        );

        goldYieldWeightedCarry = result.weightedCarry();
        if (result.nuggets() > 0) {
            spawnGoldNuggets(level, goldCollectionPoint(), result.nuggets());
        }
    }

    private void spawnGoldNuggets(ServerLevel level, Vec3 outputPoint, int totalNuggets) {
        int remaining = Math.max(0, totalNuggets);

        while (remaining > 0) {
            int count = Math.min(MAX_NUGGET_STACK, remaining);
            ItemEntity item = new ItemEntity(
                    level,
                    outputPoint.x,
                    outputPoint.y + 0.12D,
                    outputPoint.z,
                    new ItemStack(Items.GOLD_NUGGET, count)
            );
            item.setDeltaMovement(0.0D, -0.08D, 0.0D);
            level.addFreshEntity(item);
            remaining -= count;
        }

        level.playSound(
                null,
                outputPoint.x,
                outputPoint.y,
                outputPoint.z,
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS,
                0.35F,
                1.35F
        );

        level.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                outputPoint.x,
                outputPoint.y + 0.15D,
                outputPoint.z,
                4,
                0.12D,
                0.12D,
                0.12D,
                0.02D
        );
    }

    @Nullable
    private IWorldGranularContainer findConveyorReceiver(
            ServerLevel level,
            Vec3 outputPoint
    ) {
        AABB searchBox = new AABB(
                outputPoint.x - 4.5D,
                level.getMinY(),
                outputPoint.z - 4.5D,
                outputPoint.x + 4.5D,
                outputPoint.y + 2.0D,
                outputPoint.z + 4.5D
        );

        List<Entity> candidates = level.getEntitiesOfClass(
                Entity.class,
                searchBox,
                entity -> entity != this && entity instanceof IWorldGranularContainer
        );

        IWorldGranularContainer best = null;
        double bestDistSqr = Double.MAX_VALUE;

        for (Entity entity : candidates) {
            IWorldGranularContainer container = (IWorldGranularContainer) entity;

            // 1. Check direct hit at discharge point
            if (container.canReceiveAt(outputPoint)) {
                double dist = entity.distanceToSqr(outputPoint.x, outputPoint.y, outputPoint.z);
                if (dist < bestDistSqr) {
                    bestDistSqr = dist;
                    best = container;
                }
                continue;
            }

            // 2. Probe down the vertical gravity fall stream from conveyor lip to vehicle
            boolean hit = false;
            for (double y = outputPoint.y; y >= entity.getY(); y -= 0.40D) {
                Vec3 probe = new Vec3(outputPoint.x, y, outputPoint.z);
                if (container.canReceiveAt(probe)) {
                    hit = true;
                    break;
                }
            }

            // 3. Probe with a slight horizontal margin around the vehicle body
            if (!hit) {
                double bedY = entity.getY() + 1.80D;
                for (double dx = -1.20D; dx <= 1.20D && !hit; dx += 1.20D) {
                    for (double dz = -1.20D; dz <= 1.20D && !hit; dz += 1.20D) {
                        Vec3 probe = new Vec3(outputPoint.x + dx, bedY, outputPoint.z + dz);
                        if (container.canReceiveAt(probe)) {
                            hit = true;
                        }
                    }
                }
            }

            if (hit) {
                double dist = entity.distanceToSqr(outputPoint.x, outputPoint.y, outputPoint.z);
                if (dist < bestDistSqr) {
                    bestDistSqr = dist;
                    best = container;
                }
            }
        }

        return best;
    }

    private static void spawnFallingStreamParticles(
            ServerLevel level,
            Vec3 top,
            @Nullable Vec3 bottom,
            GranularMaterial material
    ) {
        if (material == null || material.id() <= 0) {
            return;
        }
        var block = material.sourceBlock() != null ? material.sourceBlock() : Blocks.DIRT;
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, block.defaultBlockState());

        double targetY = bottom != null ? bottom.y : top.y - 3.8D;
        double fallDistance = Math.max(0.2D, top.y - targetY);

        for (int i = 0; i < 4; i++) {
            double fraction = level.getRandom().nextDouble();
            double py = top.y - fraction * fallDistance;
            double px = top.x + (level.getRandom().nextDouble() - 0.5D) * 0.25D;
            double pz = top.z + (level.getRandom().nextDouble() - 0.5D) * 0.25D;
            level.sendParticles(particle, px, py, pz, 1, 0.02D, -0.22D, 0.02D, 0.05D);
        }
    }

    @Nullable
    private static BlockPos findDepositSurface(ServerLevel level, Vec3 point) {
        BlockPos start = BlockPos.containing(point.x, point.y, point.z);

        int minY = Math.max(
                level.getMinY(),
                start.getY() - OUTPUT_SURFACE_SEARCH_DEPTH
        );

        for (int y = start.getY(); y >= minY; y--) {
            BlockPos check = new BlockPos(start.getX(), y, start.getZ());

            GranularMaterial terrainMaterial = GroundworksApi.getMaterial(level, check);
            if (terrainMaterial != null && terrainMaterial != GranularMaterial.EMPTY) {
                return check;
            }

            BlockState state = level.getBlockState(check);
            if (!state.isAir()) {
                return check.above();
            }
        }

        return null;
    }

    /**
     * Selected belt runs to model +X and rises upwards (-18 deg around Z).
     * With scale(-1, -1, 1), that is world -right (distance ~7.20m)
     * and world +Y (height ~4.38m).
     */
    private Vec3 selectedOutputPoint() {
        return position()
                .add(rightVector().scale(-7.20D))
                .add(0.0D, 4.38D, 0.0D);
    }

    /**
     * Remainder belt runs to model -Z and rises upwards (-18 deg around X).
     * That is world -forward (distance ~7.45m) and world +Y (height ~4.56m).
     */
    private Vec3 remainderOutputPoint() {
        return position()
                .add(forwardVector().scale(-7.45D))
                .add(0.0D, 4.56D, 0.0D);
    }

    /**
     * Gold nuggets drop from the sluice box mounted under the side conveyor.
     * At distance ~2.65m along -right, safely outside the machine collision box (2.25m),
     * dropping directly into a vanilla hopper placed beneath.
     */
    private Vec3 goldCollectionPoint() {
        return position()
                .add(rightVector().scale(-2.65D))
                .add(0.0D, 1.65D, 0.0D);
    }

    private Vec3 forwardVector() {
        double yawRad = Math.toRadians(getYRot());
        return new Vec3(-Math.sin(yawRad), 0.0D, Math.cos(yawRad));
    }

    private Vec3 rightVector() {
        double yawRad = Math.toRadians(getYRot());
        return new Vec3(Math.cos(yawRad), 0.0D, Math.sin(yawRad));
    }

    @Override
    public int receiveMaterialAt(ServerLevel level, Vec3 worldPoint, GranularMaterial material, int units) {
        if (!canReceiveAt(worldPoint)) {
            return 0;
        }
        return acceptMaterial(material, units);
    }

    @Override
    public int receiveCompositionAt(ServerLevel level, Vec3 worldPoint, GranularComposition composition) {
        if (!canReceiveAt(worldPoint)) {
            return 0;
        }
        return acceptComposition(composition);
    }

    @Override
    public int capacity() {
        return CAPACITY;
    }

    @Override
    public int storedUnits() {
        return buffer.totalUnits();
    }

    @Override
    public GranularMaterial storedMaterial() {
        int id = buffer.dominantMaterialId();
        return id > 0 ? GranularMaterialRegistry.byId(id) : GranularMaterial.EMPTY;
    }

    @Override
    public GranularComposition storedComposition() {
        return buffer.copy();
    }

    @Override
    public int acceptMaterial(GranularMaterial material, int units) {
        if (material == null || material == GranularMaterial.EMPTY || units <= 0) {
            return 0;
        }

        int room = CAPACITY - buffer.totalUnits();
        int accepted = Math.min(units, Math.max(0, room));

        if (accepted > 0) {
            buffer.add(material, accepted);
            syncState();
        }

        return accepted;
    }

    @Override
    public int acceptComposition(GranularComposition composition) {
        if (composition == null || composition.isEmpty() || !hasRoom()) {
            return 0;
        }

        int room = CAPACITY - buffer.totalUnits();
        GranularComposition accepted = composition.copy();

        if (accepted.totalUnits() > room) {
            accepted = accepted.extractProportional(room);
        }

        int added = buffer.addAll(accepted);
        syncState();
        return added;
    }

    @Override
    public int extractMaterial(int maxUnits) {
        if (maxUnits <= 0 || buffer.isEmpty()) {
            return 0;
        }

        int selected = buffer.dominantMaterialId();
        int removed = buffer.remove(selected, maxUnits);

        if (removed > 0) {
            syncState();
        }

        return removed;
    }

    @Override
    public GranularComposition extractComposition(int maxUnits) {
        GranularComposition extracted = buffer.extractProportional(maxUnits);

        if (!extracted.isEmpty()) {
            syncState();
        }

        return extracted;
    }

    @Override
    public boolean canReceiveAt(Vec3 worldPoint) {
        return isPointInHopperFunnel(position(), getYRot(), worldPoint);
    }

    /**
     * Checks if a point is within the wide-angle hopper catchment funnel (+0.6 block flare).
     */
    public static boolean isPointInHopperFunnel(Vec3 machinePos, float yRot, Vec3 worldPoint) {
        Vec3 delta = worldPoint.subtract(machinePos);

        double yawRad = Math.toRadians(yRot);
        Vec3 forward = new Vec3(-Math.sin(yawRad), 0.0D, Math.cos(yawRad));
        Vec3 right = new Vec3(Math.cos(yawRad), 0.0D, Math.sin(yawRad));

        double localForward = delta.dot(forward);
        double localRight = delta.dot(right);

        // Generous flared catchment funnel over and around the wide-angle hopper (+0.8 block flare).
        return Math.abs(localRight) <= 3.20D
                && Math.abs(localForward) <= 3.20D
                && delta.y >= 0.65D
                && delta.y <= 18.00D;
    }

    /**
     * Pull granular terrain that has landed in the hopper chute or on the feed deck
     * directly into the buffer. Wide-angle hopper captures material from a wider area (+0.6 block).
     */
    private void ingestHopperTerrain(ServerLevel level) {
        int room = CAPACITY - buffer.totalUnits();
        if (room <= 0) {
            return;
        }

        int budget = Math.min(room, 512);
        int minY = Mth.floor(getY() + 1.15D);
        int maxY = Mth.floor(getY() + 8.00D);
        Vec3 forward = forwardVector();
        Vec3 right = rightVector();

        for (double localForward = -2.40D; localForward <= 2.40D; localForward += 0.60D) {
            for (double localRight = -2.40D; localRight <= 2.40D; localRight += 0.60D) {
                if (budget <= 0) {
                    return;
                }

                Vec3 column = position()
                        .add(forward.scale(localForward))
                        .add(right.scale(localRight));
                int x = Mth.floor(column.x);
                int z = Mth.floor(column.z);

                for (int y = maxY; y >= minY && budget > 0; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    GranularComposition before = GroundworksApi.getComposition(level, pos);
                    if (before.isEmpty()) {
                        continue;
                    }

                    int want = Math.min(budget, before.totalUnits());
                    ExcavationResult dug = GroundworksApi.excavate(level, pos, want);
                    if (!dug.success()) {
                        continue;
                    }

                    GranularComposition taken = before.copy().extractProportional(dug.unitsRemoved());
                    int added = acceptComposition(taken);
                    int leftover = dug.unitsRemoved() - added;
                    if (leftover > 0 && dug.material() != null) {
                        GroundworksApi.deposit(level, pos, dug.material(), leftover);
                    }
                    budget -= added;
                }
            }
        }
    }

    private static void giveMachineItem(Player player) {
        ItemStack stack = new ItemStack(GoldRushGroundworksMod.GOLD_RUSH_SCREEN_ITEM);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        ItemStack held = player.getItemInHand(hand);

        if (player.isSecondaryUseActive() && held.isEmpty()) {
            if (!isEmpty()) {
                if (!level().isClientSide()) {
                    player.sendOverlayMessage(
                            Component.translatable("message.pw_gold_rush_groundworks.not_empty")
                    );
                }
                return InteractionResult.FAIL;
            }

            if (!level().isClientSide()) {
                if (!player.getAbilities().instabuild) {
                    giveMachineItem(player);
                }

                level().playSound(
                        null,
                        getX(),
                        getY(),
                        getZ(),
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );
                discard();
            }

            return InteractionResult.SUCCESS;
        }

        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (isGoldItem(held)) {
            setSelectedMaterial(GOLD_MATERIAL_ID);
            player.sendOverlayMessage(
                    Component.translatable(
                            "message.pw_gold_rush_groundworks.selected_material",
                            getMaterialNameComponent(GOLD_MATERIAL_ID)
                    )
            );
            level().playSound(
                    null,
                    getX(),
                    getY() + 1.0D,
                    getZ(),
                    SoundEvents.ARMOR_EQUIP_GOLD,
                    SoundSource.BLOCKS,
                    0.80F,
                    1.20F
            );
            return InteractionResult.SUCCESS;
        }

        GranularMaterial material = materialFromHeldBlock(held);
        int targetId;
        if (material != null && material != GranularMaterial.EMPTY) {
            targetId = material.id();
        } else {
            targetId = nextMaterialId(getSelectedMaterialId());
        }

        setSelectedMaterial(targetId);
        player.sendOverlayMessage(
                Component.translatable(
                        "message.pw_gold_rush_groundworks.selected_material",
                        getMaterialNameComponent(targetId)
                )
        );
        level().playSound(
                null,
                getX(),
                getY() + 1.0D,
                getZ(),
                SoundEvents.LEVER_CLICK,
                SoundSource.BLOCKS,
                0.65F,
                1.10F
        );

        return InteractionResult.SUCCESS;
    }

    public static boolean isGold(int materialId) {
        return materialId == GOLD_MATERIAL_ID;
    }

    public static boolean isGoldItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item == Items.GOLD_NUGGET
                || item == Items.RAW_GOLD
                || item == Items.GOLD_INGOT
                || item == Items.GOLD_BLOCK
                || item == Items.RAW_GOLD_BLOCK
                || item == Items.GOLD_ORE
                || item == Items.DEEPSLATE_GOLD_ORE
                || item == Items.NETHER_GOLD_ORE;
    }

    public static int nextMaterialId(int current) {
        return switch (current) {
            case 1 -> 2; // dirt -> sand
            case 2 -> 3; // sand -> gravel
            case 3 -> 4; // gravel -> cobblestone
            case 4 -> GOLD_MATERIAL_ID; // cobblestone -> gold
            default -> 1; // gold (5) -> dirt
        };
    }

    public static Component getMaterialNameComponent(int materialId) {
        return switch (materialId) {
            case 1 -> Component.translatable("material.pw_gold_rush_groundworks.dirt");
            case 2 -> Component.translatable("material.pw_gold_rush_groundworks.sand");
            case 3 -> Component.translatable("material.pw_gold_rush_groundworks.gravel");
            case 4 -> Component.translatable("material.pw_gold_rush_groundworks.cobblestone");
            case GOLD_MATERIAL_ID -> Component.translatable("material.pw_gold_rush_groundworks.gold");
            default -> {
                GranularMaterial mat = GranularMaterialRegistry.byId(materialId);
                yield mat != null && mat != GranularMaterial.EMPTY
                        ? Component.literal(mat.name())
                        : Component.translatable("material.pw_gold_rush_groundworks.gold");
            }
        };
    }

    @Nullable
    private static GranularMaterial materialFromHeldBlock(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }

        return GranularMaterialRegistry.forBlockState(blockItem.getBlock().defaultBlockState());
    }

    public void setSelectedMaterial(int materialId) {
        entityData.set(SELECTED_MATERIAL_ID, Math.max(1, materialId));
        syncState();
    }

    private void syncState() {
        int total = Mth.clamp(buffer.totalUnits(), 0, CAPACITY);
        entityData.set(STORED_UNITS, total);

        int selectedId = getSelectedMaterialId();
        int selectedUnits;
        if (selectedId == GOLD_MATERIAL_ID) {
            int gravelId = GranularMaterialRegistry.GRAVEL != null ? GranularMaterialRegistry.GRAVEL.id() : 3;
            selectedUnits = buffer.unitsOf(gravelId);
            if (selectedUnits == 0) {
                int sandId = GranularMaterialRegistry.SAND != null ? GranularMaterialRegistry.SAND.id() : 2;
                selectedUnits = buffer.unitsOf(sandId);
            }
        } else {
            selectedUnits = buffer.unitsOf(selectedId);
        }
        entityData.set(SELECTED_UNITS, selectedUnits);

        entityData.set(REMAINDER_MATERIAL_ID, dominantRemainderMaterialId());
    }

    public int dominantRemainderMaterialId() {
        int selected = getSelectedMaterialId();
        int[] counts = buffer.toArray();
        int bestId = 0;
        int bestCount = 0;
        int effectiveSelected = selected == GOLD_MATERIAL_ID
                ? (GranularMaterialRegistry.GRAVEL != null ? GranularMaterialRegistry.GRAVEL.id() : 3)
                : selected;

        for (int id = 1; id < counts.length; id++) {
            if (id == effectiveSelected) {
                continue;
            }
            if (counts[id] > bestCount) {
                bestCount = counts[id];
                bestId = id;
            }
        }
        if (bestId > 0) {
            return bestId;
        }
        return effectiveSelected == GranularMaterialRegistry.DIRT.id()
                ? GranularMaterialRegistry.GRAVEL.id()
                : GranularMaterialRegistry.DIRT.id();
    }

    public int getSelectedMaterialId() {
        return entityData.get(SELECTED_MATERIAL_ID);
    }

    public int getRemainderMaterialId() {
        return entityData.get(REMAINDER_MATERIAL_ID);
    }

    public int getStoredUnitsForRender() {
        return entityData.get(STORED_UNITS);
    }

    public int getSelectedUnitsForRender() {
        return entityData.get(SELECTED_UNITS);
    }

    public boolean hasSelectedStream() {
        return isActive() && getStoredUnitsForRender() > 0 && getSelectedUnitsForRender() > 0;
    }

    public boolean hasRemainderStream() {
        int total = getStoredUnitsForRender();
        int selected = getSelectedUnitsForRender();
        int remainder = total - selected;
        return isActive() && total > 0 && remainder > 0;
    }

    public boolean isActive() {
        return entityData.get(ACTIVE);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        setSelectedMaterial(input.getIntOr("SelectedMaterialId", DEFAULT_SELECTED_MATERIAL_ID));
        goldYieldWeightedCarry = Math.floorMod(
                input.getLongOr("GoldYieldWeightedCarry", 0L),
                GoldYieldLogic.REFERENCE_UNITS
        );

        int count = Math.max(
                GranularMaterialRegistry.count(),
                DEFAULT_SELECTED_MATERIAL_ID + 1
        );
        int[] units = new int[count];

        for (int id = 1; id < count; id++) {
            units[id] = Math.max(0, input.getIntOr("BufferMaterial_" + id, 0));
        }

        buffer.replaceWith(units);

        if (buffer.totalUnits() > CAPACITY) {
            GranularComposition trimmed = buffer.extractProportional(CAPACITY);
            buffer.replaceWith(trimmed.toArray());
        }

        syncState();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("SelectedMaterialId", getSelectedMaterialId());
        output.putLong("GoldYieldWeightedCarry", goldYieldWeightedCarry);

        int[] units = buffer.toArray();
        for (int id = 1; id < units.length; id++) {
            if (units[id] > 0) {
                output.putInt("BufferMaterial_" + id, units[id]);
            }
        }
    }

    @Override
    public boolean hurtClient(DamageSource source) {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (isInvulnerableToBase(source)) {
            return false;
        }

        if (!(source.getEntity() instanceof Player player)) {
            return false;
        }

        if (!isEmpty()) {
            return false;
        }

        level.playSound(
                null,
                getX(),
                getY(),
                getZ(),
                SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,
                SoundSource.PLAYERS,
                0.75F,
                1.40F
        );

        if (!player.getAbilities().instabuild) {
            giveMachineItem(player);
        }

        discard();
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity other) {
        return other != null;
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return other != null;
    }

    @Override
    public boolean isClientAuthoritative() {
        return false;
    }

    @Override
    protected boolean isLocalClientAuthoritative() {
        return false;
    }

    @Override
    public float maxUpStep() {
        return 0.0F;
    }
}
