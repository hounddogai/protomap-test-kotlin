package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the drop digest calculation. */
interface DropDigestModifier {
    fun describe(): String
    fun adjust(item: DropDigest, value: Double): Double
}

data class CapDropDigestAdjuster(var cap: Double) : DropDigestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropDigest, value: Double): Double = minOf(value, cap)
}

data class MinimumDropDigestAdjuster(var minimum: Double) : DropDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DropDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDropDigestAdjuster(var step: Double) : DropDigestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DropDigest, value: Double): Double = roundTo(value, step)
}

/** Runs the drop digest steps in order and keeps a trace of each. */
data class DropDigestEngine(
    var steps: MutableList<DropDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DropDigest): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DropDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDropDigestEngine(): DropDigestEngine {
    return DropDigestEngine(
        steps = mutableListOf<DropDigestModifier>(
            CapDropDigestAdjuster(28727.0),
            MinimumDropDigestAdjuster(21.7),
            RoundingDropDigestAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 92935.8,
    )
}
