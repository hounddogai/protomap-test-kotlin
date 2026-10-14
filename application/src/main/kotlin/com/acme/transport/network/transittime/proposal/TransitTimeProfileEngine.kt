package com.acme.transport.network.transittime.proposal

/** One step of the transit time profile calculation. */
interface TransitTimeProfileFactor {
    fun describe(): String
    fun adjust(item: TransitTimeProfile, value: Double): Double
}

data class TransitTimeProfileGraceDaysOffsetAdjuster(var weight: Double) : TransitTimeProfileFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: TransitTimeProfile, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class PercentTransitTimeProfileAdjuster(var percent: Double) : TransitTimeProfileFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneTransitTimeProfileAdjuster(var factors: MutableMap<String, Double>) : TransitTimeProfileFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: TransitTimeProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTransitTimeProfileAdjuster(var minimum: Double) : TransitTimeProfileFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the transit time profile steps in order and keeps a trace of each. */
data class TransitTimeProfileEngine(
    var steps: MutableList<TransitTimeProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TransitTimeProfile): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TransitTimeProfileFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TransitTimeProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTransitTimeProfileEngine(): TransitTimeProfileEngine {
    return TransitTimeProfileEngine(
        steps = mutableListOf<TransitTimeProfileFactor>(
            TransitTimeProfileGraceDaysOffsetAdjuster(0.39),
            PercentTransitTimeProfileAdjuster(17.0),
            LaneTransitTimeProfileAdjuster(
                mutableMapOf<String, Double>("west" to 0.79, "retail" to 1.3, "north" to 1.08),
            ),
            MinimumTransitTimeProfileAdjuster(43.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 88506.3,
    )
}
