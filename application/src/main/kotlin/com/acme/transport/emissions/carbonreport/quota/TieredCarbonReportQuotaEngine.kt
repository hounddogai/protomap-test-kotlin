package com.acme.transport.emissions.carbonreport.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered carbon report quota calculation. */
interface TieredCarbonReportQuotaFactor {
    fun describe(): String
    fun adjust(item: TieredCarbonReportQuota, value: Double): Double
}

data class TieredCarbonReportQuotaAmountOffsetAdjuster(var weight: Double) : TieredCarbonReportQuotaFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: TieredCarbonReportQuota, value: Double): Double = value + item.amount * weight
}

data class FlaggedTieredCarbonReportQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredCarbonReportQuotaFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredCarbonReportQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTieredCarbonReportQuotaAdjuster(var minimum: Double) : TieredCarbonReportQuotaFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredCarbonReportQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered carbon report quota steps in order and keeps a trace of each. */
data class TieredCarbonReportQuotaEngine(
    var steps: MutableList<TieredCarbonReportQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredCarbonReportQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredCarbonReportQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredCarbonReportQuotaEngine(flags: Registry): TieredCarbonReportQuotaEngine {
    return TieredCarbonReportQuotaEngine(
        steps = mutableListOf<TieredCarbonReportQuotaFactor>(
            TieredCarbonReportQuotaAmountOffsetAdjuster(0.95),
            FlaggedTieredCarbonReportQuotaAdjuster(flags, "tiered-carbon-report-quota-strict", -0.9),
            MinimumTieredCarbonReportQuotaAdjuster(16.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 66103.5,
    )
}
