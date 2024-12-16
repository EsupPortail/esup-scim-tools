package org.esupportail.scim.server;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.util.StopWatch;

import java.time.Duration;

public class PrettyStopWatch extends StopWatch {

    @Override
    public String shortSummary() {
        Duration duration = Duration.ofNanos(getTotalTimeNanos());
        String timeInMMSS = DurationFormatUtils.formatDuration(getTotalTimeMillis(), "mm 'min' ss 'sec'", false);
        return "StopWatch '" + getId() + "': running time = " + timeInMMSS + "";
    }
}
