package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the transit time group calculation. */
interface TransitTimeGroupFactor {
    fun describe(): String
    fun adjust(item: TransitTimeGroup, value: Double): Double
}

data class RoundingTransitTimeGroupAdjuster(var step: Double) : TransitTimeGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeGroup, value: Double): Double = roundTo(value, step)
}

data class CapTransitTimeGroupAdjuster(var cap: Double) : TransitTimeGroupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeGroup, value: Double): Double = minOf(value, cap)
}

data class FlaggedTransitTimeGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TransitTimeGroupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TransitTimeGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameTransitTimeGroupAdjuster(var factors: MutableMap<String, Double>) : TransitTimeGroupFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: TransitTimeGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTransitTimeGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimeGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimeGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the transit time group steps in order and keeps a trace of each. */
data class TransitTimeGroupEngine(
    var steps: MutableList<TransitTimeGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TransitTimeGroup): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TransitTimeGroupFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TransitTimeGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTransitTimeGroupEngine(flags: Registry): TransitTimeGroupEngine {
    return TransitTimeGroupEngine(
        steps = mutableListOf<TransitTimeGroupFactor>(
            RoundingTransitTimeGroupAdjuster(0.25),
            CapTransitTimeGroupAdjuster(11622.0),
            FlaggedTransitTimeGroupAdjuster(flags, "transit-time-group-regional", -7.6),
            NameTransitTimeGroupAdjuster(mutableMapOf<String, Double>("west" to 0.72, "central" to 1.28)),
            TieredTransitTimeGroupAdjuster(
                mutableListOf<Double>(40.0, 7192.0),
                mutableListOf<Double>(0.96, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 29821.5,
    )
}
