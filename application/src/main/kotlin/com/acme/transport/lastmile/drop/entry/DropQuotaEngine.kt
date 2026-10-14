package com.acme.transport.lastmile.drop.entry

import com.acme.transport.kit.flags.Registry

/** One step of the drop quota calculation. */
interface DropQuotaStep {
    fun describe(): String
    fun adjust(item: DropQuota, value: Double): Double
}

data class CapDropQuotaAdjuster(var cap: Double) : DropQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropQuota, value: Double): Double = minOf(value, cap)
}

data class TieredDropQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DropQuotaStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DropQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleDropQuotaAdjuster(var factors: MutableMap<String, Double>) : DropQuotaStep {
    override fun describe(): String = "by title"

    override fun adjust(item: DropQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDropQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DropQuotaStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DropQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the drop quota steps in order and keeps a trace of each. */
data class DropQuotaEngine(
    var steps: MutableList<DropQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DropQuota): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DropQuotaStep = steps[index]
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

    fun evaluateAll(items: MutableList<DropQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDropQuotaEngine(flags: Registry): DropQuotaEngine {
    return DropQuotaEngine(
        steps = mutableListOf<DropQuotaStep>(
            CapDropQuotaAdjuster(15730.0),
            TieredDropQuotaAdjuster(
                mutableListOf<Double>(1990.0, 5984.0, 6823.0),
                mutableListOf<Double>(0.81, 0.95, 1.0),
            ),
            TitleDropQuotaAdjuster(
                mutableMapOf<String, Double>("east" to 0.97, "online" to 0.98, "west" to 0.92),
            ),
            FlaggedDropQuotaAdjuster(flags, "drop-quota-weighted", -3.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 20495.5,
    )
}
