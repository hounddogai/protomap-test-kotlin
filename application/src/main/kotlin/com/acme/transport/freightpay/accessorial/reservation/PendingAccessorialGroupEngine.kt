package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending accessorial group calculation. */
interface PendingAccessorialGroupModifier {
    fun describe(): String
    fun adjust(item: PendingAccessorialGroup, value: Double): Double
}

data class TieredPendingAccessorialGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingAccessorialGroupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingAccessorialGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PendingAccessorialGroupCostOffsetAdjuster(var weight: Double) : PendingAccessorialGroupModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: PendingAccessorialGroup, value: Double): Double = value + item.cost * weight
}

data class PercentPendingAccessorialGroupAdjuster(var percent: Double) : PendingAccessorialGroupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingAccessorialGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingAccessorialGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingAccessorialGroupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingAccessorialGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending accessorial group steps in order and keeps a trace of each. */
data class PendingAccessorialGroupEngine(
    var steps: MutableList<PendingAccessorialGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingAccessorialGroup): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingAccessorialGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingAccessorialGroupEngine(flags: Registry): PendingAccessorialGroupEngine {
    return PendingAccessorialGroupEngine(
        steps = mutableListOf<PendingAccessorialGroupModifier>(
            TieredPendingAccessorialGroupAdjuster(
                mutableListOf<Double>(2984.0, 8421.0),
                mutableListOf<Double>(1.2, 0.81),
            ),
            PendingAccessorialGroupCostOffsetAdjuster(1.22),
            PercentPendingAccessorialGroupAdjuster(-1.4),
            FlaggedPendingAccessorialGroupAdjuster(flags, "pending-accessorial-group-strict", -2.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 41109.3,
    )
}
