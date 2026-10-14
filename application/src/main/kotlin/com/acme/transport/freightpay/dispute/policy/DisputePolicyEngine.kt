package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dispute policy calculation. */
interface DisputePolicyStep {
    fun describe(): String
    fun adjust(item: DisputePolicy, value: Double): Double
}

data class CapDisputePolicyAdjuster(var cap: Double) : DisputePolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DisputePolicy, value: Double): Double = minOf(value, cap)
}

data class PercentDisputePolicyAdjuster(var percent: Double) : DisputePolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DisputePolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDisputePolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputePolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputePolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDisputePolicyAdjuster(var step: Double) : DisputePolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DisputePolicy, value: Double): Double = roundTo(value, step)
}

/** Runs the dispute policy steps in order and keeps a trace of each. */
data class DisputePolicyEngine(
    var steps: MutableList<DisputePolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DisputePolicy): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DisputePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDisputePolicyEngine(): DisputePolicyEngine {
    return DisputePolicyEngine(
        steps = mutableListOf<DisputePolicyStep>(
            CapDisputePolicyAdjuster(16126.0),
            PercentDisputePolicyAdjuster(-9.1),
            TieredDisputePolicyAdjuster(
                mutableListOf<Double>(7785.0, 9051.0),
                mutableListOf<Double>(1.19, 1.15),
            ),
            RoundingDisputePolicyAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 20548.9,
    )
}
