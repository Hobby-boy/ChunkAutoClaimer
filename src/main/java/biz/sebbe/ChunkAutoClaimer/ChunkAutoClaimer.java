package biz.sebbe.ChunkAutoClaimer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;



/*
 * EXPLANATION OF CONFIG VARIABLES:
 * messages/*: Localization system
 * configversion: Allows updating of stale configs
 * maxChunks: MAX number of chunks a player may claim. A admin may through command /set override this value
 * autoClaim: Number of NON-IGNORED blocks you need to place to automatically claim the chunk.
 * autoLeave: Number of NON-IGNORED blocks you need to remove to automatically abandon the chunk.
 * maxFriends: Number of FRIENDS a player may have on friendslist.
 * requireAdjacent: Require that player claim chunks adjacent to each other. Does not apply to first chunk (even if player previously owned a chunk).
 * ignoreBlockList: Blocks that are not counted when deciding if a chunk should be claimed or not.
 * lastChunk: The last chunk the player placed or removed a block in.
 * BlockCount: The number of blocks the player successively placed or removed in a chunk.
 * chunkOwner: The owner of the chunk the player is currently standing in.
 * gotWarning: Prevents the plugin from being spammy. This variable remembers if the player got a
 *  **** warning/error message that results in the chunk cannot be claimed, so the plugin does not
 *  **** give the warning over, over and over again. Variable is reset whenever the player places or
 *  **** removes a block in a valid claim or valid unowned space, OR if the player moves to a new chunk.
 *  claimsList: All claims a specific player owns. Good for deleting all claims a specific player owns.
 *  **** for example if the player fucks up and want to start over.
 *  walktest: Stores /walktest status for that specific player.
 *  allowFireSpread: Controls burn, spread, igniting and placing of fire outside of claims above ground
 *  allowUGFireSpread: Controls burn, spread, igniting and placing of fire outside of claims below ground
 *  allowtnt: Decides if TNT should have any effects ABOVE ground.
 *  allowugtnt: Decides if TNT should have any effects BELOW ground.
 *  allowwater: Decides if water place is allowed ABOVE ground.
 *  allowugwater: Decides if water place is allowed BELOW ground.
 *  allowlava: Decides if lava place is allowed ABOVE ground.
 *  allowuglava: Decides if lava place is allowed BELOW ground.
 *  belowGround: Decides ground level
 *  markClaims: Enables/disables marking of chunks.
 *  markBlock: Sets blockID to mark WITH
 *  markData: Sets block Data (for example wool color) to mark WITH
 */

public class ChunkAutoClaimer extends JavaPlugin implements Listener {
    public int maxChunks;
    public int autoClaim;
    public int autoLeave;
    public int maxFriends;
    public int configVersion;
    public int belowGround;
    public int markBlock;
    public int markData;
    public boolean markClaims;
    public boolean allowLava;
    public boolean allowWater;
    public boolean allowTNT;
    public boolean allowFireSpread;
    public boolean allowUGLava;
    public boolean allowUGWater;
    public boolean allowUGTNT;
    public boolean allowUGFireSpread;
    public boolean requireAdjacent;
    public List<Integer> ignoreBlockList;
    public String loc_chunkisunprotected;
    public String loc_canunprotected;
    public String loc_walkteston;
    public String loc_walktesthelp;
    public String loc_canop;
    public String loc_canowner;
    public String loc_canfriends;
    public String loc_cannotprotected;
    public String loc_alreadyonfriends;
    public String loc_successfullyfriend;
    public String loc_removewithdel;
    public String loc_friendsfull;
    public String loc_clearwithdel;
    public String loc_friendsusage;
    public String loc_walktestona;
    public String loc_walktestonb;
    public String loc_walktestoffa;
    public String loc_walktestoffb;
    public String loc_notonfriends;
    public String loc_removedfriend;
    public String loc_clearedfriends;
    public String loc_chunkowner;
    public String loc_alreadyunprotect;
    public String loc_successunprotect;
    public String loc_successprotect;
    public String loc_changedowner;
    public String loc_setaccessdenied;
    public String loc_resetsuccessful;
    public String loc_changedall;
    public String loc_equalerror;
    public String loc_resetaccessdenied;
    public String loc_consoleaccessdenied;
    public String loc_claimed;
    public String loc_checkprotection;
    public String loc_maxclaimsexceed;
    public String loc_resetinstructions;
    public String loc_noprotection;
    public String loc_notadjacent;
    public String loc_unclaimed;
    public String loc_joinmessage;
    public String loc_joinwalktest;
    public String loc_wilderness;
    
	@SuppressWarnings({ "unchecked" })
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		int chunkX;
		int chunkY;
		String chunkOwner;

