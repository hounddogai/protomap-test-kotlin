package com.acme.transport.customs.declaration.policy

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary declaration override calculation. */
interface PrimaryDeclarationOverrideModifier {
    fun describe(): String
    fun adjust(item: PrimaryDeclarationOverride, value: Double): Double
}

data class PercentPrimaryDeclarationOverrideAdjuster(var percent: Double) : PrimaryDeclarationOverrideModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryDeclarationOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPrimaryDeclarationOverrideAdjuster(var minimum: Double) : PrimaryDeclarationOverrideModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryDeclarationOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPrimaryDeclarationOverrideAdjuster(var cap: Double) : PrimaryDeclarationOverrideModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryDeclarationOverride, value: Double): Double = minOf(value, cap)
}

data class SegmentPrimaryDeclarationOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryDeclarationOverrideModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: PrimaryDeclarationOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary declaration override steps in order and keeps a trace of each. */
data class PrimaryDeclarationOverrideEngine(
    var steps: MutableList<PrimaryDeclarationOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryDeclarationOverride): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryDeclarationOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryDeclarationOverrideEngine(): PrimaryDeclarationOverrideEngine {
    return PrimaryDeclarationOverrideEngine(
        steps = mutableListOf<PrimaryDeclarationOverrideModifier>(
            PercentPrimaryDeclarationOverrideAdjuster(20.9),
            MinimumPrimaryDeclarationOverrideAdjuster(32.1),
            CapPrimaryDeclarationOverrideAdjuster(22205.0),
            SegmentPrimaryDeclarationOverrideAdjuster(
                mutableMapOf<String, Double>("north" to 1.26, "east" to 0.83, "online" to 0.8),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 73940.0,
    )
}
