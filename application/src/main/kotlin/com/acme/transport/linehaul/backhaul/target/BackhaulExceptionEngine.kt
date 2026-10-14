package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the backhaul exception calculation. */
interface BackhaulExceptionStep {
    fun describe(): String
    fun adjust(item: BackhaulException, value: Double): Double
}

data class CapBackhaulExceptionAdjuster(var cap: Double) : BackhaulExceptionStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulException, value: Double): Double = minOf(value, cap)
}

data class FlaggedBackhaulExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulExceptionStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryBackhaulExceptionAdjuster(var factors: MutableMap<String, Double>) : BackhaulExceptionStep {
    override fun describe(): String = "by category"

    override fun adjust(item: BackhaulException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentBackhaulExceptionAdjuster(var percent: Double) : BackhaulExceptionStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBackhaulExceptionAdjuster(var minimum: Double) : BackhaulExceptionStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the backhaul exception steps in order and keeps a trace of each. */
data class BackhaulExceptionEngine(
    var steps: MutableList<BackhaulExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BackhaulException): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBackhaulExceptionEngine(flags: Registry): BackhaulExceptionEngine {
    return BackhaulExceptionEngine(
        steps = mutableListOf<BackhaulExceptionStep>(
            CapBackhaulExceptionAdjuster(6577.0),
            FlaggedBackhaulExceptionAdjuster(flags, "backhaul-exception-global", -2.2),
            CategoryBackhaulExceptionAdjuster(mutableMapOf<String, Double>("online" to 1.32)),
            PercentBackhaulExceptionAdjuster(-6.3),
            MinimumBackhaulExceptionAdjuster(49.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 88468.5,
    )
}
