package com.acme.transport.freightpay.dispute.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual dispute bundle calculation. */
interface ManualDisputeBundleModifier {
    fun describe(): String
    fun adjust(item: ManualDisputeBundle, value: Double): Double
}

data class FlaggedManualDisputeBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDisputeBundleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDisputeBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionManualDisputeBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualDisputeBundleModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: ManualDisputeBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingManualDisputeBundleAdjuster(var step: Double) : ManualDisputeBundleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualDisputeBundle, value: Double): Double = roundTo(value, step)
}

data class PercentManualDisputeBundleAdjuster(var percent: Double) : ManualDisputeBundleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualDisputeBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ManualDisputeBundleAmountOffsetAdjuster(var weight: Double) : ManualDisputeBundleModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: ManualDisputeBundle, value: Double): Double = value + item.amount * weight
}

/** Runs the manual dispute bundle steps in order and keeps a trace of each. */
data class ManualDisputeBundleEngine(
    var steps: MutableList<ManualDisputeBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualDisputeBundle): Double {
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

    fun evaluateAll(items: MutableList<ManualDisputeBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualDisputeBundleEngine(flags: Registry): ManualDisputeBundleEngine {
    return ManualDisputeBundleEngine(
        steps = mutableListOf<ManualDisputeBundleModifier>(
            FlaggedManualDisputeBundleAdjuster(flags, "manual-dispute-bundle-manual", 3.7),
            RegionManualDisputeBundleAdjuster(
                mutableMapOf<String, Double>("north" to 1.18, "online" to 1.15, "retail" to 1.28),
            ),
            RoundingManualDisputeBundleAdjuster(0.05),
            PercentManualDisputeBundleAdjuster(-8.3),
            ManualDisputeBundleAmountOffsetAdjuster(0.67),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 86593.7,
    )
}
