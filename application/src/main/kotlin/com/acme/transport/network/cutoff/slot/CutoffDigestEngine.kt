package com.acme.transport.network.cutoff.slot

import com.acme.transport.kit.numbers.roundTo

/** One step of the cutoff digest calculation. */
interface CutoffDigestAdjuster {
    fun describe(): String
    fun adjust(item: CutoffDigest, value: Double): Double
}

data class NameCutoffDigestAdjuster(var factors: MutableMap<String, Double>) : CutoffDigestAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: CutoffDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CutoffDigestUpdatedAtOffsetAdjuster(var weight: Double) : CutoffDigestAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: CutoffDigest, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class CapCutoffDigestAdjuster(var cap: Double) : CutoffDigestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffDigest, value: Double): Double = minOf(value, cap)
}

data class RoundingCutoffDigestAdjuster(var step: Double) : CutoffDigestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffDigest, value: Double): Double = roundTo(value, step)
}

data class TieredCutoffDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffDigestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the cutoff digest steps in order and keeps a trace of each. */
data class CutoffDigestEngine(
    var steps: MutableList<CutoffDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CutoffDigest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CutoffDigestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CutoffDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCutoffDigestEngine(): CutoffDigestEngine {
    return CutoffDigestEngine(
        steps = mutableListOf<CutoffDigestAdjuster>(
            NameCutoffDigestAdjuster(mutableMapOf<String, Double>("central" to 1.28, "south" to 1.38)),
            CutoffDigestUpdatedAtOffsetAdjuster(0.5),
            CapCutoffDigestAdjuster(6965.0),
            RoundingCutoffDigestAdjuster(1.0),
            TieredCutoffDigestAdjuster(
                mutableListOf<Double>(356.0, 882.0, 2713.0),
                mutableListOf<Double>(0.88, 0.93, 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 95524.6,
    )
}
