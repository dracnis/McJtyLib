package mcjty.lib.compat.theoneprobe;

import mcjty.lib.api.infusable.CapabilityInfusable;
import mcjty.lib.base.GeneralConfig;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.multipart.MultipartBlock;
import mcjty.lib.multipart.MultipartHelper;
import mcjty.lib.multipart.MultipartTE;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class McJtyLibTOPDriver implements TOPDriver {

    public static final McJtyLibTOPDriver DRIVER = new McJtyLibTOPDriver();

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        if (blockState.getBlock() == Registration.MULTIPART_BLOCK) {
            MultipartTE.Part part = MultipartBlock.getHitPart(blockState, world, data.getPos(), MultipartHelper.getPlayerEyes(player), data.getHitVec());
            if (part != null) {
                if (part.getTileEntity() instanceof TOPInfoProvider) {
                    TOPDriver driver = ((TOPInfoProvider) part.getTileEntity()).getProbeDriver();
                    if (driver != null) {
                        driver.addProbeInfo(mode, probeInfo, player, world, blockState, data);
                    }
                } else if (part.getState().getBlock() instanceof TOPInfoProvider) {
                    TOPDriver driver = ((TOPInfoProvider) part.getState().getBlock()).getProbeDriver();
                    driver.addProbeInfo(mode, probeInfo, player, world, blockState, data);
                }
            }
        } else if (blockState.getBlock() instanceof BaseBlock) {
            addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
        }
    }

    public void addStandardProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof GenericTileEntity) {
            te.getCapability(CapabilityInfusable.INFUSABLE_CAPABILITY).ifPresent(h -> {
                int infused = h.getInfused();
                int pct = infused * 100 / GeneralConfig.maxInfuse.get();
                probeInfo.text(TextFormatting.YELLOW + "Infused: " + pct + "%");
            });
            GenericTileEntity generic = (GenericTileEntity) te;
            if (mode == ProbeMode.EXTENDED) {
                if (GeneralConfig.manageOwnership.get()) {
                    if (generic.getOwnerName() != null && !generic.getOwnerName().isEmpty()) {
                        int securityChannel = generic.getSecurityChannel();
                        if (securityChannel == -1) {
                            probeInfo.text(TextFormatting.YELLOW + "Owned by: " + generic.getOwnerName());
                        } else {
                            probeInfo.text(TextFormatting.YELLOW + "Owned by: " + generic.getOwnerName() + " (channel " + securityChannel + ")");
                        }
                        if (generic.getOwnerUUID() == null) {
                            probeInfo.text(TextFormatting.RED + "Warning! Ownership not correctly set! Please place block again!");
                        }
                    }
                }
            }
        }
    }

}