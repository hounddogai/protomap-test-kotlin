package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the transit time bundle calculation. */
interface TransitTimeBundleFactor {
    fun describe(): String
    fun adjust(item: TransitTimeBundle, value: Double): Double
}

data class PercentTransitTimeBundleAdjuster(var percent: Double) : TransitTimeBundleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TransitTimeBundleEtaMinutesOffsetAdjuster(var weight: Double) : TransitTimeBundleFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: TransitTimeBundle, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class CapTransitTimeBundleAdjuster(var cap: Double) : TransitTimeBundleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeBundle, value: Double): Double = minOf(value, cap)
}

data class CategoryTransitTimeBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : TransitTimeBundleFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: TransitTimeBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTransitTimeBundleAdjuster(var minimum: Double) : TransitTimeBundleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingTransitTimeBundleAdjuster(var step: Double) : TransitTimeBundleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeBundle, value: Double): Double = roundTo(value, step)
}

/** Runs the transit time bundle steps in order and keeps a trace of each. */
data class TransitTimeBundleEngine(
    var steps: MutableList<TransitTimeBundleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TransitTimeBundle): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TransitTimeBundleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TransitTimeBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTransitTimeBundleEngine(): TransitTimeBundleEngine {
    return TransitTimeBundleEngine(
        steps = mutableListOf<TransitTimeBundleFactor>(
            PercentTransitTimeBundleAdjuster(11.1),
            TransitTimeBundleEtaMinutesOffsetAdjuster(0.94),
            CapTransitTimeBundleAdjuster(27561.0),
            CategoryTransitTimeBundleAdjuster(mutableMapOf<String, Double>("north" to 1.38, "online" to 0.77)),
            MinimumTransitTimeBundleAdjuster(15.9),
            RoundingTransitTimeBundleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 44287.1,
    )
}
