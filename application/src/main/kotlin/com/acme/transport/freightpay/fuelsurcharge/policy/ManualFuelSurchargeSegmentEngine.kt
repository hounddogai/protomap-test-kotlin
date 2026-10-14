package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual fuel surcharge segment calculation. */
interface ManualFuelSurchargeSegmentModifier {
    fun describe(): String
    fun adjust(item: ManualFuelSurchargeSegment, value: Double): Double
}

data class ManualFuelSurchargeSegmentLoadKgOffsetAdjuster(
    var weight: Double,
) : ManualFuelSurchargeSegmentModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: ManualFuelSurchargeSegment, value: Double): Double = value + item.loadKg * weight
}

data class FlaggedManualFuelSurchargeSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualFuelSurchargeSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualFuelSurchargeSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentManualFuelSurchargeSegmentAdjuster(var percent: Double) : ManualFuelSurchargeSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualFuelSurchargeSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumManualFuelSurchargeSegmentAdjuster(var minimum: Double) : ManualFuelSurchargeSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualFuelSurchargeSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ChannelManualFuelSurchargeSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualFuelSurchargeSegmentModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: ManualFuelSurchargeSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual fuel surcharge segment steps in order and keeps a trace of each. */
data class ManualFuelSurchargeSegmentEngine(
    var steps: MutableList<ManualFuelSurchargeSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualFuelSurchargeSegment): Double {
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

    fun evaluateAll(items: MutableList<ManualFuelSurchargeSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualFuelSurchargeSegmentEngine(flags: Registry): ManualFuelSurchargeSegmentEngine {
    return ManualFuelSurchargeSegmentEngine(
        steps = mutableListOf<ManualFuelSurchargeSegmentModifier>(
            ManualFuelSurchargeSegmentLoadKgOffsetAdjuster(1.57),
            FlaggedManualFuelSurchargeSegmentAdjuster(flags, "manual-fuel-surcharge-segment-strict", 7.4),
            PercentManualFuelSurchargeSegmentAdjuster(-10.3),
            MinimumManualFuelSurchargeSegmentAdjuster(39.7),
            ChannelManualFuelSurchargeSegmentAdjuster(mutableMapOf<String, Double>("central" to 1.25)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 22237.4,
    )
}
