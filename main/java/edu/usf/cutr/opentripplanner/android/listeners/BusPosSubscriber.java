package edu.usf.cutr.opentripplanner.android.listeners;

import redis.clients.jedis.Jedis;
import edu.usf.cutr.opentripplanner.android.fragments.PosSubscriber;
import edu.usf.cutr.opentripplanner.android.tasks.ItractSubscriber;

public interface BusPosSubscriber {

	public void onItractBusPosNotify(ItractSubscriber sub);

	public void setSub(PosSubscriber subscriber);

}
