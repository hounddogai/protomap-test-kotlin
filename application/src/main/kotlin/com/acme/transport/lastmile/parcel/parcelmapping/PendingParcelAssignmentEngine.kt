package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending parcel assignment calculation. */
interface PendingParcelAssignmentFactor {
    fun describe(): String
    fun adjust(item: PendingParcelAssignment, value: Double): Double
}

data class FlaggedPendingParcelAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingParcelAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingParcelAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPendingParcelAssignmentAdjuster(var percent: Double) : PendingParcelAssignmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingParcelAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingPendingParcelAssignmentAdjuster(var step: Double) : PendingParcelAssignmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingParcelAssignment, value: Double): Double = roundTo(value, step)
}

data class PendingParcelAssignmentExpiresAtOffsetAdjuster(var weight: Double) : PendingParcelAssignmentFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: PendingParcelAssignment, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class CountryPendingParcelAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingParcelAssignmentFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: PendingParcelAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPendingParcelAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingParcelAssignmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingParcelAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending parcel assignment steps in order and keeps a trace of each. */
data class PendingParcelAssignmentEngine(
    var steps: MutableList<PendingParcelAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingParcelAssignment): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingParcelAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingParcelAssignmentEngine(flags: Registry): PendingParcelAssignmentEngine {
    return PendingParcelAssignmentEngine(
        steps = mutableListOf<PendingParcelAssignmentFactor>(
            FlaggedPendingParcelAssignmentAdjuster(flags, "pending-parcel-assignment-global", 0.3),
            PercentPendingParcelAssignmentAdjuster(-3.7),
            RoundingPendingParcelAssignmentAdjuster(1.0),
            PendingParcelAssignmentExpiresAtOffsetAdjuster(1.78),
            CountryPendingParcelAssignmentAdjuster(mutableMapOf<String, Double>("central" to 0.87)),
            TieredPendingParcelAssignmentAdjuster(
                mutableListOf<Double>(4952.0, 8307.0),
                mutableListOf<Double>(0.96, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 95967.4,
    )
}
