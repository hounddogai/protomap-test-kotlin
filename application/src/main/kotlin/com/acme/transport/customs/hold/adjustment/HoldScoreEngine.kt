package com.acme.transport.customs.hold.adjustment

import com.acme.transport.kit.flags.Registry

/** One step of the hold score calculation. */
interface HoldScoreModifier {
    fun describe(): String
    fun adjust(item: HoldScore, value: Double): Double
}

data class HoldScoreAttemptsOffsetAdjuster(var weight: Double) : HoldScoreModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: HoldScore, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class CapHoldScoreAdjuster(var cap: Double) : HoldScoreModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HoldScore, value: Double): Double = minOf(value, cap)
}

data class FlaggedHoldScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldScoreModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameHoldScoreAdjuster(var factors: MutableMap<String, Double>) : HoldScoreModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: HoldScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredHoldScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HoldScoreModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HoldScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentHoldScoreAdjuster(var percent: Double) : HoldScoreModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HoldScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the hold score steps in order and keeps a trace of each. */
data class HoldScoreEngine(
    var steps: MutableList<HoldScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HoldScore): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HoldScoreModifier = steps[index]
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

    fun evaluateAll(items: MutableList<HoldScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHoldScoreEngine(flags: Registry): HoldScoreEngine {
    return HoldScoreEngine(
        steps = mutableListOf<HoldScoreModifier>(
            HoldScoreAttemptsOffsetAdjuster(1.42),
            CapHoldScoreAdjuster(32847.0),
            FlaggedHoldScoreAdjuster(flags, "hold-score-capped", -9.5),
            NameHoldScoreAdjuster(mutableMapOf<String, Double>("north" to 1.0)),
            TieredHoldScoreAdjuster(mutableListOf<Double>(7770.0, 8004.0), mutableListOf<Double>(1.03, 0.88)),
            PercentHoldScoreAdjuster(-13.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 4962.7,
    )
}
