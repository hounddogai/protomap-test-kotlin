package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional parcel variance calculation. */
interface RegionalParcelVarianceFactor {
    fun describe(): String
    fun adjust(item: RegionalParcelVariance, value: Double): Double
}

data class RegionalParcelVarianceCapacityOffsetAdjuster(var weight: Double) : RegionalParcelVarianceFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: RegionalParcelVariance, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class PercentRegionalParcelVarianceAdjuster(var percent: Double) : RegionalParcelVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalParcelVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRegionalParcelVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalParcelVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalParcelVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalParcelVarianceAdjuster(var cap: Double) : RegionalParcelVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalParcelVariance, value: Double): Double = minOf(value, cap)
}

data class DepotRegionalParcelVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalParcelVarianceFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: RegionalParcelVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRegionalParcelVarianceAdjuster(var step: Double) : RegionalParcelVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalParcelVariance, value: Double): Double = roundTo(value, step)
}

/** Runs the regional parcel variance steps in order and keeps a trace of each. */
data class RegionalParcelVarianceEngine(
    var steps: MutableList<RegionalParcelVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalParcelVariance): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalParcelVarianceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalParcelVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalParcelVarianceEngine(flags: Registry): RegionalParcelVarianceEngine {
    return RegionalParcelVarianceEngine(
        steps = mutableListOf<RegionalParcelVarianceFactor>(
            RegionalParcelVarianceCapacityOffsetAdjuster(1.75),
            PercentRegionalParcelVarianceAdjuster(-20.2),
            FlaggedRegionalParcelVarianceAdjuster(flags, "regional-parcel-variance-global", -5.1),
            CapRegionalParcelVarianceAdjuster(15199.0),
            DepotRegionalParcelVarianceAdjuster(mutableMapOf<String, Double>("east" to 0.84, "north" to 1.15)),
            RoundingRegionalParcelVarianceAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 11960.5,
    )
}
