package com.acme.transport.linehaul.tender.tenderrollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled tender cycle calculation. */
interface ScheduledTenderCycleStep {
    fun describe(): String
    fun adjust(item: ScheduledTenderCycle, value: Double): Double
}

data class RoundingScheduledTenderCycleAdjuster(var step: Double) : ScheduledTenderCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledTenderCycle, value: Double): Double = roundTo(value, step)
}

data class PercentScheduledTenderCycleAdjuster(var percent: Double) : ScheduledTenderCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledTenderCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedScheduledTenderCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledTenderCycleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledTenderCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScheduledTenderCycleAdjuster(var minimum: Double) : ScheduledTenderCycleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledTenderCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled tender cycle steps in order and keeps a trace of each. */
data class ScheduledTenderCycleEngine(
    var steps: MutableList<ScheduledTenderCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledTenderCycle): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledTenderCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledTenderCycleEngine(flags: Registry): ScheduledTenderCycleEngine {
    return ScheduledTenderCycleEngine(
        steps = mutableListOf<ScheduledTenderCycleStep>(
            RoundingScheduledTenderCycleAdjuster(0.5),
            PercentScheduledTenderCycleAdjuster(19.3),
            FlaggedScheduledTenderCycleAdjuster(flags, "scheduled-tender-cycle-tiered", 3.4),
            MinimumScheduledTenderCycleAdjuster(49.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 13569.3,
    )
}
