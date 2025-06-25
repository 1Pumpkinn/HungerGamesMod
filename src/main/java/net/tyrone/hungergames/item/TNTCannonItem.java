package net.tyrone.hungergames.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;

public class TNTCannonItem extends Item {
    public TNTCannonItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            Vec3d direction = user.getRotationVec(1.0F).normalize();
            Vec3d pos = user.getPos().add(direction.multiply(2)).add(0, 1, 0);

            TntEntity tnt = new TntEntity(EntityType.TNT, world);
            tnt.setPos(pos.x, pos.y, pos.z);
            tnt.setFuse(40); // 2 seconds fuse
            tnt.setVelocity(direction.x, direction.y + 0.5, direction.z);
            world.spawnEntity(tnt);

            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.PLAYERS, 1f, 1f);
        }

        user.getItemCooldownManager().set((Item) this, 20); // 1 second cooldown
        return ActionResult.SUCCESS;
    }
}