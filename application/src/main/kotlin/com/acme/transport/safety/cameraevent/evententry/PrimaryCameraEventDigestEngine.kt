package com.acme.transport.safety.cameraevent.evententry

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary camera event digest calculation. */
interface PrimaryCameraEventDigestFactor {
    fun describe(): String
    fun adjust(item: PrimaryCameraEventDigest, value: Double): Double
}

data class PercentPrimaryCameraEventDigestAdjuster(var percent: Double) : PrimaryCameraEventDigestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryCameraEventDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPrimaryCameraEventDigestAdjuster(var cap: Double) : PrimaryCameraEventDigestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryCameraEventDigest, value: Double): Double = minOf(value, cap)
}

data class MinimumPrimaryCameraEventDigestAdjuster(var minimum: Double) : PrimaryCameraEventDigestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryCameraEventDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPrimaryCameraEventDigestAdjuster(var step: Double) : PrimaryCameraEventDigestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryCameraEventDigest, value: Double): Double = roundTo(value, step)
}

data class LocalePrimaryCameraEventDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryCameraEventDigestFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: PrimaryCameraEventDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PrimaryCameraEventDigestSequenceOffsetAdjuster(var weight: Double) : PrimaryCameraEventDigestFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: PrimaryCameraEventDigest, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

/** Runs the primary camera event digest steps in order and keeps a trace of each. */
data class PrimaryCameraEventDigestEngine(
    var steps: MutableList<PrimaryCameraEventDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryCameraEventDigest): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryCameraEventDigestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryCameraEventDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryCameraEventDigestEngine(): PrimaryCameraEventDigestEngine {
    return PrimaryCameraEventDigestEngine(
        steps = mutableListOf<PrimaryCameraEventDigestFactor>(
            PercentPrimaryCameraEventDigestAdjuster(-20.6),
            CapPrimaryCameraEventDigestAdjuster(26587.0),
            MinimumPrimaryCameraEventDigestAdjuster(36.6),
            RoundingPrimaryCameraEventDigestAdjuster(5.0),
            LocalePrimaryCameraEventDigestAdjuster(mutableMapOf<String, Double>("north" to 1.23)),
            PrimaryCameraEventDigestSequenceOffsetAdjuster(1.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 58854.1,
    )
}
