package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.kit.flags.Registry

/** One step of the manual duty period ledger calculation. */
interface ManualDutyPeriodLedgerStep {
    fun describe(): String
    fun adjust(item: ManualDutyPeriodLedger, value: Double): Double
}

data class MinimumManualDutyPeriodLedgerAdjuster(var minimum: Double) : ManualDutyPeriodLedgerStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDutyPeriodLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LaneManualDutyPeriodLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualDutyPeriodLedgerStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: ManualDutyPeriodLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ManualDutyPeriodLedgerStartsAtOffsetAdjuster(var weight: Double) : ManualDutyPeriodLedgerStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: ManualDutyPeriodLedger, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class FlaggedManualDutyPeriodLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDutyPeriodLedgerStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDutyPeriodLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredManualDutyPeriodLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualDutyPeriodLedgerStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualDutyPeriodLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapManualDutyPeriodLedgerAdjuster(var cap: Double) : ManualDutyPeriodLedgerStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualDutyPeriodLedger, value: Double): Double = minOf(value, cap)
}

/** Runs the manual duty period ledger steps in order and keeps a trace of each. */
data class ManualDutyPeriodLedgerEngine(
    var steps: MutableList<ManualDutyPeriodLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualDutyPeriodLedger): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualDutyPeriodLedgerStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualDutyPeriodLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualDutyPeriodLedgerEngine(flags: Registry): ManualDutyPeriodLedgerEngine {
    return ManualDutyPeriodLedgerEngine(
        steps = mutableListOf<ManualDutyPeriodLedgerStep>(
            MinimumManualDutyPeriodLedgerAdjuster(47.3),
            LaneManualDutyPeriodLedgerAdjuster(mutableMapOf<String, Double>("north" to 0.78, "west" to 1.19)),
            ManualDutyPeriodLedgerStartsAtOffsetAdjuster(1.66),
            FlaggedManualDutyPeriodLedgerAdjuster(flags, "manual-duty-period-ledger-legacy", 3.0),
            TieredManualDutyPeriodLedgerAdjuster(
                mutableListOf<Double>(1839.0, 6797.0, 7737.0, 9710.0),
                mutableListOf<Double>(0.95, 1.17, 1.11, 0.86),
            ),
            CapManualDutyPeriodLedgerAdjuster(28690.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 63021.3,
    )
}
