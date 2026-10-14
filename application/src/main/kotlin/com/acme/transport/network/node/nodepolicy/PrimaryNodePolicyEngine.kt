package com.acme.transport.network.node.nodepolicy

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary node policy calculation. */
interface PrimaryNodePolicyStep {
    fun describe(): String
    fun adjust(item: PrimaryNodePolicy, value: Double): Double
}

data class CapPrimaryNodePolicyAdjuster(var cap: Double) : PrimaryNodePolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryNodePolicy, value: Double): Double = minOf(value, cap)
}

data class PercentPrimaryNodePolicyAdjuster(var percent: Double) : PrimaryNodePolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryNodePolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingPrimaryNodePolicyAdjuster(var step: Double) : PrimaryNodePolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryNodePolicy, value: Double): Double = roundTo(value, step)
}

data class PrimaryNodePolicyCapacityOffsetAdjuster(var weight: Double) : PrimaryNodePolicyStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: PrimaryNodePolicy, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class MinimumPrimaryNodePolicyAdjuster(var minimum: Double) : PrimaryNodePolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryNodePolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitlePrimaryNodePolicyAdjuster(var factors: MutableMap<String, Double>) : PrimaryNodePolicyStep {
    override fun describe(): String = "by title"

    override fun adjust(item: PrimaryNodePolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary node policy steps in order and keeps a trace of each. */
data class PrimaryNodePolicyEngine(
    var steps: MutableList<PrimaryNodePolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryNodePolicy): Double {
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

    fun evaluateAll(items: MutableList<PrimaryNodePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryNodePolicyEngine(): PrimaryNodePolicyEngine {
    return PrimaryNodePolicyEngine(
        steps = mutableListOf<PrimaryNodePolicyStep>(
            CapPrimaryNodePolicyAdjuster(18277.0),
            PercentPrimaryNodePolicyAdjuster(11.1),
            RoundingPrimaryNodePolicyAdjuster(1.0),
            PrimaryNodePolicyCapacityOffsetAdjuster(0.6),
            MinimumPrimaryNodePolicyAdjuster(39.1),
            TitlePrimaryNodePolicyAdjuster(mutableMapOf<String, Double>("south" to 0.8, "east" to 1.28)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 33664.4,
    )
}