		ArrayList<String> friendsListOwner;
		ArrayList<String> friendsListPlayer;
		ArrayList<String> allclaims;
		ArrayList<String> newallclaims;
		String chunktoremove;
		String CommandArgument;
		String SetAllTarget;
		int chunktoremoveX;
		int chunktoremoveY;
		boolean walktest;
		ArrayList<String> emptyList = new ArrayList<String>(Arrays.asList(new String[] {}));
		if (args.length == 0) {
			CommandArgument = "";
			SetAllTarget = "";
		}
		else
		{
			CommandArgument = args[0];
			CommandArgument = CommandArgument.replaceAll("[^abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_]", "");
			if (CommandArgument.length() > 16) {	
				CommandArgument = CommandArgument.substring(0,16);
			}
			if (CommandArgument.length() < 3) {
				CommandArgument = "";
				SetAllTarget = "";
			}
			else
			{
				if (args.length > 1) {
					SetAllTarget = args[1];
					SetAllTarget = SetAllTarget.replaceAll("[^abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_]", "");
					if (SetAllTarget.length() > 16) {	
						SetAllTarget = SetAllTarget.substring(0,16);
					}
					if (SetAllTarget.length() < 3) {
						SetAllTarget = "";
					}
				}
				else
				{
					SetAllTarget = "";
				}
			}
		}
		SetAllTarget = SetAllTarget.toLowerCase();
		CommandArgument = CommandArgument.toLowerCase();
		if ((sender instanceof Player)) {
			chunkX = ((Player) sender).getLocation().getChunk().getX();
			chunkY = ((Player) sender).getLocation().getChunk().getZ();
			chunkOwner = getConfig().getString("main.claims." + String.valueOf(chunkX) + "," + String.valueOf(chunkY), null);
			if (chunkOwner == null) {
				chunkOwner = "";
			}
			chunkOwner = chunkOwner.toLowerCase();
			if (!(chunkOwner.equals(""))) {
				friendsListOwner = new ArrayList<String>((List<String>) getConfig().getList("main.players." + chunkOwner.toLowerCase() + ".friends", emptyList));
			}
			else
			{
				friendsListOwner = new ArrayList<String>(emptyList);
			}
			friendsListPlayer = new ArrayList<String>((List<String>) getConfig().getList("main.players." + sender.getName().toLowerCase() + ".friends", emptyList));
			if (cmd.getName().equalsIgnoreCase("chk")) {
				markClaim((Player) sender);
				if (chunkOwner.equals("")) {		
					sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_chunkisunprotected.replaceAll("##X##", String.valueOf(chunkX)).replaceAll("##Y##", String.valueOf(chunkY)));
					sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_canunprotected);
				}
				else
				{
					walktest =  getConfig().getBoolean("main.players." + sender.getName().toLowerCase() + ".walktest", false);
					sender.sendMessage(ChatColor.RED + "[CAC] "+ loc_chunkowner.replaceAll("##X##", String.valueOf(chunkX)).replaceAll("##Y##", String.valueOf(chunkY)).replaceAll("##N##", chunkOwner));
					if (walktest) {
						sender.sendMessage(ChatColor.RED + "[CAC] " + loc_walkteston);
						sender.sendMessage(ChatColor.RED + "[CAC] " + loc_walktesthelp);
					}
					else
					{
						if (sender.isOp()||sender.hasPermission("chunkautoclaimer.op")) {
							sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_canop);
						}
						else
						{
							if (chunkOwner.equals(sender.getName().toLowerCase())) {
								sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_canowner);
							}
							else
							{
								if (friendsListOwner.contains(sender.getName().toLowerCase())) {
									sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_canfriends);
								}
								else
								{
									sender.sendMessage(ChatColor.RED + "[CAC] " + loc_cannotprotected);
								}
							}
						}
					}
										
				}
				
			}
			
			if (cmd.getName().equalsIgnoreCase("add")) {
				if (CommandArgument.length() > 0) {
					if (friendsListPlayer.contains(CommandArgument.toLowerCase())) {
						sender.sendMessage(ChatColor.RED + "[CAC] " + loc_alreadyonfriends.replaceAll("##N##", CommandArgument.toLowerCase()));
					}
					else
					{
						if ((friendsListPlayer.size() < maxFriends) || sender.isOp() || sender.hasPermission("chunkautoclaimer.unlimited")) { 
							friendsListPlayer.add(CommandArgument.toLowerCase());
							sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_successfullyfriend.replaceAll("##N##", CommandArgument.toLowerCase()));
							sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_removewithdel);
						}
						else
						{
							sender.sendMessage(ChatColor.RED + "[CAC] " + loc_friendsfull);
							sender.sendMessage(ChatColor.RED + "[CAC] " + loc_removewithdel);
							sender.sendMessage(ChatColor.RED + "[CAC] " + loc_clearwithdel);
						}
					}
					getConfig().set("main.players." + sender.getName().toLowerCase() + ".friends", friendsListPlayer);
					saveConfig();
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "[CAC] " + loc_friendsusage);
				}
			}
			
			
			
			if (cmd.getName().equalsIgnoreCase("walktest")) {
					walktest =  getConfig().getBoolean("main.players." + sender.getName().toLowerCase() + ".walktest", false);
					walktest = !walktest;
					getConfig().set("main.players." + sender.getName().toLowerCase() + ".walktest", walktest);
					saveConfig();
					if (walktest) {
						sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_walktestona);
						sender.sendMessage(ChatColor.RED + "[CAC] " + loc_walktestonb);
					}
					else
					{
						sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_walktestoffa);
						sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_walktestoffb);
					}
			}
			
			
			
			if (cmd.getName().equalsIgnoreCase("del")) {
				if (CommandArgument.length() > 0) {
					if (!friendsListPlayer.contains(CommandArgument.toLowerCase())) {
						sender.sendMessage(ChatColor.RED + "[CAC] " + loc_notonfriends.replaceAll("##N##", CommandArgument.toLowerCase()));
					}
					else
					{
						friendsListPlayer.remove(CommandArgument.toLowerCase());
						sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_removedfriend.replaceAll("##N##", CommandArgument.toLowerCase()));
					}
					getConfig().set("main.players." + sender.getName().toLowerCase() + ".friends", friendsListPlayer);
					saveConfig();
				}
				else
				{
					friendsListPlayer.clear();
					getConfig().set("main.players." + sender.getName().toLowerCase() + ".friends", friendsListPlayer);
					saveConfig();
					sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_clearedfriends);
				}
			}
			

			if (cmd.getName().equalsIgnoreCase("set")) {
				if (sender.isOp() || sender.hasPermission("chunkautoclaimer.op")) {
					markClaim((Player) sender);
					if (!chunkOwner.equals("")) {
						allclaims = new ArrayList<String>((List<String>) getConfig().getList("main.players." + chunkOwner + ".claims",emptyList));
					}
					else
					{
						allclaims = emptyList;
					}
        			if (CommandArgument.length() == 0) {
						if (chunkOwner.equals("")) {
							sender.sendMessage(ChatColor.RED + "[CAC] " + loc_alreadyunprotect.replaceAll("##X##", String.valueOf(chunkX)).replaceAll("##Y##", String.valueOf(chunkY)));
						}
						else
						{
							getConfig().set("main.claims." + String.valueOf(chunkX) + "," + String.valueOf(chunkY), null);
							allclaims.remove(String.valueOf(chunkX) + "," + String.valueOf(chunkY));
							sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_successunprotect.replaceAll("##X##", String.valueOf(chunkX)).replaceAll("##Y##", String.valueOf(chunkY)));
						}
					}
					else
					{
						if (chunkOwner.equals(CommandArgument.toLowerCase())) {
							sender.sendMessage(ChatColor.RED + "[CAC] " + loc_equalerror);
						}
						else
						{
							newallclaims = new ArrayList<String>((List<String>) getConfig().getList("main.players." + CommandArgument.toLowerCase() + ".claims",emptyList));
	        				getConfig().set("main.claims." + String.valueOf(chunkX) + "," + String.valueOf(chunkY), CommandArgument.toLowerCase());
							newallclaims.add(String.valueOf(chunkX) + "," + String.valueOf(chunkY));
							if (chunkOwner.equals("")) {
								sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_successprotect.replaceAll("##X##", String.valueOf(chunkX)).replaceAll("##Y##", String.valueOf(chunkY)).replaceAll("##N##", CommandArgument.toLowerCase()));
							}
							else
							{
								getConfig().set("main.claims." + String.valueOf(chunkX) + "," + String.valueOf(chunkY), CommandArgument.toLowerCase());
								allclaims.remove(String.valueOf(chunkX) + "," + String.valueOf(chunkY));
								sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_changedowner.replaceAll("##X##", String.valueOf(chunkX)).replaceAll("##Y##", String.valueOf(chunkY)).replaceAll("##M##", CommandArgument.toLowerCase()).replaceAll("##N##", chunkOwner));
							}
							getConfig().set("main.players." + CommandArgument.toLowerCase() + ".claims", newallclaims);
						}
					}
					if (!chunkOwner.equals("")) {
						getConfig().set("main.players." + chunkOwner + ".claims", allclaims);
					}
					saveConfig();
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "[CAC] " + loc_setaccessdenied);
				}
			}
			
			if (cmd.getName().equalsIgnoreCase("reset")) {
				if (CommandArgument.length() == 0) {
				allclaims = new ArrayList<String>((List<String>) getConfig().getList("main.players." + sender.getName().toLowerCase() + ".claims",emptyList));
					for (Iterator<String> it = allclaims.iterator(); it.hasNext();) {
						chunktoremove = it.next();
						chunktoremoveX = Integer.valueOf(chunktoremove.split(",")[0]);
						chunktoremoveY = Integer.valueOf(chunktoremove.split(",")[1]);
						getConfig().set("main.claims." + String.valueOf(chunktoremoveX) + "," + String.valueOf(chunktoremoveY), null);
					}
					getConfig().set("main.players." + sender.getName().toLowerCase() + ".claims", new ArrayList<String>(emptyList));
					getConfig().set("main.players." + sender.getName().toLowerCase() + ".friends", new ArrayList<String>(emptyList));
					getConfig().set("main.players." + sender.getName().toLowerCase() + ".walktest", false);
					getConfig().set("main.players." + sender.getName().toLowerCase() + ".gotwarning", false);
					sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_resetsuccessful.replaceAll("##N##", sender.getName().toLowerCase()));
				}
				else
				{
					if (sender.isOp()  || sender.hasPermission("chunkautoclaimer.op")) {
						if (SetAllTarget.length() == 0) {
							allclaims = new ArrayList<String>((List<String>) getConfig().getList("main.players." + CommandArgument.toLowerCase() + ".claims",emptyList));
							for (Iterator<String> it = allclaims.iterator(); it.hasNext();) {
								chunktoremove = it.next();
								chunktoremoveX = Integer.valueOf(chunktoremove.split(",")[0]);
								chunktoremoveY = Integer.valueOf(chunktoremove.split(",")[1]);
								getConfig().set("main.claims." + String.valueOf(chunktoremoveX) + "," + String.valueOf(chunktoremoveY), null);
							}
							getConfig().set("main.players." + CommandArgument.toLowerCase() + ".claims", new ArrayList<String>(emptyList));
							getConfig().set("main.players." + CommandArgument.toLowerCase() + ".friends", new ArrayList<String>(emptyList));
							getConfig().set("main.players." + CommandArgument.toLowerCase() + ".walktest", false);
							getConfig().set("main.players." + CommandArgument.toLowerCase() + ".gotwarning", false);
							sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_resetsuccessful.replaceAll("##N##", CommandArgument.toLowerCase()));
						}
						else
						{
							if (SetAllTarget.toLowerCase().equalsIgnoreCase(CommandArgument.toLowerCase())) {
								sender.sendMessage(ChatColor.RED + "[CAC] " + loc_equalerror);
							}
							else
							{
								allclaims = new ArrayList<String>((List<String>) getConfig().getList("main.players." + CommandArgument.toLowerCase() + ".claims",emptyList));
								newallclaims = new ArrayList<String>((List<String>) getConfig().getList("main.players." + SetAllTarget.toLowerCase() + ".claims",emptyList));
								for (Iterator<String> it = allclaims.iterator(); it.hasNext();) {
									chunktoremove = it.next();
									chunktoremoveX = Integer.valueOf(chunktoremove.split(",")[0]);
									chunktoremoveY = Integer.valueOf(chunktoremove.split(",")[1]);
									getConfig().set("main.claims." + String.valueOf(chunktoremoveX) + "," + String.valueOf(chunktoremoveY), SetAllTarget.toLowerCase());
									newallclaims.add(chunktoremove);
								}
								getConfig().set("main.players." + CommandArgument.toLowerCase() + ".claims", new ArrayList<String>(emptyList));
								getConfig().set("main.players." + SetAllTarget.toLowerCase() + ".claims", newallclaims);
								getConfig().set("main.players." + CommandArgument.toLowerCase() + ".friends", new ArrayList<String>(emptyList));
								getConfig().set("main.players." + CommandArgument.toLowerCase() + ".walktest", false);
								getConfig().set("main.players." + CommandArgument.toLowerCase() + ".gotwarning", false);
								sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_resetsuccessful.replaceAll("##N##", CommandArgument.toLowerCase()));
								sender.sendMessage(ChatColor.GREEN + "[CAC] " + loc_changedall.replaceAll("##N##", SetAllTarget.toLowerCase()));
							}
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "[CAC] " + loc_resetaccessdenied);
					}
				}
				saveConfig();
			}
			
			
	        } else {
	           sender.sendMessage(ChatColor.RED + "[CAC] " + loc_consoleaccessdenied);
	    }
			saveConfig();
	        return true;
	}
    
	@SuppressWarnings({ "unchecked" })
	public void onEnable() {
		ArrayList<String> emptyList = new ArrayList<String>(Arrays.asList(new String[] {}));
		maxChunks = getConfig().getInt("main.config.maxclaims",9);
		autoClaim = getConfig().getInt("main.config.claimblocks",7);
		autoLeave = getConfig().getInt("main.config.unclaimblocks",7);
		maxFriends = getConfig().getInt("main.config.maxfriends",5);
		markBlock = getConfig().getInt("main.config.markblock",35);
		markData = getConfig().getInt("main.config.markdata",6);
		markClaims = getConfig().getBoolean("main.config.markclaims",true);
		belowGround = getConfig().getInt("main.config.belowground",60);
		requireAdjacent = getConfig().getBoolean("main.config.requiretouch",true);
		allowFireSpread = getConfig().getBoolean("main.config.allowfirespread",false);
		allowLava = getConfig().getBoolean("main.config.allowlava",false);
		allowWater = getConfig().getBoolean("main.config.allowwater",false);
		allowTNT = getConfig().getBoolean("main.config.allowtnt",false);
		allowUGFireSpread = getConfig().getBoolean("main.config.allowugfirespread",true);
		allowUGLava = getConfig().getBoolean("main.config.allowuglava",true);
		allowUGWater = getConfig().getBoolean("main.config.allowugwater",true);
		allowUGTNT = getConfig().getBoolean("main.config.allowugtnt",true);
		configVersion = getConfig().getInt("main.config.configversion",0);
		loc_chunkisunprotected = getConfig().getString("main.messages.chunkisprotected","This chunk ##X##,##Y## is UNPROTECTED!");
		loc_canunprotected = getConfig().getString("main.messages.canunprotected","You can build here because the chunk is UNPROTECTED!");
		loc_walkteston = getConfig().getString("main.messages.walkteston","You cannot touch here because you have WALKTEST ON.");
		loc_walktesthelp = getConfig().getString("main.messages.walktesthelp","Turn off by typing /walktest .");
		loc_canop = getConfig().getString("main.messages.canop","You can build here because you are OP!");
		loc_canowner = getConfig().getString("main.messages.canowner","You can build here because you are the OWNER!");
		loc_canfriends = getConfig().getString("main.messages.canfriends","You can build here because you are on the owner's FRIENDSLIST!");
		loc_cannotprotected = getConfig().getString("main.messages.cannotprotected","You cannot build here because this area is protected.");
		loc_alreadyonfriends = getConfig().getString("main.messages.alreadyonfriends","##N## is already on your friendslist!");
		loc_successfullyfriend = getConfig().getString("main.messages.sucessfullyfriend","Successfully added ##N## to your friendslist!");
		loc_removewithdel = getConfig().getString("main.messages.removewithdel","Remove friends with /del <name> .");
		loc_friendsfull = getConfig().getString("main.messages.friendsfull","Your friendslist is full!");
		loc_clearwithdel = getConfig().getString("main.messages.clearwithdel","Or clear the friendslist with /del .");
		loc_friendsusage = getConfig().getString("main.messages.friendsusage","Usage: /add <name> - Trusts <name> to build in your areas!");
		loc_walktestona = getConfig().getString("main.messages.walktestona","Successfully toggled walktest ON!");
		loc_walktestonb = getConfig().getString("main.messages.walktestonb","You CANNOT build in authorized claims now!");
		loc_walktestoffa = getConfig().getString("main.messages.walktestoffa","Successfully toggled walktest OFF!");
		loc_walktestoffb = getConfig().getString("main.messages.walktestoffb","You CAN build in authorized claims now!");
		loc_notonfriends = getConfig().getString("main.messages.notonfriends","##N## is not on your friendslist!");
		loc_removedfriend = getConfig().getString("main.messages.removedfriend","Successfully removed ##N## from your friendslist!");
		loc_clearedfriends = getConfig().getString("main.messages.clearedfriends","Successfully cleared your friendslist!");
		loc_chunkowner = getConfig().getString("main.messages.chunkowner","This chunk ##X##,##Y## belongs to: ##N##.");
		loc_alreadyunprotect = getConfig().getString("main.messages.alreadyunprotect","This chunk ##X##,##Y## is already unprotected!");
		loc_successunprotect = getConfig().getString("main.messages.successunprotect","Successfully unprotected this chunk ##X##,##Y##!");
		loc_successprotect = getConfig().getString("main.messages.successprotect","Successfully protected this chunk ##X##,##Y## under ##N##.");
		loc_changedowner = getConfig().getString("main.messages.changedowner","Changed owner of ##X##,##Y## from ##N## to ##M##.");
		loc_setaccessdenied = getConfig().getString("main.messages.setaccessdenied","/set is a OP only command!");
		loc_resetsuccessful = getConfig().getString("main.messages.resetsuccessful","Successfully reset ##N## player profile!");
		loc_changedall = getConfig().getString("main.messages.changedall","and transferred all claims to ##N##.");
		loc_equalerror = getConfig().getString("main.messages.equalerror","Old and New owner cannot match!");
		loc_resetaccessdenied = getConfig().getString("main.messages.resetaccessdenied","/reset <name> is a OP only command, use /reset to reset yourself!");
		loc_consoleaccessdenied = getConfig().getString("main.messages.consoleaccessdenied","All commands require you to be a player!");
		loc_claimed = getConfig().getString("main.messages.claimed","This chunk ##X##,##Y## now belongs to you.");
		loc_checkprotection = getConfig().getString("main.messages.checkprotection","Check protection with /chk or /walktest .");
		loc_maxclaimsexceed = getConfig().getString("main.messages.maxclaimsexceed","Error: You already own ##I## chunks and cannot claim more!");
		loc_resetinstructions = getConfig().getString("main.messages.resetinstructions","If you messed up, start over with /reset .");
		loc_noprotection = getConfig().getString("main.messages.noprotection","Anything you build here will NOT be protected.");
		loc_notadjacent = getConfig().getString("main.messages.notadjacent","Error: You cannot claim here because its not adjacent to your other chunks!");
		loc_unclaimed = getConfig().getString("main.messages.unclaimed","This chunk ##X##,##Y## is now UNPROTECTED.");
		loc_joinmessage = getConfig().getString("main.messages.joinmessage","Place ##I## successive craftable blocks to protect areas!");
		loc_joinwalktest = getConfig().getString("main.messages.joinwalktest","Walktest was turned OFF for you!");
		loc_wilderness = getConfig().getString("main.messages.wilderness","Server Configuration does not allow this!");
		ignoreBlockList = (List<Integer>) getConfig().getList("main.config.ignoreblocks", Arrays.asList(new Integer[] {2,3,4,12,13,17,24,50,81,87,88,121}));
		if (configVersion < 18) {
			maxChunks = 9;
			autoClaim = 7;
			autoLeave = 7;
			maxFriends = 5;
			markBlock = 35;
			markData = 6;
			markClaims = true;
			requireAdjacent = true;
			allowFireSpread = false;
			allowWater = false;
			allowLava = false;
			allowTNT = false;
			allowUGFireSpread = true;
			allowUGWater = true;
			allowUGLava = true;
			allowUGTNT = true;
			belowGround = 60;
			configVersion = 18;
			ignoreBlockList = Arrays.asList(new Integer[] {2,3,4,12,13,17,24,50,81,87,88,121});
			loc_chunkisunprotected = "This chunk ##X##,##Y## is UNPROTECTED!";
			loc_canunprotected = "You can build here because the chunk is UNPROTECTED!";
			loc_walkteston = "You cannot touch here because you have WALKTEST ON.";
			loc_walktesthelp = "Turn off by typing /walktest .";
			loc_canop = "You can build here because you are OP!";
			loc_canowner = "You can build here because you are the OWNER!";
			loc_canfriends = "You can build here because you are on the owner's FRIENDSLIST!";
			loc_cannotprotected = "You cannot build here because this area is protected.";
			loc_alreadyonfriends = "##N## is already on your friendslist!";
			loc_successfullyfriend = "Successfully added ##N## to your friendslist!";
			loc_removewithdel = "Remove friends with /del <name> .";
			loc_friendsfull = "Your friendslist is full!";
			loc_clearwithdel = "Or clear the friendslist with /del .";
			loc_friendsusage = "Usage: /add <name> - Trusts <name> to build in your areas!";
			loc_walktestona = "Successfully toggled walktest ON!";
			loc_walktestonb = "You CANNOT build in authorized claims now!";
			loc_walktestoffa = "Successfully toggled walktest OFF!";
			loc_walktestoffb = "You CAN build in authorized claims now!";
			loc_notonfriends = "##N## is not on your friendslist!";
			loc_removedfriend = "Successfully removed ##N## from your friendslist!";
			loc_clearedfriends = "Successfully cleared your friendslist!";
			loc_chunkowner = "This chunk ##X##,##Y## belongs to: ##N##.";
			loc_alreadyunprotect = "This chunk ##X##,##Y## is already unprotected!";
			loc_successunprotect = "Successfully unprotected this chunk ##X##,##Y##!";
			loc_successprotect = "Successfully protected this chunk ##X##,##Y## under ##N##.";
			loc_changedowner = "Changed owner of ##X##,##Y## from ##N## to ##M##.";
			loc_setaccessdenied = "/set is a OP only command!";
			loc_resetsuccessful = "Successfully reset ##N## player profile!";
			loc_changedall = "and transferred all claims to ##N##.";
			loc_equalerror = "Old and New owner cannot match!";
			loc_resetaccessdenied = "/reset <name> is a OP only command, use /reset to reset yourself!";
			loc_consoleaccessdenied = "All commands require you to be a player!";
			loc_claimed = "This chunk ##X##,##Y## now belongs to you.";
			loc_checkprotection = "Check protection with /chk or /walktest .";
			loc_maxclaimsexceed = "Error: You already own ##I## chunks and cannot claim more!";
			loc_resetinstructions = "If you messed up, start over with /reset .";
			loc_noprotection = "Anything you build here will NOT be protected.";
			loc_notadjacent = "Error: You cannot claim here because its not adjacent to your other chunks!";
			loc_unclaimed = "This chunk ##X##,##Y## is now UNPROTECTED.";
			loc_joinmessage = "Place ##I## successive craftable blocks to protect areas!";
			loc_joinwalktest = "Walktest was turned OFF for you!";
			loc_wilderness = "Server Configuration does not allow this!";
		}
		getConfig().set("main.config.maxclaims",maxChunks);
		getConfig().set("main.config.claimblocks",autoClaim);
		getConfig().set("main.config.unclaimblocks",autoLeave);
		getConfig().set("main.config.maxfriends",maxFriends);
		getConfig().set("main.config.markblock",markBlock);
		getConfig().set("main.config.markdata",markData);
		getConfig().set("main.config.markclaims",markClaims);
		getConfig().set("main.config.configversion",configVersion);
		getConfig().set("main.config.belowground",belowGround);
		getConfig().set("main.config.requiretouch",requireAdjacent);
		getConfig().set("main.config.allowfirespread",allowFireSpread);
		getConfig().set("main.config.allowlava",allowLava);
		getConfig().set("main.config.allowwater",allowWater);
		getConfig().set("main.config.allowtnt",allowTNT);
		getConfig().set("main.config.allowugfirespread",allowUGFireSpread);
		getConfig().set("main.config.allowuglava",allowUGLava);
		getConfig().set("main.config.allowugwater",allowUGWater);
		getConfig().set("main.config.allowugtnt",allowUGTNT);
		getConfig().set("main.config.ignoreblocks", ignoreBlockList);
		getConfig().set("main.messages.chunkisprotected",loc_chunkisunprotected);
		getConfig().set("main.messages.canunprotected",loc_canunprotected);
		getConfig().set("main.messages.walkteston",loc_walkteston);
		getConfig().set("main.messages.walktesthelp",loc_walktesthelp);
		getConfig().set("main.messages.canop",loc_canop);
		getConfig().set("main.messages.canowner",loc_canowner);
		getConfig().set("main.messages.canfriends",loc_canfriends);
		getConfig().set("main.messages.cannotprotected",loc_cannotprotected);
		getConfig().set("main.messages.alreadyonfriends",loc_alreadyonfriends);
		getConfig().set("main.messages.sucessfullyfriend",loc_successfullyfriend);
		getConfig().set("main.messages.removewithdel",loc_removewithdel);
		getConfig().set("main.messages.friendsfull",loc_friendsfull);
		getConfig().set("main.messages.clearwithdel",loc_clearwithdel);
		getConfig().set("main.messages.friendsusage",loc_friendsusage);
		getConfig().set("main.messages.walktestona",loc_walktestona);
		getConfig().set("main.messages.walktestonb",loc_walktestonb);
		getConfig().set("main.messages.walktestoffa",loc_walktestoffa);
		getConfig().set("main.messages.walktestoffb",loc_walktestoffb);
		getConfig().set("main.messages.notonfriends",loc_notonfriends);
		getConfig().set("main.messages.removedfriend",loc_removedfriend);
		getConfig().set("main.messages.clearedfriends",loc_clearedfriends);
		getConfig().set("main.messages.chunkowner",loc_chunkowner);
		getConfig().set("main.messages.alreadyunprotect",loc_alreadyunprotect);
		getConfig().set("main.messages.successunprotect",loc_successunprotect);
		getConfig().set("main.messages.successprotect",loc_successprotect);
		getConfig().set("main.messages.changedowner",loc_changedowner);
		getConfig().set("main.messages.setaccessdenied",loc_setaccessdenied);
		getConfig().set("main.messages.resetsuccessful",loc_resetsuccessful);
		getConfig().set("main.messages.changedall",loc_changedall);
		getConfig().set("main.messages.equalerror",loc_equalerror);
		getConfig().set("main.messages.resetaccessdenied",loc_resetaccessdenied);
		getConfig().set("main.messages.consoleaccessdenied",loc_consoleaccessdenied);
		getConfig().set("main.messages.claimed",loc_claimed);
		getConfig().set("main.messages.checkprotection",loc_checkprotection);
		getConfig().set("main.messages.maxclaimsexceed",loc_maxclaimsexceed);
		getConfig().set("main.messages.resetinstructions",loc_resetinstructions);
		getConfig().set("main.messages.noprotection",loc_noprotection);
		getConfig().set("main.messages.notadjacent",loc_notadjacent);
		getConfig().set("main.messages.unclaimed",loc_unclaimed);
		getConfig().set("main.messages.joinmessage",loc_joinmessage);
		getConfig().set("main.messages.joinwalktest",loc_joinwalktest);
		getConfig().set("main.messages.wilderness",loc_wilderness);
		saveConfig();
		emptyList.clear();
		getServer().getPluginManager().registerEvents(this,this);
	}
	
			@SuppressWarnings({ "unchecked" })
			@EventHandler
			public void blockplace(BlockPlaceEvent e) {
				ArrayList<String> emptyList = new ArrayList<String>(Arrays.asList(new String[] {}));
				emptyList.clear();
				int chunkX;
				int chunkY;
				String lastChunk;
				int lastChunkX;
				int lastChunkY;
				String playerName;
				String chunkOwner;
				String chunkOwnerN;
				String chunkOwnerE;
				String chunkOwnerW;
				String chunkOwnerS;
				ArrayList<String> friendsList;
				ArrayList<String> allclaims;
				int BlockCount;
				int claimCount;
				boolean canClaimAdjacent;
				boolean walktest;

				boolean gotWarning;
		        if (!e.isCancelled() && e.canBuild() && (e.getBlock().getTypeId() != 0)) {
		        			chunkX = e.getBlock().getChunk().getX();
		        			chunkY = e.getBlock().getChunk().getZ();
		        			playerName = e.getPlayer().getName().toLowerCase();
		        			lastChunk = getConfig().getString("main.players." + playerName + ".lastclaim", "0,0");
		        			lastChunkX = Integer.valueOf(lastChunk.split(",")[0]);
		        			lastChunkY = Integer.valueOf(lastChunk.split(",")[1]);
		        			chunkOwner = getConfig().getString("main.claims." + String.valueOf(chunkX) + "," + String.valueOf(chunkY), null);
		        			if (chunkOwner == null) {
		        				chunkOwner = "";
		        			}
		        			chunkOwner = chunkOwner.toLowerCase();
		        			BlockCount = getConfig().getInt("main.players." + playerName + ".blockcounter",0);
		        			gotWarning = getConfig().getBoolean("main.players." + playerName + ".gotwarning",false);
		        			walktest =  getConfig().getBoolean("main.players." + playerName + ".walktest", false);
		        			allclaims = new ArrayList<String>((List<String>) getConfig().getList("main.players." + playerName + ".claims", emptyList));
		        			claimCount = allclaims.size();
		        			if (chunkOwner.equals("")) {
		        				if (e.getBlock().getTypeId() == 51) {
		        					if (e.getBlock().getLocation().getBlockY() > belowGround) {
		        						if (!allowFireSpread) {
		        							e.setCancelled(true);
		        						}
		        					}
		        					else
		        					{
		        						if (!allowUGFireSpread) {
		        							e.setCancelled(true);
		        						}
		        					}
		        				}
			        			if (claimCount == 0 || !requireAdjacent) {
			        				canClaimAdjacent = true;
			        			}
			        			else
			        			{
			        				chunkOwnerN = getConfig().getString("main.claims." + String.valueOf(chunkX + 1) + "," + String.valueOf(chunkY), null);
				        			if (chunkOwnerN == null) {
				        				chunkOwnerN = "";
				        			}
				        			chunkOwnerN = chunkOwnerN.toLowerCase();
			        				chunkOwnerE = getConfig().getString("main.claims." + String.valueOf(chunkX - 1) + "," + String.valueOf(chunkY), null);
				        			if (chunkOwnerE == null) {
				        				chunkOwnerE = "";
				        			}
				        			chunkOwnerE = chunkOwnerE.toLowerCase();
			        				chunkOwnerW = getConfig().getString("main.claims." + String.valueOf(chunkX) + "," + String.valueOf(chunkY + 1), null);
				        			if (chunkOwnerW == null) {
				        				chunkOwnerW = "";
				        			}
				        			chunkOwnerW = chunkOwnerW.toLowerCase();
			        				chunkOwnerS = getConfig().getString("main.claims." + String.valueOf(chunkX) + "," + String.valueOf(chunkY - 1), null);
				        			if (chunkOwnerS == null) {
				        				chunkOwnerS = "";
				        			}
				        			chunkOwnerS = chunkOwnerS.toLowerCase();
			        				if (chunkOwnerN.equalsIgnoreCase(playerName) || chunkOwnerE.equalsIgnoreCase(playerName) || chunkOwnerW.equalsIgnoreCase(playerName) || chunkOwnerS.equalsIgnoreCase(playerName)) {
			        					canClaimAdjacent = true;
			        				}
			        				else
			        				{
			        					canClaimAdjacent = false;
			        				}
			        				
			        			}
		        				
		        				if (canClaimAdjacent || e.getPlayer().isOp() || e.getPlayer().hasPermission("chunkautoclaimer.unlimited")) {
		        					if (claimCount < maxChunks || e.getPlayer().isOp() || e.getPlayer().hasPermission("chunkautoclaimer.unlimited")) {
		        						if (!ignoreBlockList.contains(e.getBlockPlaced().getTypeId()) && (e.getBlock().getTypeId() != 51)) {
		        							if ((lastChunkX == chunkX) && (lastChunkY == chunkY)) {
		        								BlockCount = BlockCount + 1;
		        								if (BlockCount >= autoClaim) {
		        									BlockCount = 0;
		        									chunkOwner = playerName.toLowerCase();
		        				        			allclaims.add(chunkX + "," + chunkY);
		        				        			e.getPlayer().sendMessage(ChatColor.GREEN + "[CAC] " + loc_claimed.replaceAll("##X##",String.valueOf(chunkX)).replaceAll("##Y##", String.valueOf(chunkY)));
		        				        			e.getPlayer().sendMessage(ChatColor.GREEN + "[CAC] " + loc_checkprotection);
		        				        			markClaim(e.getPlayer());
		        								}
		        							}
		        							else
		        							{
		        								BlockCount = 1;
		        								lastChunkX = chunkX;
		        								lastChunkY = chunkY;
		        								gotWarning = false;
		        							}
		        						}
		        						else
		        						{
		        							BlockCount = 0;
    										lastChunkX = chunkX;
    										lastChunkY = chunkY;
		        						}
		        						gotWarning = false;
		        					}
		        					else
		        					{
		        						if (!gotWarning) {
		        							e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_maxclaimsexceed.replaceAll("##I##", String.valueOf(claimCount)));
		        							e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_resetinstructions);
		        							e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_noprotection);
		        							e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_checkprotection);
		        							gotWarning = true;
		        		        			markClaim(e.getPlayer());
		        						}
		        					}
		        				}
		        				else
		        				{
	        						if (!gotWarning) {
	        							e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_notadjacent);
	        							e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_resetinstructions);
	        							e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_noprotection);
	        							e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_checkprotection);
	        							gotWarning = true;
	        		        			markClaim(e.getPlayer());
	        						}
		        				}
		        			}
		        			else
		        			{
		        				if (walktest) {
    								e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_walkteston);
    								e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_chunkowner.replaceAll("##X##", String.valueOf(chunkX)).replaceAll("##Y##", String.valueOf(chunkY)).replaceAll("##N##", chunkOwner.toLowerCase()));
    								e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_walktesthelp);
    								e.setCancelled(true);
    			        			markClaim(e.getPlayer());
		        				}
		        				else
		        				{
		        					if (!playerName.equalsIgnoreCase(chunkOwner)) {
		        						friendsList = new ArrayList<String>((List<String>) getConfig().getList("main.players." + chunkOwner.toLowerCase() + ".friends", emptyList));
		        						if (!friendsList.contains(playerName.toLowerCase())) {
		        							if (!e.getPlayer().isOp() && !e.getPlayer().hasPermission("chunkautoclaimer.op")) {
		        								e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_chunkowner.replaceAll("##X##", String.valueOf(chunkX)).replaceAll("##Y##", String.valueOf(chunkY)).replaceAll("##N##", chunkOwner.toLowerCase()));
		        								e.setCancelled(true);
		        			        			markClaim(e.getPlayer());
		        							}
		        						}
		        					}
		        				}
		        				gotWarning = false;
		        				BlockCount = 0;
		        			}
		        			getConfig().set("main.players." + playerName + ".blockcounter", BlockCount);
		        			getConfig().set("main.players." + playerName + ".lastclaim", lastChunkX + "," + lastChunkY);
		        			getConfig().set("main.players." + playerName + ".gotwarning", gotWarning);
		        			getConfig().set("main.players." + playerName + ".claims", allclaims);
		        			if (chunkOwner.equals("")) {
		        				getConfig().set("main.claims." + String.valueOf(chunkX) + "," + String.valueOf(chunkY), null);	        				
		        			}
		        			else
		        			{
		        				getConfig().set("main.claims." + String.valueOf(chunkX) + "," + String.valueOf(chunkY), chunkOwner);
		        			}
		        			saveConfig();
		        }
			}
			
			@EventHandler
		    public void onExplosion (EntityExplodeEvent event) {
		    	ArrayList<String> emptyList = new ArrayList<String>(Arrays.asList(new String[] {}));
				emptyList.clear();
				String chunkOwner;
		        if (!event.isCancelled()) {
		            List<Block> blocks = event.blockList();
		            if (blocks != null) {
		                Collection<Block> saveBanks = new HashSet<Block>();
		                for (Iterator<Block> it = blocks.iterator(); it.hasNext();) {
		                    Block block = it.next();
		                    chunkOwner = getConfig().getString("main.claims." + String.valueOf(block.getChunk().getX()) + "," + String.valueOf(block.getChunk().getZ()), null);
		        			if (chunkOwner == null) {
		        				chunkOwner = "";
		        			}
		        			chunkOwner = chunkOwner.toLowerCase();
		                    if (!(chunkOwner.equals(""))) {
		                        saveBanks.add(block);
		                    }
		                    else
		                    {
		                    	if (block.getLocation().getBlockY() > belowGround) {
		                    		if (!allowTNT) {
		                    			saveBanks.add(block);
		                    		}
		                    	}
		                    	else
		                    	{
		                    		if (!allowUGTNT) {
		                    			saveBanks.add(block);
		                    		}
		                    	}
		                    }
		                }
		                if (!saveBanks.isEmpty()) {
		                    event.blockList().removeAll(saveBanks);
		                }
		            }
		        }
		    }
		    
			@EventHandler
		    public void blockburnevent (BlockBurnEvent e) {
		    	ArrayList<String> emptyList = new ArrayList<String>(Arrays.asList(new String[] {}));
				emptyList.clear();
				String chunkOwner;
				chunkOwner = getConfig().getString("main.claims." + String.valueOf(e.getBlock().getChunk().getX()) + "," + String.valueOf(e.getBlock().getChunk().getZ()), null);
    			if (chunkOwner == null) {
    				chunkOwner = "";
    			}
    			chunkOwner = chunkOwner.toLowerCase();
				
		        if (!e.isCancelled()) {
		            if (!(chunkOwner.equals(""))) {
		            	e.setCancelled(true);
		            }
		            else
		            {
    					if (e.getBlock().getLocation().getBlockY() > belowGround) {
    						if (!allowFireSpread) {
    							e.setCancelled(true);
    						}
    					}
    					else
    					{
    						if (!allowUGFireSpread) {
    							e.setCancelled(true);
    						}
    					}
		            }
		        }
		    }
		    
		    @SuppressWarnings({ "unchecked" })
			@EventHandler
		    public void ignitefire (BlockIgniteEvent e) {
		    	boolean walktest;
		    	ArrayList<String> emptyList = new ArrayList<String>(Arrays.asList(new String[] {}));
				emptyList.clear();
				String chunkOwner;
				ArrayList<String> friendsList;
				chunkOwner = getConfig().getString("main.claims." + String.valueOf(e.getBlock().getChunk().getX()) + "," + String.valueOf(e.getBlock().getChunk().getZ()), null);
    			if (chunkOwner == null) {
    				chunkOwner = "";
    			}
    			chunkOwner = chunkOwner.toLowerCase();
				
		        if (!e.isCancelled()) {
		            if (!(chunkOwner.equals(""))) {
		                if (e.getCause() == IgniteCause.FLINT_AND_STEEL) {
		                	walktest =  getConfig().getBoolean("main.players." + e.getPlayer().getName().toLowerCase() + ".walktest", false);
		                	if (walktest) {
								e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_walkteston);
								e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_chunkowner.replaceAll("##X##", String.valueOf(e.getBlock().getChunk().getX())).replaceAll("##Y##", String.valueOf(e.getBlock().getChunk().getZ())).replaceAll("##N##", chunkOwner.toLowerCase()));
								e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_walktesthelp);
								e.setCancelled(true); 	 
			        			markClaim(e.getPlayer());
		                	}
		                	else
		                	{
		                		if (!chunkOwner.equalsIgnoreCase(e.getPlayer().getName().toLowerCase())) {
		                			friendsList = new ArrayList<String>((List<String>) getConfig().getList("main.players." + chunkOwner.toLowerCase() + ".friends", emptyList));
		                			if (!friendsList.contains(e.getPlayer().getName().toLowerCase())) {
		                				if (!e.getPlayer().isOp() && !e.getPlayer().hasPermission("chunkautoclaimer.op")) {
		                					e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_chunkowner.replaceAll("##X##", String.valueOf(e.getBlock().getChunk().getX())).replaceAll("##Y##", String.valueOf(e.getBlock().getChunk().getZ())).replaceAll("##N##", chunkOwner.toLowerCase()));
		                					e.setCancelled(true);
		        		        			markClaim(e.getPlayer());
		                				}
		                			}
		                		}
		                	}
		                    
		                } else {
		                    e.setCancelled(true);
		                }
		            }
		            else
		            {
    					if (e.getBlock().getLocation().getBlockY() > belowGround) {
    						if (!allowFireSpread) {
    							e.setCancelled(true);
    						}
    					}
    					else
    					{
    						if (!allowUGFireSpread) {
    							e.setCancelled(true);
    						}
    					}
		            }
		        }
		    }
	
		    @SuppressWarnings("unchecked")
			@EventHandler
		    public void paintbevent (PaintingBreakEvent e) {
		    	ArrayList<String> emptyList = new ArrayList<String>(Arrays.asList(new String[] {}));
				emptyList.clear();
				String chunkOwner;
				String playerName;
				boolean walktest;
				ArrayList<String> friendsList;
				
		        if (!e.isCancelled()) {
	        		chunkOwner = getConfig().getString("main.claims." + String.valueOf(e.getPainting().getLocation().getChunk().getX()) + "," + String.valueOf(e.getPainting().getLocation().getChunk().getZ()), null);
        			if (chunkOwner == null) {
        				chunkOwner = "";
        			}
        			chunkOwner = chunkOwner.toLowerCase();
		        	if (!(chunkOwner.equals(""))) {
		        		if (e instanceof PaintingBreakByEntityEvent){
		        			PaintingBreakByEntityEvent pbbeEvent = (PaintingBreakByEntityEvent) e;
		        			if (pbbeEvent.getRemover() instanceof Player) {
		        				playerName = ((Player) pbbeEvent.getRemover()).getName().toLowerCase();
		        				walktest =  getConfig().getBoolean("main.players." + playerName + ".walktest", false);
			                	if (walktest) {
			                		((Player) pbbeEvent.getRemover()).sendMessage(ChatColor.RED + "[CAC] " + loc_walkteston);
			                		((Player) pbbeEvent.getRemover()).sendMessage(ChatColor.RED + "[CAC] " + loc_chunkowner.replaceAll("##X##", String.valueOf(e.getPainting().getLocation().getChunk().getX())).replaceAll("##Y##", String.valueOf(e.getPainting().getLocation().getChunk().getZ())).replaceAll("##N##", chunkOwner.toLowerCase()));
			                		((Player) pbbeEvent.getRemover()).sendMessage(ChatColor.RED + "[CAC] " + loc_walktesthelp);
									e.setCancelled(true); 	  
				        			markClaim(((Player) pbbeEvent.getRemover()));
			                	}
			                	else
			                	{
			                		if (!chunkOwner.equalsIgnoreCase(playerName)) {
			                			friendsList = new ArrayList<String>((List<String>) getConfig().getList("main.players." + chunkOwner.toLowerCase() + ".friends", emptyList));
			                			if (!friendsList.contains(playerName)) {
			                				if (!((Player) pbbeEvent.getRemover()).isOp() && !((Player) pbbeEvent.getRemover()).hasPermission("chunkautoclaimer.op")) {
			                					((Player) pbbeEvent.getRemover()).sendMessage(ChatColor.RED + "[CAC] " + loc_chunkowner.replaceAll("##X##", String.valueOf(e.getPainting().getLocation().getChunk().getX())).replaceAll("##Y##", String.valueOf(e.getPainting().getLocation().getChunk().getZ())).replaceAll("##N##", chunkOwner.toLowerCase()));
			                					e.setCancelled(true);
			        		        			markClaim(((Player) pbbeEvent.getRemover()));
			                				}
			                			}
			                		}
			                	}
		        				
		        			}
			        		else
			        		{
			        			e.setCancelled(true);
			        		}
		        		}
		        		else
		        		{
		        			e.setCancelled(true);
		        		}
		        	}
		        }
		    }
		    	
		    
		    @SuppressWarnings("unchecked")
			@EventHandler
		    public void paintpevent (PaintingPlaceEvent e) {
		    	ArrayList<String> emptyList = new ArrayList<String>(Arrays.asList(new String[] {}));
				emptyList.clear();
				String chunkOwner;
				String playerName;
				boolean walktest;
				ArrayList<String> friendsList;
		        if (!e.isCancelled()) {
	        		chunkOwner = getConfig().getString("main.claims." + String.valueOf(e.getPainting().getLocation().getChunk().getX()) + "," + String.valueOf(e.getPainting().getLocation().getChunk().getZ()), null);
        			if (chunkOwner == null) {
        				chunkOwner = "";
        			}
        			chunkOwner = chunkOwner.toLowerCase();
		        	if (!(chunkOwner.equals(""))) {
		        		playerName = e.getPlayer().getName().toLowerCase();
		        		walktest =  getConfig().getBoolean("main.players." + playerName + ".walktest", false);
			            if (walktest) {
			                e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_walkteston);
			                e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_chunkowner.replaceAll("##X##", String.valueOf(e.getPainting().getLocation().getChunk().getX())).replaceAll("##Y##", String.valueOf(e.getPainting().getLocation().getChunk().getZ())).replaceAll("##N##", chunkOwner.toLowerCase()));
			                e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_walktesthelp);
							e.setCancelled(true);
		        			markClaim(e.getPlayer());
			            }
			            else
			            {
			            	if (!chunkOwner.equalsIgnoreCase(playerName)) {
			            		friendsList = new ArrayList<String>((List<String>) getConfig().getList("main.players." + chunkOwner.toLowerCase() + ".friends", emptyList));
			                	if (!friendsList.contains(playerName)) {
			                		if (!e.getPlayer().isOp() && !e.getPlayer().hasPermission("chunkautoclaimer.op")) {
			                			e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_chunkowner.replaceAll("##X##", String.valueOf(e.getPainting().getLocation().getChunk().getX())).replaceAll("##Y##", String.valueOf(e.getPainting().getLocation().getChunk().getZ())).replaceAll("##N##", chunkOwner.toLowerCase()));
			                			e.setCancelled(true);
	        		        			markClaim(e.getPlayer());
			                		}
			                	}
			            	}
			            }
		        	}
		        }
		    }
  	
		    
		    @EventHandler
		    public void pistonevent (BlockPistonExtendEvent e) {
	            Block targetBlock;
	            Block targetStickyBlock;
	            Integer blocki;
		    	ArrayList<String> emptyList = new ArrayList<String>(Arrays.asList(new String[] {}));
				emptyList.clear();
				String chunkOwnerA;
				String chunkOwnerB;
				String chunkOwnerC;
		        if (!e.isCancelled()) {
		        	if (!e.getDirection().equals(BlockFace.DOWN) && !e.getDirection().equals(BlockFace.UP) && !e.getDirection().equals(BlockFace.SELF)) {
		        		targetBlock = e.getBlock().getRelative(e.getDirection());
		        		for (blocki = 0; blocki < 12; blocki++) {
		        			if (!(targetBlock.getTypeId() == 0)) {
		        				targetBlock = targetBlock.getRelative(e.getDirection());
		        			}
		        			else
		        			{
		        				break;
		        			}
		        		}
		        		chunkOwnerA = getConfig().getString("main.claims." + String.valueOf(e.getBlock().getChunk().getX()) + "," + String.valueOf(e.getBlock().getChunk().getZ()), null);
						chunkOwnerB = getConfig().getString("main.claims." + String.valueOf(targetBlock.getChunk().getX()) + "," + String.valueOf(targetBlock.getChunk().getZ()), null);
	        			if (chunkOwnerA == null) {
	        				chunkOwnerA = "";
	        			}

	        			if (chunkOwnerB == null) {
	        				chunkOwnerB = "";
	        			}
	        			chunkOwnerA = chunkOwnerA.toLowerCase();
	        			chunkOwnerB = chunkOwnerB.toLowerCase();
		            	if (!chunkOwnerA.equals(chunkOwnerB) && !(chunkOwnerB.equals(""))) {
		                	e.setCancelled(true);
		            	}
		            	else
		            	{
		            		if (e.isSticky()) {
				        		targetStickyBlock = e.getBlock().getRelative(e.getDirection());
			        			if (targetStickyBlock.getTypeId() == 0) {
			        				targetStickyBlock = targetStickyBlock.getRelative(e.getDirection());
				        			if (!(targetStickyBlock.getTypeId() == 0)) {
				        				chunkOwnerA = getConfig().getString("main.claims." + String.valueOf(e.getBlock().getChunk().getX()) + "," + String.valueOf(e.getBlock().getChunk().getZ()), null);
				        				chunkOwnerC = getConfig().getString("main.claims." + String.valueOf(targetStickyBlock.getChunk().getX()) + "," + String.valueOf(targetStickyBlock.getChunk().getZ()), null);
					        			if (chunkOwnerA == null) {
					        				chunkOwnerA = "";
					        			}

					        			if (chunkOwnerC == null) {
					        				chunkOwnerC = "";
					        			}
					        			chunkOwnerA = chunkOwnerA.toLowerCase();
					        			chunkOwnerC = chunkOwnerC.toLowerCase();
						            	if (!chunkOwnerA.equals(chunkOwnerC) && !(chunkOwnerC.equals(""))) {
						                	e.setCancelled(true);
						            	}
				        			}
			        			}
		            		}
		            	}
		        	}
		        }
		    }
		    
		    @EventHandler
		    public void fluidevent (BlockFromToEvent e) {
		    	ArrayList<String> emptyList = new ArrayList<String>(Arrays.asList(new String[] {}));
				emptyList.clear();
				String chunkOwnerA;
				String chunkOwnerB;
				boolean allowFlow;
		        if (!e.isCancelled()) {
					chunkOwnerA = getConfig().getString("main.claims." + String.valueOf(e.getBlock().getChunk().getX()) + "," + String.valueOf(e.getBlock().getChunk().getZ()), null);
					chunkOwnerB = getConfig().getString("main.claims." + String.valueOf(e.getToBlock().getChunk().getX()) + "," + String.valueOf(e.getToBlock().getChunk().getZ()), null);
        			if (chunkOwnerA == null) {
        				chunkOwnerA = "";
        			}

        			if (chunkOwnerB == null) {
        				chunkOwnerB = "";
        			}
        			chunkOwnerA = chunkOwnerA.toLowerCase();
        			chunkOwnerB = chunkOwnerB.toLowerCase();
        			allowFlow = false;
        			if (chunkOwnerA.length() > 0) {
        				if (chunkOwnerB.length() > 0) {
        					if (chunkOwnerA.equals(chunkOwnerB)) {
        						allowFlow = true;
        					}
        				}
        			}
        			if (chunkOwnerB.equals("") && ((e.getBlock().getType().getId() == 10)||(e.getBlock().getType().getId() == 11)) && (e.getToBlock().getLocation().getBlockY() > belowGround) && allowLava) {
        				allowFlow = true;
        			}
        			if (chunkOwnerB.equals("") && ((e.getBlock().getType().getId() == 10)||(e.getBlock().getType().getId() == 11)) && (e.getToBlock().getLocation().getBlockY() <= belowGround) && allowUGLava) {
        				allowFlow = true;
        			}
        			if (chunkOwnerB.equals("") && ((e.getBlock().getType().getId() == 8)||(e.getBlock().getType().getId() == 9)) && (e.getToBlock().getLocation().getBlockY() > belowGround) && allowWater) {
        				allowFlow = true;
        			}
        			if (chunkOwnerB.equals("") && ((e.getBlock().getType().getId() == 8)||(e.getBlock().getType().getId() == 9)) && (e.getToBlock().getLocation().getBlockY() <= belowGround) && allowUGWater) {
        				allowFlow = true;
        			}
		            if (!allowFlow) {
		                e.setCancelled(true);
		            }
		        }
		    }
		    
		    @SuppressWarnings({ "unchecked", "deprecation" })
			@EventHandler
		    public void onPlayerEmptyBucket (PlayerBucketEmptyEvent event) {
		    	ArrayList<String> emptyList = new ArrayList<String>(Arrays.asList(new String[] {}));
				emptyList.clear();
	            Block targetBlock;
	            boolean walktest;
				String chunkOwner;
				ArrayList<String> friendsList;
		        if (!event.isCancelled()) {
		            Block block = event.getBlockClicked();
		            BlockFace face = event.getBlockFace();
		            if (face.equals(BlockFace.UP) || face.equals(BlockFace.DOWN)|| face.equals(BlockFace.SELF)) {
		                targetBlock = block;
		            } else {
		                targetBlock = block.getRelative(face);
		            }
		            chunkOwner = getConfig().getString("main.claims." + String.valueOf(targetBlock.getChunk().getX()) + "," + String.valueOf(targetBlock.getChunk().getZ()), null);
        			if (chunkOwner == null) {
        				chunkOwner = "";
        			}
        			chunkOwner = chunkOwner.toLowerCase();
		            if (!(chunkOwner.equals(""))) {
	                	walktest =  getConfig().getBoolean("main.players." + event.getPlayer().getName().toLowerCase() + ".walktest", false);
	                	if (walktest) {
							event.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_walkteston);
							event.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_chunkowner.replaceAll("##X##", String.valueOf(targetBlock.getChunk().getX())).replaceAll("##Y##", String.valueOf(targetBlock.getChunk().getZ())).replaceAll("##N##", chunkOwner.toLowerCase()));
							event.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_walktesthelp);
							event.setCancelled(true);
							event.getPlayer().updateInventory();
		        			markClaim(event.getPlayer());
	                	}
	                	else
	                	{
	                		if (!chunkOwner.equalsIgnoreCase(event.getPlayer().getName().toLowerCase())) {
	                			friendsList = new ArrayList<String>((List<String>) getConfig().getList("main.players." + chunkOwner.toLowerCase() + ".friends", emptyList));
	                			if (!friendsList.contains(event.getPlayer().getName().toLowerCase())) {
	                				if (!event.getPlayer().isOp() && !event.getPlayer().hasPermission("chunkautoclaimer.op")) {
	                					event.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_chunkowner.replaceAll("##X##", String.valueOf(targetBlock.getChunk().getX())).replaceAll("##Y##", String.valueOf(targetBlock.getChunk().getZ())).replaceAll("##N##", chunkOwner.toLowerCase()));
	                					event.setCancelled(true);
	                					event.getPlayer().updateInventory();
	        		        			markClaim(event.getPlayer());
	                				}
	                			}
	                		}
	                	}
		            }
		            else
		            {
    					if (targetBlock.getLocation().getBlockY() > belowGround) {
    						if (event.getBucket() == Material.LAVA_BUCKET) {
        						if (!allowLava && !event.getPlayer().isOp() && !event.getPlayer().hasPermission("chunkautoclaimer.op")) {
        							event.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_wilderness);
        							event.setCancelled(true);
        							event.getPlayer().updateInventory();
        						}
    						}
    						if (event.getBucket() == Material.WATER_BUCKET) {
        						if (!allowWater && !event.getPlayer().isOp() && !event.getPlayer().hasPermission("chunkautoclaimer.op")) {
        							event.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_wilderness);
        							event.setCancelled(true);
        							event.getPlayer().updateInventory();
        						}
    						}
    					}
    					else
    					{
    						if (event.getBucket() == Material.LAVA_BUCKET) {
        						if (!allowUGLava && !event.getPlayer().isOp() && !event.getPlayer().hasPermission("chunkautoclaimer.op")) {
        							event.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_wilderness);
        							event.setCancelled(true);
        							event.getPlayer().updateInventory();
        						}
    						}
    						if (event.getBucket() == Material.WATER_BUCKET) {
        						if (!allowUGWater && !event.getPlayer().isOp() && !event.getPlayer().hasPermission("chunkautoclaimer.op")) {
        							event.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_wilderness);
        							event.setCancelled(true);
        							event.getPlayer().updateInventory();
        						}
    						}
    					}
		            }
		        }
		    }
		    
		    @SuppressWarnings({ "unchecked", "deprecation" })
			@EventHandler
		    public void onPlayerFillBucket (PlayerBucketFillEvent event) {
		    	ArrayList<String> emptyList = new ArrayList<String>(Arrays.asList(new String[] {}));
				emptyList.clear();
	            Block targetBlock;
	            boolean walktest;
				String chunkOwner;
				ArrayList<String> friendsList;
		        if (!event.isCancelled()) {
		            Block block = event.getBlockClicked();
		            BlockFace face = event.getBlockFace();
		            if (face.equals(BlockFace.UP) || face.equals(BlockFace.DOWN)|| face.equals(BlockFace.SELF)) {
		                targetBlock = block;
		            } else {
		                targetBlock = block.getRelative(face);
		            }
		            chunkOwner = getConfig().getString("main.claims." + String.valueOf(targetBlock.getChunk().getX()) + "," + String.valueOf(targetBlock.getChunk().getZ()), null);
        			if (chunkOwner == null) {
        				chunkOwner = "";
        			}
        			chunkOwner = chunkOwner.toLowerCase();
		            if (!(chunkOwner.equals(""))) {
	                	walktest =  getConfig().getBoolean("main.players." + event.getPlayer().getName().toLowerCase() + ".walktest", false);
	                	if (walktest) {
							event.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_walkteston);
							event.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_chunkowner.replaceAll("##X##", String.valueOf(targetBlock.getChunk().getX())).replaceAll("##Y##", String.valueOf(targetBlock.getChunk().getZ())).replaceAll("##N##", chunkOwner.toLowerCase()));
							event.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_walktesthelp);
							event.setCancelled(true); 
							event.getPlayer().updateInventory();
		        			markClaim(event.getPlayer());
	                	}
	                	else
	                	{
	                		if (!chunkOwner.equalsIgnoreCase(event.getPlayer().getName().toLowerCase())) {
	                			friendsList = new ArrayList<String>((List<String>) getConfig().getList("main.players." + chunkOwner.toLowerCase() + ".friends", emptyList));
	                			if (!friendsList.contains(event.getPlayer().getName().toLowerCase())) {
	                				if (!event.getPlayer().isOp() && !event.getPlayer().hasPermission("chunkautoclaimer.op")) {
	                					event.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_chunkowner.replaceAll("##X##", String.valueOf(targetBlock.getChunk().getX())).replaceAll("##Y##", String.valueOf(targetBlock.getChunk().getZ())).replaceAll("##N##", chunkOwner.toLowerCase()));
	                					event.setCancelled(true);
	                					event.getPlayer().updateInventory();
	        		        			markClaim(event.getPlayer());
	                				}
	                			}
	                		}
	                	}
		            }
		        }
		    }
		    
			@SuppressWarnings({ "unchecked" })
			@EventHandler
			public void blockdestroy(BlockBreakEvent e) {
				ArrayList<String> emptyList = new ArrayList<String>(Arrays.asList(new String[] {}));
				emptyList.clear();
				int chunkX;
				int chunkY;
				String lastChunk;
				int lastChunkX;
				int lastChunkY;
				String playerName;
				String chunkOwner;
				ArrayList<String> friendsList;
				ArrayList<String> allclaims;
				int BlockCount;
				boolean gotWarning;
				boolean walktest;
				
		        if (!e.isCancelled() && e.getBlock().getTypeId() != 51) {
		        			chunkX = e.getBlock().getChunk().getX();
		        			chunkY = e.getBlock().getChunk().getZ();
		        			playerName = e.getPlayer().getName().toLowerCase();
		        			lastChunk = getConfig().getString("main.players." + playerName + ".lastclaim", "0,0");
		        			lastChunkX = Integer.valueOf(lastChunk.split(",")[0]);
		        			lastChunkY = Integer.valueOf(lastChunk.split(",")[1]);
		        			chunkOwner = getConfig().getString("main.claims." + String.valueOf(chunkX) + "," + String.valueOf(chunkY), null);
		        			if (chunkOwner == null) {
		        				chunkOwner = "";
		        			}
		        			chunkOwner = chunkOwner.toLowerCase();
		        			BlockCount = getConfig().getInt("main.players." + playerName + ".blockcounter",0);
		        			gotWarning = getConfig().getBoolean("main.players." + playerName + ".gotwarning",false);
		        			walktest =  getConfig().getBoolean("main.players." + playerName + ".walktest", false);
		        			allclaims = new ArrayList<String>((List<String>) getConfig().getList("main.players." + playerName + ".claims",emptyList));
		        			if (chunkOwner.equals("")) {
		        				BlockCount = 0;
		        			}
		        			else
		        			{
		        				if (walktest) {
    								e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_walkteston);
    								e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_chunkowner.replaceAll("##X##", String.valueOf(chunkX)).replaceAll("##Y##", String.valueOf(chunkY)).replaceAll("##N##", chunkOwner.toLowerCase()));
    								e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_walktesthelp);
    								e.setCancelled(true);
        		        			markClaim(e.getPlayer());
		        				}
		        				else
		        				{
		        					if (playerName.equalsIgnoreCase(chunkOwner)) {
		        						if (!ignoreBlockList.contains(e.getBlock().getTypeId())) {
        									if ((lastChunkX == chunkX) && (lastChunkY == chunkY)) {
        										BlockCount = BlockCount + 1;
        										if (BlockCount >= autoLeave) {
        											BlockCount = 0;
        											chunkOwner = "";
        				        					allclaims.remove(chunkX + "," + chunkY);
        				        					e.getPlayer().sendMessage(ChatColor.AQUA + "[CAC] " + loc_unclaimed.replaceAll("##X##",String.valueOf(chunkX)).replaceAll("##Y##", String.valueOf(chunkY)));
        				        					e.getPlayer().sendMessage(ChatColor.AQUA + "[CAC] " + loc_checkprotection);
        		        		        			markClaim(e.getPlayer());
        										}
        									}
        									else
        									{
        										BlockCount = 1;
        										lastChunkX = chunkX;
        										lastChunkY = chunkY;
        										gotWarning = false;
        									}
        								}
	        							else
	        							{
	        								BlockCount = 0;
    										lastChunkX = chunkX;
    										lastChunkY = chunkY;
	        							}
        								gotWarning = false;
		        					}
		        					else
		        					{
				        				friendsList = new ArrayList<String>((List<String>) getConfig().getList("main.players." + chunkOwner.toLowerCase() + ".friends", emptyList));
		        						if (!friendsList.contains(playerName.toLowerCase())) {
		        							if (!e.getPlayer().isOp() && !e.getPlayer().hasPermission("chunkautoclaimer.op")) {
		        								e.getPlayer().sendMessage(ChatColor.RED + "[CAC] " + loc_chunkowner.replaceAll("##X##", String.valueOf(chunkX)).replaceAll("##Y##", String.valueOf(chunkY)).replaceAll("##N##", chunkOwner.toLowerCase()));
		        								e.setCancelled(true);
			        		        			markClaim(e.getPlayer());
		        							}
		        						}
		        					}
		        				}
		        			}
		        			getConfig().set("main.players." + playerName + ".blockcounter", BlockCount);
		        			getConfig().set("main.players." + playerName + ".lastclaim", lastChunkX + "," + lastChunkY);
		        			getConfig().set("main.players." + playerName + ".gotwarning", gotWarning);
		        			getConfig().set("main.players." + playerName + ".claims", allclaims);
		        			if (chunkOwner.equals("")) {
		        				getConfig().set("main.claims." + String.valueOf(chunkX) + "," + String.valueOf(chunkY), null);
		        			}
		        			else
		        			{
		        				getConfig().set("main.claims." + String.valueOf(chunkX) + "," + String.valueOf(chunkY), chunkOwner);
		        			}
		        			saveConfig();
		        }
			}
			@EventHandler
			public void pjoin(PlayerJoinEvent e) {
				ArrayList<String> emptyList = new ArrayList<String>(Arrays.asList(new String[] {}));
				emptyList.clear();
				boolean walktest;
				walktest =  getConfig().getBoolean("main.players." + e.getPlayer().getName().toLowerCase() + ".walktest", false);
    			e.getPlayer().sendMessage(ChatColor.YELLOW + "[CAC] " + loc_joinmessage.replaceAll("##I##", String.valueOf(autoClaim)));
				if (walktest) {
					getConfig().set("main.players." + e.getPlayer().getName().toLowerCase() + ".walktest", false);
					saveConfig();
					e.getPlayer().sendMessage(ChatColor.AQUA + "[CAC] " + loc_joinwalktest);
				}
			}

	public void onDisable() {
		ArrayList<String> emptyList = new ArrayList<String>(Arrays.asList(new String[] {}));
		emptyList.clear();
		saveConfig();
	}
	
	public void markClaim (Player p) {
		String lastMark;
		int lastMarkX;
		int lastMarkY;
		Location corner;
		if (markClaims) {
			lastMark = getConfig().getString("main.players." + p.getName() + ".lastmark", "0,0");
			lastMarkX = Integer.valueOf(lastMark.split(",")[0]);
			lastMarkY = Integer.valueOf(lastMark.split(",")[1]);
			if (!((lastMarkX == p.getLocation().getChunk().getX()) && (lastMarkY == p.getLocation().getChunk().getZ()))) {


				if (p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 0).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 0).getX(),p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 0).getLocation()) - 1,p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 0).getZ()).getLocation();
					p.sendBlockChange(corner,corner.getBlock().getTypeId(),corner.getBlock().getData());
				}
				if (p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(1, 0, 0).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(1, 0, 0).getX(),p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(1, 0, 0).getLocation()) - 1,p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(1, 0, 0).getZ()).getLocation();
					p.sendBlockChange(corner,corner.getBlock().getTypeId(),corner.getBlock().getData());
				}
				if (p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 1).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 1).getX(),p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 1).getLocation()) - 1,p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 1).getZ()).getLocation();
					p.sendBlockChange(corner,corner.getBlock().getTypeId(),corner.getBlock().getData());
				}
				
				
				if (p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 0).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 0).getX(),p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 0).getLocation()) - 1,p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 0).getZ()).getLocation();
					p.sendBlockChange(corner,corner.getBlock().getTypeId(),corner.getBlock().getData());
				}
				if (p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(14, 0, 0).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(14, 0, 0).getX(),p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(14, 0, 0).getLocation()) - 1,p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(14, 0, 0).getZ()).getLocation();
					p.sendBlockChange(corner,corner.getBlock().getTypeId(),corner.getBlock().getData());
				}
				if (p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 1).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 1).getX(),p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 1).getLocation()) - 1,p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 1).getZ()).getLocation();
					p.sendBlockChange(corner,corner.getBlock().getTypeId(),corner.getBlock().getData());
				}
				
				if (p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 15).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 15).getX(),p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 15).getLocation()) - 1,p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 15).getZ()).getLocation();
					p.sendBlockChange(corner,corner.getBlock().getTypeId(),corner.getBlock().getData());
				}
				if (p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(1, 0, 15).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(1, 0, 15).getX(),p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(1, 0, 15).getLocation()) - 1,p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(1, 0, 15).getZ()).getLocation();
					p.sendBlockChange(corner,corner.getBlock().getTypeId(),corner.getBlock().getData());
				}
				if (p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 14).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 14).getX(),p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 14).getLocation()) - 1,p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(0, 0, 14).getZ()).getLocation();
					p.sendBlockChange(corner,corner.getBlock().getTypeId(),corner.getBlock().getData());
				}
				
				if (p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 15).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 15).getX(),p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 15).getLocation()) - 1,p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 15).getZ()).getLocation();
					p.sendBlockChange(corner,corner.getBlock().getTypeId(),corner.getBlock().getData());
				}
				if (p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(14, 0, 15).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(14, 0, 15).getX(),p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(14, 0, 15).getLocation()) - 1,p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(14, 0, 15).getZ()).getLocation();
					p.sendBlockChange(corner,corner.getBlock().getTypeId(),corner.getBlock().getData());
				}
				if (p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 14).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 14).getX(),p.getWorld().getHighestBlockYAt(p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 14).getLocation()) - 1,p.getWorld().getChunkAt(lastMarkX,lastMarkY).getBlock(15, 0, 14).getZ()).getLocation();
					p.sendBlockChange(corner,corner.getBlock().getTypeId(),corner.getBlock().getData());
				}
				
				
				
				if (p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(0, 0, 0).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getLocation().getChunk().getBlock(0, 0, 0).getX(),p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(0, 0, 0).getLocation()) - 1,p.getLocation().getChunk().getBlock(0, 0, 0).getZ()).getLocation();
					p.sendBlockChange(corner,markBlock,(byte)(char) markData);
				}
				if (p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(1, 0, 0).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getLocation().getChunk().getBlock(1, 0, 0).getX(),p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(1, 0, 0).getLocation()) - 1,p.getLocation().getChunk().getBlock(1, 0, 0).getZ()).getLocation();
					p.sendBlockChange(corner,markBlock,(byte)(char) markData);
				}
				if (p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(0, 0, 1).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getLocation().getChunk().getBlock(0, 0, 1).getX(),p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(0, 0, 1).getLocation()) - 1,p.getLocation().getChunk().getBlock(0, 0, 1).getZ()).getLocation();
					p.sendBlockChange(corner,markBlock,(byte)(char) markData);
				}
				
				
				if (p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(15, 0, 0).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getLocation().getChunk().getBlock(15, 0, 0).getX(),p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(15, 0, 0).getLocation()) - 1,p.getLocation().getChunk().getBlock(15, 0, 0).getZ()).getLocation();
					p.sendBlockChange(corner,markBlock,(byte)(char) markData);
				}
				if (p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(14, 0, 0).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getLocation().getChunk().getBlock(14, 0, 0).getX(),p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(14, 0, 0).getLocation()) - 1,p.getLocation().getChunk().getBlock(14, 0, 0).getZ()).getLocation();
					p.sendBlockChange(corner,markBlock,(byte)(char) markData);
				}
				if (p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(15, 0, 1).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getLocation().getChunk().getBlock(15, 0, 1).getX(),p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(15, 0, 1).getLocation()) - 1,p.getLocation().getChunk().getBlock(15, 0, 1).getZ()).getLocation();
					p.sendBlockChange(corner,markBlock,(byte)(char) markData);
				}
				
				if (p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(0, 0, 15).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getLocation().getChunk().getBlock(0, 0, 15).getX(),p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(0, 0, 15).getLocation()) - 1,p.getLocation().getChunk().getBlock(0, 0, 15).getZ()).getLocation();
					p.sendBlockChange(corner,markBlock,(byte)(char) markData);
				}
				if (p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(1, 0, 15).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getLocation().getChunk().getBlock(1, 0, 15).getX(),p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(1, 0, 15).getLocation()) - 1,p.getLocation().getChunk().getBlock(1, 0, 15).getZ()).getLocation();
					p.sendBlockChange(corner,markBlock,(byte)(char) markData);
				}
				if (p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(0, 0, 14).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getLocation().getChunk().getBlock(0, 0, 14).getX(),p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(0, 0, 14).getLocation()) - 1,p.getLocation().getChunk().getBlock(0, 0, 14).getZ()).getLocation();
					p.sendBlockChange(corner,markBlock,(byte)(char) markData);
				}
				
				if (p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(15, 0, 15).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getLocation().getChunk().getBlock(15, 0, 15).getX(),p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(15, 0, 15).getLocation()) - 1,p.getLocation().getChunk().getBlock(15, 0, 15).getZ()).getLocation();
					p.sendBlockChange(corner,markBlock,(byte)(char) markData);
				}
				if (p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(14, 0, 15).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getLocation().getChunk().getBlock(14, 0, 15).getX(),p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(14, 0, 15).getLocation()) - 1,p.getLocation().getChunk().getBlock(14, 0, 15).getZ()).getLocation();
					p.sendBlockChange(corner,markBlock,(byte)(char) markData);
				}
				if (p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(15, 0, 14).getLocation()) > 1) {
					corner = p.getWorld().getBlockAt(p.getLocation().getChunk().getBlock(15, 0, 14).getX(),p.getWorld().getHighestBlockYAt(p.getLocation().getChunk().getBlock(15, 0, 14).getLocation()) - 1,p.getLocation().getChunk().getBlock(15, 0, 14).getZ()).getLocation();
					p.sendBlockChange(corner,markBlock,(byte)(char) markData);
				}
				
				lastMarkX = p.getLocation().getChunk().getX();
				lastMarkY = p.getLocation().getChunk().getZ();
				lastMark = lastMarkX + "," + lastMarkY;
				getConfig().set("main.players." + p.getName() + ".lastmark", lastMark);
				saveConfig();
			}
		}
	}
	
}
