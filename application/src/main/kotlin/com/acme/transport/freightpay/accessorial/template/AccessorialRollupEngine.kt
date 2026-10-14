package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the accessorial rollup calculation. */
interface AccessorialRollupStep {
    fun describe(): String
    fun adjust(item: AccessorialRollup, value: Double): Double
}

data class MinimumAccessorialRollupAdjuster(var minimum: Double) : AccessorialRollupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAccessorialRollupAdjuster(var step: Double) : AccessorialRollupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialRollup, value: Double): Double = roundTo(value, step)
}

data class FlaggedAccessorialRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AccessorialRollupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AccessorialRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameAccessorialRollupAdjuster(var factors: MutableMap<String, Double>) : AccessorialRollupStep {
    override fun describe(): String = "by name"

    override fun adjust(item: AccessorialRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAccessorialRollupAdjuster(var cap: Double) : AccessorialRollupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AccessorialRollup, value: Double): Double = minOf(value, cap)
}

data class AccessorialRollupAmountOffsetAdjuster(var weight: Double) : AccessorialRollupStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: AccessorialRollup, value: Double): Double = value + item.amount * weight
}

/** Runs the accessorial rollup steps in order and keeps a trace of each. */
data class AccessorialRollupEngine(
    var steps: MutableList<AccessorialRollupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AccessorialRollup): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAccessorialRollupEngine(flags: Registry): AccessorialRollupEngine {
    return AccessorialRollupEngine(
        steps = mutableListOf<AccessorialRollupStep>(
            MinimumAccessorialRollupAdjuster(21.5),
            RoundingAccessorialRollupAdjuster(0.05),
            FlaggedAccessorialRollupAdjuster(flags, "accessorial-rollup-batched", -8.5),
            NameAccessorialRollupAdjuster(mutableMapOf<String, Double>("central" to 1.23)),
            CapAccessorialRollupAdjuster(18792.0),
            AccessorialRollupAmountOffsetAdjuster(1.27),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 63030.1,
    )
}
