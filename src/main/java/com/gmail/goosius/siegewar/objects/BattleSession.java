package com.gmail.goosius.siegewar.objects;


import com.gmail.goosius.siegewar.Messaging;
import com.gmail.goosius.siegewar.settings.SiegeWarSettings;
import com.gmail.goosius.siegewar.settings.Translation;
import com.palmergames.util.TimeMgmt;

import java.time.*;
import java.time.temporal.TemporalField;

import static com.palmergames.util.TimeMgmt.ONE_HOUR_IN_MILLIS;

/**
 * This class represents a "Battle Session".
 *
 * A battle session is period of time, typically 50 minutes,
 * in which individual "battles" take place at each siege.
 *
 * During a battle, siege points can be gained/lost.
 *
 * At the end of a battle session:
 * - The winner of each battle is declared
 *    - either the "Attacker" or the "Defender", or "Draw"
 *    - If nobody contested a battle, no winner is declared.
 * - All scoreboards will show that the winner won +1 "battle".
 * - Siege points are reset to 0
 * - Plunder is awarded to any soldiers on the winning side,
 *   who gained banner control during the battle.
 *   (Attackers steal from the town, defenders steals from the war chest)
 *
 * Until the next battle session starts:
 * - This period is known as a "break", and typically lasts 10 minutes.
 * - During this period, siege points remain at 0, and banner control is not possible.
 *
 * At the normal end of a siege, whoever has won the most battles, wins the siege.
 *
 * @author Goosius
 */
public class BattleSession {

	private static BattleSession battleSession = null;  //The singleton instance
	private boolean active; 			//Is the session active, or is it on break ?
	private long scheduledEndTIme;	//The time this battle session is scheduled to end

	public BattleSession() {
		active = false;
		scheduledEndTIme = 0;
	}

	//Singleton
	public static BattleSession getBattleSession() {
		if(battleSession == null) {
			battleSession = new BattleSession();
		}
		return battleSession;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public long getScheduledEndTime() {
		return scheduledEndTIme;
	}

	public void setScheduledEndTime(long t) {
		scheduledEndTIme = t;
	}

	public String getFormattedTimeRemainingUntilBattleSessionEnds() {
		double timeLeftMillis = scheduledEndTIme - System.currentTimeMillis();
		if(timeLeftMillis > 0) {
			return TimeMgmt.getFormattedTimeValue(timeLeftMillis);
		} else {
			return "N/A";
		}
	}

	public String getFormattedTimeUntilNextBattleSessionStarts() {
		return TimeMgmt.getFormattedTimeValue(getTimeOfNextBattleSessionMillis());
	}

	public static long getTimeOfNextBattleSessionMillis() {
		LocalDateTime currentDateTime = LocalDateTime.now(Clock.systemUTC());
		Duration closestDuration = null;
		Duration candidateDuration;
		LocalTime candidateTime;
		LocalDate candidateDate;
		LocalDateTime candidateDateTime;
		String[] startTimeHourMinutePair;
		for (String startTime : SiegeWarSettings.getWarSiegeBattleSessionsStartTimesUtc()) {
			if (startTime.contains(":")) {
				startTimeHourMinutePair = startTime.split(":");
				candidateTime = LocalTime.of(Integer.parseInt(startTimeHourMinutePair[0]), Integer.parseInt(startTimeHourMinutePair[1]));
			} else {
				candidateTime = LocalTime.of(Integer.parseInt(startTime), 0);
			}

			//Convert candidate to local date time
			if (candidateTime.isAfter(currentDateTime.toLocalTime())) {
				candidateDate = LocalDate.now(Clock.systemUTC());
			} else {
				candidateDate = LocalDate.now(Clock.systemUTC()).plusDays(1);
			}
			candidateDateTime = LocalDateTime.of(candidateDate, candidateTime);

			//Make this candidate our favourite if it is closer
			candidateDuration = Duration.between(currentDateTime, candidateDateTime);
			if (closestDuration == null) {
				closestDuration = candidateDuration;
			} else if (candidateDuration.getSeconds() < closestDuration.getSeconds())
				closestDuration = candidateDuration;
			}

		//Return closest duration
		return closestDuration.getSeconds() * 10000;
	}
}
