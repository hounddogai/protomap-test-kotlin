package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary backhaul score calculation. */
interface PrimaryBackhaulScoreAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryBackhaulScore, value: Double): Double
}

data class MinimumPrimaryBackhaulScoreAdjuster(var minimum: Double) : PrimaryBackhaulScoreAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryBackhaulScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPrimaryBackhaulScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryBackhaulScoreAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryBackhaulScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PrimaryBackhaulScorePalletsOffsetAdjuster(var weight: Double) : PrimaryBackhaulScoreAdjuster {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: PrimaryBackhaulScore, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class TieredPrimaryBackhaulScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryBackhaulScoreAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryBackhaulScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary backhaul score steps in order and keeps a trace of each. */
data class PrimaryBackhaulScoreEngine(
    var steps: MutableList<PrimaryBackhaulScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryBackhaulScore): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryBackhaulScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryBackhaulScoreEngine(flags: Registry): PrimaryBackhaulScoreEngine {
    return PrimaryBackhaulScoreEngine(
        steps = mutableListOf<PrimaryBackhaulScoreAdjuster>(
            MinimumPrimaryBackhaulScoreAdjuster(43.6),
            FlaggedPrimaryBackhaulScoreAdjuster(flags, "primary-backhaul-score-capped", 5.7),
            PrimaryBackhaulScorePalletsOffsetAdjuster(0.64),
            TieredPrimaryBackhaulScoreAdjuster(
                mutableListOf<Double>(937.0, 7610.0),
                mutableListOf<Double>(1.09, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 96189.3,
    )
}
