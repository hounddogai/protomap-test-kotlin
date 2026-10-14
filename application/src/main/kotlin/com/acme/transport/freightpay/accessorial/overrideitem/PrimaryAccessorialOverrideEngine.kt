package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary accessorial override calculation. */
interface PrimaryAccessorialOverrideAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryAccessorialOverride, value: Double): Double
}

data class CapPrimaryAccessorialOverrideAdjuster(var cap: Double) : PrimaryAccessorialOverrideAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryAccessorialOverride, value: Double): Double = minOf(value, cap)
}

data class NamePrimaryAccessorialOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryAccessorialOverrideAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: PrimaryAccessorialOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingPrimaryAccessorialOverrideAdjuster(var step: Double) : PrimaryAccessorialOverrideAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryAccessorialOverride, value: Double): Double = roundTo(value, step)
}

data class PrimaryAccessorialOverrideLimitOffsetAdjuster(
    var weight: Double,
) : PrimaryAccessorialOverrideAdjuster {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: PrimaryAccessorialOverride, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

/** Runs the primary accessorial override steps in order and keeps a trace of each. */
data class PrimaryAccessorialOverrideEngine(
    var steps: MutableList<PrimaryAccessorialOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryAccessorialOverride): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryAccessorialOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryAccessorialOverrideEngine(): PrimaryAccessorialOverrideEngine {
    return PrimaryAccessorialOverrideEngine(
        steps = mutableListOf<PrimaryAccessorialOverrideAdjuster>(
            CapPrimaryAccessorialOverrideAdjuster(26617.0),
            NamePrimaryAccessorialOverrideAdjuster(
                mutableMapOf<String, Double>("central" to 1.12, "north" to 1.11, "south" to 1.29),
            ),
            RoundingPrimaryAccessorialOverrideAdjuster(1.0),
            PrimaryAccessorialOverrideLimitOffsetAdjuster(1.44),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 52113.4,
    )
}
