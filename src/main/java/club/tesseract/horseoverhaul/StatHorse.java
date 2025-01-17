package club.tesseract.horseoverhaul;

import club.tesseract.horseoverhaul.attributes.PersistentAttribute;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Horse;

import club.tesseract.horseoverhaul.listener.BreedingListener;


/**
 * @see <a href="https://minecraft.gamepedia.com/Attribute">Gamepedia Attributes</a>
 * @see <a href="https://minecraft.gamepedia.com/Horse">Gamepedia Horse</a>
 */
public class StatHorse{


	private static final String heart = "❤";
	private static final String UNICODE_CIRCLE = "⬤";
	private static final String UNICODE_SQUARE = "⬛";
	
	/**
	 * fields
	 */
	
	public AbstractHorse roach;
	byte food;
	

	public StatHorse(AbstractHorse horse) {
		roach = horse;
		food = 0;
	}
	
	public StatHorse(AbstractHorse horse, byte f) {
		roach = horse;
		food = BreedingListener.FOOD_EFFECTS ? f : 0;
	}

	public double getJumpHeight() {
		double x = roach.getJumpStrength();
		
		return -0.1817584952 * Math.pow(x, 3) + 3.689713992 * Math.pow(x, 2) + 2.128599134 * x - 0.343930367;
	}
	
	public int getHealth() {
		return (int)(roach.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()/2);
	}
	
	public double getSpeed() {
		double x = roach.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
		return 43.178 * x - 0.02141;
	}


