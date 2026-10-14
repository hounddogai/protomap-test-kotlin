package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual coaching ledger calculation. */
interface ManualCoachingLedgerFactor {
    fun describe(): String
    fun adjust(item: ManualCoachingLedger, value: Double): Double
}

data class FlaggedManualCoachingLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualCoachingLedgerFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualCoachingLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ManualCoachingLedgerDurationMinutesOffsetAdjuster(var weight: Double) : ManualCoachingLedgerFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: ManualCoachingLedger, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class TieredManualCoachingLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualCoachingLedgerFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualCoachingLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingManualCoachingLedgerAdjuster(var step: Double) : ManualCoachingLedgerFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualCoachingLedger, value: Double): Double = roundTo(value, step)
}

/** Runs the manual coaching ledger steps in order and keeps a trace of each. */
data class ManualCoachingLedgerEngine(
    var steps: MutableList<ManualCoachingLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualCoachingLedger): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualCoachingLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualCoachingLedgerEngine(flags: Registry): ManualCoachingLedgerEngine {
    return ManualCoachingLedgerEngine(
        steps = mutableListOf<ManualCoachingLedgerFactor>(
            FlaggedManualCoachingLedgerAdjuster(flags, "manual-coaching-ledger-rolling", -3.5),
            ManualCoachingLedgerDurationMinutesOffsetAdjuster(0.54),
            TieredManualCoachingLedgerAdjuster(
                mutableListOf<Double>(2885.0, 4681.0, 7865.0),
                mutableListOf<Double>(1.16, 0.92, 1.07),
            ),
            RoundingManualCoachingLedgerAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 64548.6,
    )
}
