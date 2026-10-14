package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scorecard record calculation. */
interface ScorecardRecordAdjuster {
    fun describe(): String
    fun adjust(item: ScorecardRecord, value: Double): Double
}

data class CapScorecardRecordAdjuster(var cap: Double) : ScorecardRecordAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScorecardRecord, value: Double): Double = minOf(value, cap)
}

data class RoundingScorecardRecordAdjuster(var step: Double) : ScorecardRecordAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardRecord, value: Double): Double = roundTo(value, step)
}

data class PercentScorecardRecordAdjuster(var percent: Double) : ScorecardRecordAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedScorecardRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardRecordAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameScorecardRecordAdjuster(var factors: MutableMap<String, Double>) : ScorecardRecordAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: ScorecardRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredScorecardRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScorecardRecordAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScorecardRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scorecard record steps in order and keeps a trace of each. */
data class ScorecardRecordEngine(
    var steps: MutableList<ScorecardRecordAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScorecardRecord): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardRecordAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScorecardRecordEngine(flags: Registry): ScorecardRecordEngine {
    return ScorecardRecordEngine(
        steps = mutableListOf<ScorecardRecordAdjuster>(
            CapScorecardRecordAdjuster(8926.0),
            RoundingScorecardRecordAdjuster(1.0),
            PercentScorecardRecordAdjuster(-1.7),
            FlaggedScorecardRecordAdjuster(flags, "scorecard-record-default", -2.9),
            NameScorecardRecordAdjuster(mutableMapOf<String, Double>("west" to 0.98)),
            TieredScorecardRecordAdjuster(
                mutableListOf<Double>(5568.0, 6258.0, 8337.0),
                mutableListOf<Double>(1.03, 0.99, 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 47019.4,
    )
}