	public void calculateBirth(AbstractHorse mother, AbstractHorse father) {
		if(food == (byte)2) {
			roach.setJumpStrength(1.0);
			roach.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30);
			roach.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3375);
			return;
		}
		double fatherJump, fatherHealth, fatherSpeed, motherJump, motherHealth, motherSpeed;
		
		fatherJump = father.getJumpStrength();
		fatherHealth = father.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
		fatherSpeed = father.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
		
		motherJump = mother.getJumpStrength();
		motherHealth = mother.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
		motherSpeed = mother.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
		
		roach.setJumpStrength(calcJump(fatherJump, motherJump));
		roach.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(calcHealth(fatherHealth, motherHealth));
		roach.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(calcSpeed(fatherSpeed, motherSpeed));
	}


	// Breeding calculations //

	/**
	 * @param fatherStat: mother's stat, on scale of 0-1.0
	 * @param motherStat: father's stat, on scale of 0-1.0
	 * @return double on scale of 0-1.0 that will determine the child's attribute
	 */
	private double randomizer(double fatherStat, double motherStat) {
		double min = Math.min(fatherStat, motherStat);
		double max = Math.max(fatherStat, motherStat);
		double nmax, nmin;
		
		double offset = Math.pow(max-min, 3) * 3.5;
		nmax = max + ( (1-max-offset) / 4 );
		nmin = min - ( (1-min) /5.5);
		
		double foal = Math.random() * (nmax-nmin) + nmin;
		
		if(food!=0 && foal<min) foal = min;
		
		return Math.min(foal, 1.0);
		
	}
	
	/**
	 * 	Horse Jump height can range from 0.4-1.0, average is 0.7
	 */
	private double calcJump(double f, double m) {
		
		double mc = (m - .4)  / 0.6;
		double fc = (f - .4) / 0.6;
		
		double child = randomizer(mc, fc);
		return child * 0.6 + 0.4;
	}
	
	/**
	 * Health can range from 15-30, average is 22-23
	 */
	private double calcHealth(double f, double m) {
		 
		double mc = (m - 15) / 15;
		double fc = (f - 15) / 15;
		
		double child = randomizer(mc, fc);
		return child * 15 + 15;
	}
	
	/**
	 * Speed ranges from 0.1125 - 0.3375, average is 0.225
	 */
	private double calcSpeed(double f, double m) {
		
		double mc = (m - 0.1125) / 0.225;
		double fc = (f - 0.1125) / 0.225;
		
		double child = randomizer(mc, fc);
		return child * 0.225 + 0.1125;
	}
	
	
	/**
	 * Methods for printing/display a horse's stats
	 */
	public String printStats(boolean border) {
		String msg = "";
		if(roach == null)return msg;
		if(roach.getCustomName()!=null) {
			
			String stripped = ChatColor.stripColor(roach.getCustomName());
			
			StringBuilder line = new StringBuilder(ChatColor.GRAY.toString());
			line.append("-".repeat(Math.max(0, stripped.length() + 6)));
			line.append("\n").append(ChatColor.RESET);
			
			msg+= line;
			
			if( roach.getCustomName().equals(stripped) )
				msg += ChatColor.DARK_AQUA;
			
			msg += (roach.getCustomName() + "'s Stats" + ChatColor.RESET + "\n");
			
			msg += line;
				
		}
		
		else if (roach instanceof Horse) {
			String color = ((Horse)this.roach).getColor().name();
			color = color.toCharArray()[0] + color.substring(1).toLowerCase();
			msg += (ChatColor.DARK_AQUA.toString() + ChatColor.UNDERLINE + color + " Horse's Stats") + ChatColor.RESET + "\n \n";
		}
		
		else {
			String type = roach.getType().name();
			type = type.toCharArray()[0] + type.substring(1).toLowerCase();
			msg += (ChatColor.DARK_AQUA.toString() + ChatColor.UNDERLINE + type + "'s Stats") + ChatColor.RESET + "\n \n";
		}
			
		
		msg += ChatColor.RED + "Health:\n" + printHearts(getHealth()) + " " + ChatColor.RED + HorseOverhaul.statNumberFormat.format(getHealth()) + "h\n";
		msg += ChatColor.GREEN + "Speed:\n" + printSpeed(getSpeed()) + " " + ChatColor.GREEN + HorseOverhaul.statNumberFormat.format(getSpeed()) + "m/s\n";
		msg += ChatColor.BLUE + "Jump Height:\n" + printJump(getJumpHeight()) + " " + ChatColor.BLUE + HorseOverhaul.statNumberFormat.format(getJumpHeight()) + "m\n";
		
		
		if(border) {
			String bord = ChatColor.LIGHT_PURPLE + "-----------------------------------------------------";
			return bord + "\n" + msg + bord;
		}
		else
			return msg + ChatColor.YELLOW + "Can Breed:\n" + (PersistentAttribute.NEUTERED.getData(roach, (byte)0) == (byte)1 ? ChatColor.LIGHT_PURPLE + "False" : ChatColor.LIGHT_PURPLE + "True") + "\n";
	}
		
	private String printJump(double jh) {
		
		String msg = "";
		double b = 0;
		StringBuilder blocks = new StringBuilder();
		
		while(jh - b >= 0.2625) {
			
			blocks.append(UNICODE_SQUARE);
			b += 0.525;
			
		}
		
		msg += ChatColor.DARK_BLUE + blocks.toString();
		
		blocks = new StringBuilder();
		
		while(b < 5.25) {
			
			blocks.append(UNICODE_SQUARE);
			b += 0.525;
			
		}
		
		msg += ChatColor.GRAY + blocks.toString();
		return msg;
		
	}
	private String printSpeed(double sp) {
		
		String msg = "";
		int b = 0;
		StringBuilder rate = new StringBuilder();
		
		while(sp - b >= 0.5) {
			
			rate.append(UNICODE_CIRCLE);
			b++;
		}
		
		msg += ChatColor.DARK_GREEN + rate.toString();
		
		rate = new StringBuilder();
		
		while(b < 14.5125) {
			
			rate.append(UNICODE_CIRCLE);
			b++;
			
		}
		
		msg += ChatColor.GRAY + rate.toString();
		return msg;
		
	}
	
	private String printHearts(int hp) {
		
		String msg = "";
		int s = 0;
		StringBuilder hearts = new StringBuilder();
		
		while(s<hp) {
			
			hearts.append(heart);
			s++;
			
		}
		
		msg += ChatColor.DARK_RED + hearts.toString();
		
		hearts = new StringBuilder();
		
		while(s < 15) {
			
			hearts.append(heart);
			s++;
			
		}	
		
		msg += ChatColor.GRAY + hearts.toString();
		return msg;
		
	}
	
}
