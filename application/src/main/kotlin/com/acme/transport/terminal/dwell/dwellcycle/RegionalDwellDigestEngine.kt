package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional dwell digest calculation. */
interface RegionalDwellDigestFactor {
    fun describe(): String
    fun adjust(item: RegionalDwellDigest, value: Double): Double
}

data class RoundingRegionalDwellDigestAdjuster(var step: Double) : RegionalDwellDigestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDwellDigest, value: Double): Double = roundTo(value, step)
}

data class FlaggedRegionalDwellDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDwellDigestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDwellDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionalDwellDigestAmountOffsetAdjuster(var weight: Double) : RegionalDwellDigestFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: RegionalDwellDigest, value: Double): Double = value + item.amount * weight
}

data class PercentRegionalDwellDigestAdjuster(var percent: Double) : RegionalDwellDigestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDwellDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelRegionalDwellDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalDwellDigestFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: RegionalDwellDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional dwell digest steps in order and keeps a trace of each. */
data class RegionalDwellDigestEngine(
    var steps: MutableList<RegionalDwellDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalDwellDigest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDwellDigestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDwellDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalDwellDigestEngine(flags: Registry): RegionalDwellDigestEngine {
    return RegionalDwellDigestEngine(
        steps = mutableListOf<RegionalDwellDigestFactor>(
            RoundingRegionalDwellDigestAdjuster(5.0),
            FlaggedRegionalDwellDigestAdjuster(flags, "regional-dwell-digest-pending", 1.6),
            RegionalDwellDigestAmountOffsetAdjuster(1.26),
            PercentRegionalDwellDigestAdjuster(-23.7),
            LabelRegionalDwellDigestAdjuster(mutableMapOf<String, Double>("online" to 1.25)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 9378.6,
    )
}
