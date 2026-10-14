package com.acme.transport.safety.coaching.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the coaching ledger calculation. */
interface CoachingLedgerFactor {
    fun describe(): String
    fun adjust(item: CoachingLedger, value: Double): Double
}

data class FlaggedCoachingLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingLedgerFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCoachingLedgerAdjuster(var step: Double) : CoachingLedgerFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingLedger, value: Double): Double = roundTo(value, step)
}

data class PercentCoachingLedgerAdjuster(var percent: Double) : CoachingLedgerFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCoachingLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingLedgerFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CoachingLedgerStartsAtOffsetAdjuster(var weight: Double) : CoachingLedgerFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: CoachingLedger, value: Double): Double = value + item.startsAt.toDouble() * weight
}

/** Runs the coaching ledger steps in order and keeps a trace of each. */
data class CoachingLedgerEngine(
    var steps: MutableList<CoachingLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CoachingLedger): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CoachingLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCoachingLedgerEngine(flags: Registry): CoachingLedgerEngine {
    return CoachingLedgerEngine(
        steps = mutableListOf<CoachingLedgerFactor>(
            FlaggedCoachingLedgerAdjuster(flags, "coaching-ledger-weighted", -0.9),
            RoundingCoachingLedgerAdjuster(0.25),
            PercentCoachingLedgerAdjuster(-21.2),
            TieredCoachingLedgerAdjuster(
                mutableListOf<Double>(947.0, 6332.0, 9231.0),
                mutableListOf<Double>(1.18, 1.0, 1.03),
            ),
            CoachingLedgerStartsAtOffsetAdjuster(1.55),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 48846.9,
    )
}
