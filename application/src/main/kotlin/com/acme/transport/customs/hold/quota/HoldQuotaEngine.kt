package com.acme.transport.customs.hold.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the hold quota calculation. */
interface HoldQuotaAdjuster {
    fun describe(): String
    fun adjust(item: HoldQuota, value: Double): Double
}

data class HoldQuotaMarginOffsetAdjuster(var weight: Double) : HoldQuotaAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: HoldQuota, value: Double): Double = value + item.margin * weight
}

data class TieredHoldQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HoldQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HoldQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentHoldQuotaAdjuster(var percent: Double) : HoldQuotaAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HoldQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumHoldQuotaAdjuster(var minimum: Double) : HoldQuotaAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HoldQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedHoldQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldQuotaAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameHoldQuotaAdjuster(var factors: MutableMap<String, Double>) : HoldQuotaAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: HoldQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the hold quota steps in order and keeps a trace of each. */
data class HoldQuotaEngine(
    var steps: MutableList<HoldQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: HoldQuota): Double {
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

    fun evaluateAll(items: MutableList<HoldQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultHoldQuotaEngine(flags: Registry): HoldQuotaEngine {
    return HoldQuotaEngine(
        steps = mutableListOf<HoldQuotaAdjuster>(
            HoldQuotaMarginOffsetAdjuster(0.09),
            TieredHoldQuotaAdjuster(
                mutableListOf<Double>(721.0, 881.0, 5076.0),
                mutableListOf<Double>(0.95, 1.17, 1.02),
            ),
            PercentHoldQuotaAdjuster(-17.2),
            MinimumHoldQuotaAdjuster(49.1),
            FlaggedHoldQuotaAdjuster(flags, "hold-quota-seasonal", -3.3),
            NameHoldQuotaAdjuster(mutableMapOf<String, Double>("west" to 1.13, "retail" to 1.09)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 74703.6,
    )
}
