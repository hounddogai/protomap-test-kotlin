package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional scorecard audit calculation. */
interface RegionalScorecardAuditStep {
    fun describe(): String
    fun adjust(item: RegionalScorecardAudit, value: Double): Double
}

data class FlaggedRegionalScorecardAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalScorecardAuditStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalScorecardAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalScorecardAuditAdjuster(var step: Double) : RegionalScorecardAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalScorecardAudit, value: Double): Double = roundTo(value, step)
}

data class NameRegionalScorecardAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalScorecardAuditStep {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalScorecardAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRegionalScorecardAuditAdjuster(var cap: Double) : RegionalScorecardAuditStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalScorecardAudit, value: Double): Double = minOf(value, cap)
}

/** Runs the regional scorecard audit steps in order and keeps a trace of each. */
data class RegionalScorecardAuditEngine(
    var steps: MutableList<RegionalScorecardAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalScorecardAudit): Double {
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

    fun evaluateAll(items: MutableList<RegionalScorecardAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalScorecardAuditEngine(flags: Registry): RegionalScorecardAuditEngine {
    return RegionalScorecardAuditEngine(
        steps = mutableListOf<RegionalScorecardAuditStep>(
            FlaggedRegionalScorecardAuditAdjuster(flags, "regional-scorecard-audit-adaptive", 8.0),
            RoundingRegionalScorecardAuditAdjuster(0.5),
            NameRegionalScorecardAuditAdjuster(
                mutableMapOf<String, Double>("west" to 1.37, "south" to 0.86, "east" to 1.19),
            ),
            CapRegionalScorecardAuditAdjuster(32900.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 37258.8,
    )
}
