package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending cross dock summary calculation. */
interface PendingCrossDockSummaryFactor {
    fun describe(): String
    fun adjust(item: PendingCrossDockSummary, value: Double): Double
}

data class CategoryPendingCrossDockSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingCrossDockSummaryFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: PendingCrossDockSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapPendingCrossDockSummaryAdjuster(var cap: Double) : PendingCrossDockSummaryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingCrossDockSummary, value: Double): Double = minOf(value, cap)
}

data class MinimumPendingCrossDockSummaryAdjuster(var minimum: Double) : PendingCrossDockSummaryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingCrossDockSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPendingCrossDockSummaryAdjuster(var percent: Double) : PendingCrossDockSummaryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingCrossDockSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PendingCrossDockSummarySequenceOffsetAdjuster(var weight: Double) : PendingCrossDockSummaryFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: PendingCrossDockSummary, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class FlaggedPendingCrossDockSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingCrossDockSummaryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingCrossDockSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending cross dock summary steps in order and keeps a trace of each. */
data class PendingCrossDockSummaryEngine(
    var steps: MutableList<PendingCrossDockSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingCrossDockSummary): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingCrossDockSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingCrossDockSummaryEngine(flags: Registry): PendingCrossDockSummaryEngine {
    return PendingCrossDockSummaryEngine(
        steps = mutableListOf<PendingCrossDockSummaryFactor>(
            CategoryPendingCrossDockSummaryAdjuster(mutableMapOf<String, Double>("retail" to 1.03)),
            CapPendingCrossDockSummaryAdjuster(22192.0),
            MinimumPendingCrossDockSummaryAdjuster(37.8),
            PercentPendingCrossDockSummaryAdjuster(-1.7),
            PendingCrossDockSummarySequenceOffsetAdjuster(0.52),
            FlaggedPendingCrossDockSummaryAdjuster(flags, "pending-cross-dock-summary-regional", -5.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 60475.6,
    )
}
