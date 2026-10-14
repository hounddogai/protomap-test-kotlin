package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.flags.Registry

/** One step of the parcel quota calculation. */
interface ParcelQuotaStep {
    fun describe(): String
    fun adjust(item: ParcelQuota, value: Double): Double
}

data class TitleParcelQuotaAdjuster(var factors: MutableMap<String, Double>) : ParcelQuotaStep {
    override fun describe(): String = "by title"

    override fun adjust(item: ParcelQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapParcelQuotaAdjuster(var cap: Double) : ParcelQuotaStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ParcelQuota, value: Double): Double = minOf(value, cap)
}

data class FlaggedParcelQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ParcelQuotaStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ParcelQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentParcelQuotaAdjuster(var percent: Double) : ParcelQuotaStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the parcel quota steps in order and keeps a trace of each. */
data class ParcelQuotaEngine(
    var steps: MutableList<ParcelQuotaStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ParcelQuota): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelQuotaStep = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultParcelQuotaEngine(flags: Registry): ParcelQuotaEngine {
    return ParcelQuotaEngine(
        steps = mutableListOf<ParcelQuotaStep>(
            TitleParcelQuotaAdjuster(mutableMapOf<String, Double>("online" to 1.11, "south" to 0.93)),
            CapParcelQuotaAdjuster(27336.0),
            FlaggedParcelQuotaAdjuster(flags, "parcel-quota-manual", 2.7),
            PercentParcelQuotaAdjuster(4.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 13388.4,
    )
}
