package com.piotrek.goldrushgroundworks.client.render;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class GoldRushScreenRenderState extends EntityRenderState {
    public float baseYaw;
    public boolean active;
    public float beltPhase;
    public int selectedMaterialId;
    public int remainderMaterialId;
    public int storedUnits;
    public int selectedUnits;
    public boolean hasSelectedStream;
    public boolean hasRemainderStream;
}
