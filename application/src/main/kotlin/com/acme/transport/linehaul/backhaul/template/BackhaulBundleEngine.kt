package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the backhaul bundle calculation. */
interface BackhaulBundleModifier {
    fun describe(): String
    fun adjust(item: BackhaulBundle, value: Double): Double
}

data class RoundingBackhaulBundleAdjuster(var step: Double) : BackhaulBundleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulBundle, value: Double): Double = roundTo(value, step)
}

data class LabelBackhaulBundleAdjuster(var factors: MutableMap<String, Double>) : BackhaulBundleModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: BackhaulBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BackhaulBundleAmountOffsetAdjuster(var weight: Double) : BackhaulBundleModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: BackhaulBundle, value: Double): Double = value + item.amount * weight
}

data class FlaggedBackhaulBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulBundleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapBackhaulBundleAdjuster(var cap: Double) : BackhaulBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulBundle, value: Double): Double = minOf(value, cap)
}

data class MinimumBackhaulBundleAdjuster(var minimum: Double) : BackhaulBundleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the backhaul bundle steps in order and keeps a trace of each. */
data class BackhaulBundleEngine(
    var steps: MutableList<BackhaulBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BackhaulBundle): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BackhaulBundleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BackhaulBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBackhaulBundleEngine(flags: Registry): BackhaulBundleEngine {
    return BackhaulBundleEngine(
        steps = mutableListOf<BackhaulBundleModifier>(
            RoundingBackhaulBundleAdjuster(1.0),
            LabelBackhaulBundleAdjuster(mutableMapOf<String, Double>("east" to 1.21, "central" to 0.78)),
            BackhaulBundleAmountOffsetAdjuster(0.9),
            FlaggedBackhaulBundleAdjuster(flags, "backhaul-bundle-weighted", -3.4),
            CapBackhaulBundleAdjuster(30637.0),
            MinimumBackhaulBundleAdjuster(7.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 38661.1,
    )
}
