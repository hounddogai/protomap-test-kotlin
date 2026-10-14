package com.acme.transport.network.transittime.timebudget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the transit time target calculation. */
interface TransitTimeTargetFactor {
    fun describe(): String
    fun adjust(item: TransitTimeTarget, value: Double): Double
}

data class PercentTransitTimeTargetAdjuster(var percent: Double) : TransitTimeTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTransitTimeTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TransitTimeTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TransitTimeTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UnitTransitTimeTargetAdjuster(var factors: MutableMap<String, Double>) : TransitTimeTargetFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: TransitTimeTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTransitTimeTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimeTargetFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimeTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the transit time target steps in order and keeps a trace of each. */
data class TransitTimeTargetEngine(
    var steps: MutableList<TransitTimeTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TransitTimeTarget): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TransitTimeTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTransitTimeTargetEngine(flags: Registry): TransitTimeTargetEngine {
    return TransitTimeTargetEngine(
        steps = mutableListOf<TransitTimeTargetFactor>(
            PercentTransitTimeTargetAdjuster(20.2),
            FlaggedTransitTimeTargetAdjuster(flags, "transit-time-target-regional", 5.2),
            UnitTransitTimeTargetAdjuster(
                mutableMapOf<String, Double>("west" to 0.9, "central" to 1.09, "north" to 0.93),
            ),
            TieredTransitTimeTargetAdjuster(
                mutableListOf<Double>(777.0, 5078.0, 6673.0, 8143.0),
                mutableListOf<Double>(1.17, 0.85, 0.96, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 44695.7,
    )
}
