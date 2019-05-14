package juuxel.adorn.entity

import juuxel.adorn.block.SeatBlock
import juuxel.adorn.lib.ModNetworking
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.client.network.packet.EntityPositionS2CPacket
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SittingVehicleEntity(type: EntityType<*>, world: World) : Entity(type, world) {
    init {
        noClip = true
        isInvulnerable = true
    }

    fun setPos(pos: BlockPos, blockOffset: Double) {
        check(!world.isClient) {
            "setPos must be called on the logical server"
        }
        x = pos.x + 0.5
        y = pos.y + 0.25 + blockOffset
        z = pos.z + 0.5
        PlayerStream.watching(this).forEach {
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(it, EntityPositionS2CPacket(this))
        }
    }

    override fun interact(player: PlayerEntity, hand: Hand): Boolean {
        player.startRiding(this)
        return true
    }

    override fun removePassenger(entity: Entity?) {
        super.removePassenger(entity)
        kill()
    }

    override fun kill() {
        super.kill()
        val pos = BlockPos(this)
        val state = world.getBlockState(pos)
        if (state.block is SeatBlock) {
            world.setBlockState(pos, state.with(SeatBlock.OCCUPIED, false))
        }
    }

    override fun canClimb() = false
    override fun collides() = false
    override fun getMountedHeightOffset() = 0.0
    override fun createSpawnPacket() = ModNetworking.createEntitySpawnPacket(this)
    override fun hasNoGravity() = true
    override fun isInvisible() = true

    override fun initDataTracker() {}
    override fun readCustomDataFromTag(tag: CompoundTag?) {}
    override fun writeCustomDataToTag(tag: CompoundTag?) {}
}
