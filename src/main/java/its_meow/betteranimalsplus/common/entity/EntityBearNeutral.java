package its_meow.betteranimalsplus.common.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIFindEntityNearest;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityBearNeutral extends EntityBear {
	
	
	private int warningSoundTicks;
	private World world = null;

	public EntityBearNeutral(World worldIn) {
		super(worldIn);
		this.world = worldIn;
		this.setSize(2F, 1.5F);
	}

	@Override
	protected void initEntityAI()
    {
        //super.initEntityAI();
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityBearNeutral.AIMeleeAttack());
        this.targetTasks.addTask(1, new EntityBearNeutral.AIHurtByTarget());
        this.tasks.addTask(5, new EntityAIWander(this, 0.5D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityChicken>(this, EntityChicken.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityRabbit>(this, EntityRabbit.class, true));
        //TODO: Once pheasants are added target them
    }

	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
	}
	
	@Override
	public void setAttackTarget(EntityLivingBase entitylivingbaseIn) {
		if(world.getDifficulty() == EnumDifficulty.PEACEFUL) {
			super.setAttackTarget(null);
		} else {
			super.setAttackTarget(entitylivingbaseIn);
		}
	}
	
	/**
	 * Checks if the entity's current position is a valid location to spawn this entity.
	 */
	public boolean getCanSpawnHere()
	{
		return this.world.getDifficulty() != EnumDifficulty.PEACEFUL;
	}

	public static void registerFixesBearNeutral(DataFixer fixer)
	{
		EntityLiving.registerFixesMob(fixer, EntityBearNeutral.class);
	}


	/**
	 * Called when the entity is attacked.
	 */
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		if (this.isEntityInvulnerable(source))
		{
			return false;
		}
		else
		{
			Entity entity = source.getTrueSource();

			if (entity instanceof EntityPlayer)
			{
				this.setAttackTarget((EntityPlayer) entity);
				this.playWarningSound();
			}

			return super.attackEntityFrom(source, amount);
		}
	}
	
	public void onUpdate()
    {
        super.onUpdate();


        if (this.warningSoundTicks > 0)
        {
            --this.warningSoundTicks;
        }
    }

	protected void playWarningSound()
    {
        if (this.warningSoundTicks <= 0)
        {
            this.playSound(SoundEvents.ENTITY_POLAR_BEAR_WARNING, 1.0F, 1.0F);
            this.warningSoundTicks = 40;
        }
    }

	protected SoundEvent getAmbientSound()
	{
		return SoundEvents.ENTITY_POLAR_BEAR_AMBIENT;
	}

	protected SoundEvent getHurtSound(DamageSource damageSourceIn)
	{
		return SoundEvents.ENTITY_POLAR_BEAR_HURT;
	}

	protected SoundEvent getDeathSound()
	{
		return SoundEvents.ENTITY_POLAR_BEAR_DEATH;
	}

	protected void playStepSound(BlockPos pos, Block blockIn)
	{
		this.playSound(SoundEvents.ENTITY_POLAR_BEAR_STEP, 0.15F, 1.0F);
	}

	public boolean processInteract(EntityPlayer player, EnumHand hand)
	{
		return false;
	}

	public boolean isPreventingPlayerRest(EntityPlayer playerIn)
	{
		return world.getDifficulty() != EnumDifficulty.PEACEFUL && this.getAttackTarget() == playerIn;
	}

	public class AIHurtByTarget extends EntityAIHurtByTarget
	{
		public AIHurtByTarget()
		{
			super(EntityBearNeutral.this, false);
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		public void startExecuting()
		{
			super.startExecuting();

		}

		protected void setEntityAttackTarget(EntityCreature creatureIn, EntityLivingBase entityLivingBaseIn)
		{
			if (creatureIn instanceof EntityBearNeutral)
			{
				super.setEntityAttackTarget(creatureIn, entityLivingBaseIn);
			}
		}
	}

	public class AIMeleeAttack extends EntityAIAttackMelee
	{
		public AIMeleeAttack()
		{
			super(EntityBearNeutral.this, 1.25D, true);
		}

		protected void checkAndPerformAttack(EntityLivingBase p_190102_1_, double p_190102_2_)
		{
			double d0 = this.getAttackReachSqr(p_190102_1_);

			if (p_190102_2_ <= d0 && this.attackTick <= 0)
			{
				this.attackTick = 20;
				this.attacker.attackEntityAsMob(p_190102_1_);
			}
			else if (p_190102_2_ <= d0 * 2.0D)
			{
				if (this.attackTick <= 0)
				{
					this.attackTick = 20;
				}

				if (this.attackTick <= 10)
				{
					EntityBearNeutral.this.playWarningSound();
				}
			}
			else
			{
				this.attackTick = 20;
			}
		}

		/**
		 * Reset the task's internal state. Called when this task is interrupted by another one
		 */
		public void resetTask()
		{
			super.resetTask();
		}

		protected double getAttackReachSqr(EntityLivingBase attackTarget)
		{
			return (double)(4.0F + attackTarget.width);
		}
	}
}
