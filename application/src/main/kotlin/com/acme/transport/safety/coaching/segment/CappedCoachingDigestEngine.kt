package com.acme.transport.safety.coaching.segment

/** One step of the capped coaching digest calculation. */
interface CappedCoachingDigestModifier {
    fun describe(): String
    fun adjust(item: CappedCoachingDigest, value: Double): Double
}

data class TieredCappedCoachingDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedCoachingDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedCoachingDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedCoachingDigestAdjuster(var cap: Double) : CappedCoachingDigestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedCoachingDigest, value: Double): Double = minOf(value, cap)
}

data class PercentCappedCoachingDigestAdjuster(var percent: Double) : CappedCoachingDigestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCoachingDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped coaching digest steps in order and keeps a trace of each. */
data class CappedCoachingDigestEngine(
    var steps: MutableList<CappedCoachingDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedCoachingDigest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedCoachingDigestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedCoachingDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedCoachingDigestEngine(): CappedCoachingDigestEngine {
    return CappedCoachingDigestEngine(
        steps = mutableListOf<CappedCoachingDigestModifier>(
            TieredCappedCoachingDigestAdjuster(
                mutableListOf<Double>(821.0, 5616.0, 5977.0, 7757.0),
                mutableListOf<Double>(0.92, 0.9, 0.93, 0.95),
            ),
            CapCappedCoachingDigestAdjuster(28980.0),
            PercentCappedCoachingDigestAdjuster(-4.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 69316.2,
    )
}
