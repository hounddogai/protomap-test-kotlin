package com.acme.transport.crewing.roster.claim

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the roster claim calculation. */
interface RosterClaimStep {
    fun describe(): String
    fun adjust(item: RosterClaim, value: Double): Double
}

data class TieredRosterClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterClaimStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RosterClaimAmountOffsetAdjuster(var weight: Double) : RosterClaimStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: RosterClaim, value: Double): Double = value + item.amount * weight
}

data class PercentRosterClaimAdjuster(var percent: Double) : RosterClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the roster claim steps in order and keeps a trace of each. */
data class RosterClaimEngine(
    var steps: MutableList<RosterClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RosterClaim): Double {
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

    fun evaluateAll(items: MutableList<RosterClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRosterClaimEngine(): RosterClaimEngine {
    return RosterClaimEngine(
        steps = mutableListOf<RosterClaimStep>(
            TieredRosterClaimAdjuster(mutableListOf<Double>(5802.0, 8458.0), mutableListOf<Double>(0.83, 1.07)),
            RosterClaimAmountOffsetAdjuster(0.73),
            PercentRosterClaimAdjuster(-7.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 37622.5,
    )
}
