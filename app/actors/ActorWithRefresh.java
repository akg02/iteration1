package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * An abstract actor with refresh behavior
 */
abstract class ActorWithRefresh extends AbstractActor {
    private final ActorSystem actorSystem;
    protected final Duration refreshInterval;
    private Cancellable scheduler;
    protected static final Object TICK = new Object();
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Constructor
     */
    protected ActorWithRefresh(ActorSystem actorSystem, Duration refreshInterval) {
        this.actorSystem = actorSystem;
        this.refreshInterval = refreshInterval;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        log.info("create a refresher with interval {}", refreshInterval);
        this.scheduler = actorSystem.scheduler().scheduleAtFixedRate(
                Duration.ZERO,
                refreshInterval,
                () -> self().tell(TICK, ActorRef.noSender()),
                actorSystem.dispatcher());
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        if (scheduler != null) {
            log.info("stop refresher");
            scheduler.cancel();
            scheduler = null;
        }
    }

    /**
     * Override to implement the refresh behavior
     */
    protected abstract void doRefresh();
}
