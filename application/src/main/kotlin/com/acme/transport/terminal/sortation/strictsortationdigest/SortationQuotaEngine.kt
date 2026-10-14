package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.flags.Registry

/** One step of the sortation quota calculation. */
interface SortationQuotaFactor {
    fun describe(): String
    fun adjust(item: SortationQuota, value: Double): Double
}

data class FlaggedSortationQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationQuotaFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSortationQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SortationQuotaFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SortationQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentSortationQuotaAdjuster(var percent: Double) : SortationQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SortationQuotaRatioOffsetAdjuster(var weight: Double) : SortationQuotaFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: SortationQuota, value: Double): Double = value + item.ratio * weight
}

/** Runs the sortation quota steps in order and keeps a trace of each. */
data class SortationQuotaEngine(
    var steps: MutableList<SortationQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SortationQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SortationQuotaFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SortationQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSortationQuotaEngine(flags: Registry): SortationQuotaEngine {
    return SortationQuotaEngine(
        steps = mutableListOf<SortationQuotaFactor>(
            FlaggedSortationQuotaAdjuster(flags, "sortation-quota-lenient", 0.6),
            TieredSortationQuotaAdjuster(
                mutableListOf<Double>(1591.0, 3682.0, 6200.0),
                mutableListOf<Double>(0.94, 0.99, 0.88),
            ),
            PercentSortationQuotaAdjuster(10.0),
            SortationQuotaRatioOffsetAdjuster(1.65),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 40632.3,
    )
}
