package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled duty period ledger calculation. */
interface ScheduledDutyPeriodLedgerFactor {
    fun describe(): String
    fun adjust(item: ScheduledDutyPeriodLedger, value: Double): Double
}

data class RoundingScheduledDutyPeriodLedgerAdjuster(var step: Double) : ScheduledDutyPeriodLedgerFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledDutyPeriodLedger, value: Double): Double = roundTo(value, step)
}

data class PercentScheduledDutyPeriodLedgerAdjuster(var percent: Double) : ScheduledDutyPeriodLedgerFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledDutyPeriodLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ScheduledDutyPeriodLedgerScoreOffsetAdjuster(var weight: Double) : ScheduledDutyPeriodLedgerFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: ScheduledDutyPeriodLedger, value: Double): Double = value + item.score * weight
}

data class CapScheduledDutyPeriodLedgerAdjuster(var cap: Double) : ScheduledDutyPeriodLedgerFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledDutyPeriodLedger, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled duty period ledger steps in order and keeps a trace of each. */
data class ScheduledDutyPeriodLedgerEngine(
    var steps: MutableList<ScheduledDutyPeriodLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledDutyPeriodLedger): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDutyPeriodLedgerFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledDutyPeriodLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledDutyPeriodLedgerEngine(): ScheduledDutyPeriodLedgerEngine {
    return ScheduledDutyPeriodLedgerEngine(
        steps = mutableListOf<ScheduledDutyPeriodLedgerFactor>(
            RoundingScheduledDutyPeriodLedgerAdjuster(0.5),
            PercentScheduledDutyPeriodLedgerAdjuster(-11.6),
            ScheduledDutyPeriodLedgerScoreOffsetAdjuster(0.28),
            CapScheduledDutyPeriodLedgerAdjuster(37851.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 16077.0,
    )
}
