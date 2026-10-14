package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the lane plan ledger calculation. */
interface LanePlanLedgerStep {
    fun describe(): String
    fun adjust(item: LanePlanLedger, value: Double): Double
}

data class TieredLanePlanLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanLedgerStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLanePlanLedgerAdjuster(var step: Double) : LanePlanLedgerStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanLedger, value: Double): Double = roundTo(value, step)
}

data class LanePlanLedgerGraceDaysOffsetAdjuster(var weight: Double) : LanePlanLedgerStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: LanePlanLedger, value: Double): Double = value + item.graceDays.toDouble() * weight
}

/** Runs the lane plan ledger steps in order and keeps a trace of each. */
data class LanePlanLedgerEngine(
    var steps: MutableList<LanePlanLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LanePlanLedger): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LanePlanLedgerStep = steps[index]
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

    fun evaluateAll(items: MutableList<LanePlanLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLanePlanLedgerEngine(): LanePlanLedgerEngine {
    return LanePlanLedgerEngine(
        steps = mutableListOf<LanePlanLedgerStep>(
            TieredLanePlanLedgerAdjuster(
                mutableListOf<Double>(3268.0, 9835.0, 9917.0),
                mutableListOf<Double>(1.02, 1.19, 1.03),
            ),
            RoundingLanePlanLedgerAdjuster(0.1),
            LanePlanLedgerGraceDaysOffsetAdjuster(1.97),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 3339.7,
    )
}
