package com.acme.transport.linehaul.tender.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted tender exception calculation. */
interface WeightedTenderExceptionAdjuster {
    fun describe(): String
    fun adjust(item: WeightedTenderException, value: Double): Double
}

data class MinimumWeightedTenderExceptionAdjuster(var minimum: Double) : WeightedTenderExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedTenderException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleWeightedTenderExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedTenderExceptionAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: WeightedTenderException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredWeightedTenderExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedTenderExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedTenderException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentWeightedTenderExceptionAdjuster(var percent: Double) : WeightedTenderExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedTenderException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedWeightedTenderExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedTenderExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedTenderException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapWeightedTenderExceptionAdjuster(var cap: Double) : WeightedTenderExceptionAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedTenderException, value: Double): Double = minOf(value, cap)
}

/** Runs the weighted tender exception steps in order and keeps a trace of each. */
data class WeightedTenderExceptionEngine(
    var steps: MutableList<WeightedTenderExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedTenderException): Double {
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

    fun evaluateAll(items: MutableList<WeightedTenderException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedTenderExceptionEngine(flags: Registry): WeightedTenderExceptionEngine {
    return WeightedTenderExceptionEngine(
        steps = mutableListOf<WeightedTenderExceptionAdjuster>(
            MinimumWeightedTenderExceptionAdjuster(33.0),
            LocaleWeightedTenderExceptionAdjuster(
                mutableMapOf<String, Double>("retail" to 1.13, "online" to 1.3, "west" to 1.39),
            ),
            TieredWeightedTenderExceptionAdjuster(
                mutableListOf<Double>(521.0, 1615.0),
                mutableListOf<Double>(0.95, 0.85),
            ),
            PercentWeightedTenderExceptionAdjuster(16.1),
            FlaggedWeightedTenderExceptionAdjuster(flags, "weighted-tender-exception-default", 0.5),
            CapWeightedTenderExceptionAdjuster(38062.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 11984.5,
    )
}
