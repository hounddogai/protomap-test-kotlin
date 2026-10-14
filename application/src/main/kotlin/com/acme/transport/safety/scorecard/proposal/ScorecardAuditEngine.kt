package com.acme.transport.safety.scorecard.proposal

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scorecard audit calculation. */
interface ScorecardAuditStep {
    fun describe(): String
    fun adjust(item: ScorecardAudit, value: Double): Double
}

data class PercentScorecardAuditAdjuster(var percent: Double) : ScorecardAuditStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LocaleScorecardAuditAdjuster(var factors: MutableMap<String, Double>) : ScorecardAuditStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: ScorecardAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumScorecardAuditAdjuster(var minimum: Double) : ScorecardAuditStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScorecardAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingScorecardAuditAdjuster(var step: Double) : ScorecardAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardAudit, value: Double): Double = roundTo(value, step)
}

data class CapScorecardAuditAdjuster(var cap: Double) : ScorecardAuditStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScorecardAudit, value: Double): Double = minOf(value, cap)
}

/** Runs the scorecard audit steps in order and keeps a trace of each. */
data class ScorecardAuditEngine(
    var steps: MutableList<ScorecardAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScorecardAudit): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScorecardAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScorecardAuditEngine(): ScorecardAuditEngine {
    return ScorecardAuditEngine(
        steps = mutableListOf<ScorecardAuditStep>(
            PercentScorecardAuditAdjuster(-1.8),
            LocaleScorecardAuditAdjuster(mutableMapOf<String, Double>("west" to 0.96)),
            MinimumScorecardAuditAdjuster(30.5),
            RoundingScorecardAuditAdjuster(0.5),
            CapScorecardAuditAdjuster(43022.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 49818.6,
    )
}
