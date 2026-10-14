package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual redelivery exception calculation. */
interface ManualRedeliveryExceptionModifier {
    fun describe(): String
    fun adjust(item: ManualRedeliveryException, value: Double): Double
}

data class PercentManualRedeliveryExceptionAdjuster(var percent: Double) : ManualRedeliveryExceptionModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualRedeliveryException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumManualRedeliveryExceptionAdjuster(var minimum: Double) : ManualRedeliveryExceptionModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualRedeliveryException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredManualRedeliveryExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualRedeliveryExceptionModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualRedeliveryException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ManualRedeliveryExceptionCapacityOffsetAdjuster(
    var weight: Double,
) : ManualRedeliveryExceptionModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: ManualRedeliveryException, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class FlaggedManualRedeliveryExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualRedeliveryExceptionModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualRedeliveryException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleManualRedeliveryExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualRedeliveryExceptionModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: ManualRedeliveryException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual redelivery exception steps in order and keeps a trace of each. */
data class ManualRedeliveryExceptionEngine(
    var steps: MutableList<ManualRedeliveryExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualRedeliveryException): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualRedeliveryException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualRedeliveryExceptionEngine(flags: Registry): ManualRedeliveryExceptionEngine {
    return ManualRedeliveryExceptionEngine(
        steps = mutableListOf<ManualRedeliveryExceptionModifier>(
            PercentManualRedeliveryExceptionAdjuster(16.4),
            MinimumManualRedeliveryExceptionAdjuster(35.4),
            TieredManualRedeliveryExceptionAdjuster(
                mutableListOf<Double>(3443.0, 4820.0, 7113.0, 9119.0),
                mutableListOf<Double>(1.2, 1.11, 0.85, 0.91),
            ),
            ManualRedeliveryExceptionCapacityOffsetAdjuster(0.24),
            FlaggedManualRedeliveryExceptionAdjuster(flags, "manual-redelivery-exception-rolling", 7.6),
            LocaleManualRedeliveryExceptionAdjuster(
                mutableMapOf<String, Double>("north" to 1.1, "east" to 0.74),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 6915.9,
    )
}
