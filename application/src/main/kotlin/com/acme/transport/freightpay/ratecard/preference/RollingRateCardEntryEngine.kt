package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling rate card entry calculation. */
interface RollingRateCardEntryStep {
    fun describe(): String
    fun adjust(item: RollingRateCardEntry, value: Double): Double
}

data class TieredRollingRateCardEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingRateCardEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingRateCardEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRollingRateCardEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingRateCardEntryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingRateCardEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryRollingRateCardEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingRateCardEntryStep {
    override fun describe(): String = "by category"

    override fun adjust(item: RollingRateCardEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rolling rate card entry steps in order and keeps a trace of each. */
data class RollingRateCardEntryEngine(
    var steps: MutableList<RollingRateCardEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingRateCardEntry): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingRateCardEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingRateCardEntryEngine(flags: Registry): RollingRateCardEntryEngine {
    return RollingRateCardEntryEngine(
        steps = mutableListOf<RollingRateCardEntryStep>(
            TieredRollingRateCardEntryAdjuster(
                mutableListOf<Double>(1284.0, 3212.0, 5127.0, 6606.0),
                mutableListOf<Double>(0.98, 1.17, 1.17, 1.09),
            ),
            FlaggedRollingRateCardEntryAdjuster(flags, "rolling-rate-card-entry-strict", -2.9),
            CategoryRollingRateCardEntryAdjuster(
                mutableMapOf<String, Double>("central" to 0.7, "east" to 0.77, "west" to 0.7),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 98056.7,
    )
}
