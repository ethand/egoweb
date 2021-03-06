package net.sf.egonet.model;

import java.lang.reflect.Field;
import java.util.Random;

import org.uncommons.maths.random.MersenneTwisterRNG;

public abstract class Entity implements java.io.Serializable
{
    private Long id;

    private Boolean active;
    
	private static Random random;
	private Long randomKey;

	public Entity() {
		setActive(true);
	}
	
    public Long getId()
    {
        return id;
    }

	public void setId(Long id)
    {
		this.id = id;
	}

	// ----------------------------------------

	// pp 396-400 of Java Persistence with Hibernate discuss the importance of
	// providing equals and hashCode for persistence classes.
	// If we avoid (sets or (detached and unsaved entities)) and cascade save
	// then we won't need to worry about equals and hashCode.

	public boolean equals(Object obj)
	{
		if(! (obj instanceof Entity)) {
			return false;
		}
		Entity entity = (Entity) obj;
		if(! getRandomKey().equals(entity.getRandomKey())) {
			return false;
		}
		return getId() == null || entity.getId() == null || getId().equals(entity.getId());
	}

	public int hashCode()
	{
		return getRandomKey().hashCode();
	}

	public Long getRandomKey() {
		if(randomKey == null) {
			randomKey = generateRandom();
		}
		return randomKey;
	}
	
	// ----------------------------------------

	public void setRandomKey(Long randomKey) {
		this.randomKey = randomKey;
	}

	private static Long generateRandom() {
		if(random == null) {
			random = new MersenneTwisterRNG();
		}
		return random.nextLong();
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getActive() {
		return active;
	}
	
	// -------------------------------------------
	
	protected String migrateToText(Entity entity, String fieldname) {
		try {
			Class<?> c = entity.getClass();
			Field oldField = c.getDeclaredField(fieldname+"Old");
			Field newField = c.getDeclaredField(fieldname);
			String oldVal = (String) oldField.get(entity);
			String newVal = (String) newField.get(entity);
			if((newVal == null || newVal.isEmpty()) && oldVal != null && ! oldVal.isEmpty()) {
				newField.set(entity, oldVal);
				oldField.set(entity, "");
			}
			return (String) newField.get(entity);
		} catch(Exception ex) {
			throw new RuntimeException("migrateToText failed",ex);
		}
	}
}
