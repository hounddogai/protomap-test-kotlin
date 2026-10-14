package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the carbon report ledger calculation. */
interface CarbonReportLedgerFactor {
    fun describe(): String
    fun adjust(item: CarbonReportLedger, value: Double): Double
}

data class PercentCarbonReportLedgerAdjuster(var percent: Double) : CarbonReportLedgerFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionCarbonReportLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : CarbonReportLedgerFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: CarbonReportLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCarbonReportLedgerAdjuster(var step: Double) : CarbonReportLedgerFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CarbonReportLedger, value: Double): Double = roundTo(value, step)
}

data class MinimumCarbonReportLedgerAdjuster(var minimum: Double) : CarbonReportLedgerFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCarbonReportLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CarbonReportLedgerFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CarbonReportLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCarbonReportLedgerAdjuster(var cap: Double) : CarbonReportLedgerFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportLedger, value: Double): Double = minOf(value, cap)
}

/** Runs the carbon report ledger steps in order and keeps a trace of each. */
data class CarbonReportLedgerEngine(
    var steps: MutableList<CarbonReportLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CarbonReportLedger): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CarbonReportLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCarbonReportLedgerEngine(flags: Registry): CarbonReportLedgerEngine {
    return CarbonReportLedgerEngine(
        steps = mutableListOf<CarbonReportLedgerFactor>(
            PercentCarbonReportLedgerAdjuster(12.4),
            RegionCarbonReportLedgerAdjuster(mutableMapOf<String, Double>("online" to 0.93, "central" to 0.93)),
            RoundingCarbonReportLedgerAdjuster(0.1),
            MinimumCarbonReportLedgerAdjuster(42.0),
            FlaggedCarbonReportLedgerAdjuster(flags, "carbon-report-ledger-global", 7.5),
            CapCarbonReportLedgerAdjuster(24208.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 6314.7,
    )
}
