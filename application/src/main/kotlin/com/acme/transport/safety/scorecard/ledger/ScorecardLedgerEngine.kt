package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scorecard ledger calculation. */
interface ScorecardLedgerModifier {
    fun describe(): String
    fun adjust(item: ScorecardLedger, value: Double): Double
}

data class RoundingScorecardLedgerAdjuster(var step: Double) : ScorecardLedgerModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardLedger, value: Double): Double = roundTo(value, step)
}

data class FlaggedScorecardLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardLedgerModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScorecardLedgerAdjuster(var percent: Double) : ScorecardLedgerModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CategoryScorecardLedgerAdjuster(var factors: MutableMap<String, Double>) : ScorecardLedgerModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: ScorecardLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumScorecardLedgerAdjuster(var minimum: Double) : ScorecardLedgerModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scorecard ledger steps in order and keeps a trace of each. */
data class ScorecardLedgerEngine(
    var steps: MutableList<ScorecardLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScorecardLedger): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScorecardLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScorecardLedgerEngine(flags: Registry): ScorecardLedgerEngine {
    return ScorecardLedgerEngine(
        steps = mutableListOf<ScorecardLedgerModifier>(
            RoundingScorecardLedgerAdjuster(0.1),
            FlaggedScorecardLedgerAdjuster(flags, "scorecard-ledger-rolling", -0.6),
            PercentScorecardLedgerAdjuster(-8.5),
            CategoryScorecardLedgerAdjuster(mutableMapOf<String, Double>("north" to 1.23, "south" to 1.34)),
            MinimumScorecardLedgerAdjuster(40.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 78339.1,
    )
}
