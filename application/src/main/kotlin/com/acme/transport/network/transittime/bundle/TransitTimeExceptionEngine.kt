package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.flags.Registry

/** One step of the transit time exception calculation. */
interface TransitTimeExceptionAdjuster {
    fun describe(): String
    fun adjust(item: TransitTimeException, value: Double): Double
}

data class PercentTransitTimeExceptionAdjuster(var percent: Double) : TransitTimeExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelTransitTimeExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : TransitTimeExceptionAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: TransitTimeException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTransitTimeExceptionAdjuster(var cap: Double) : TransitTimeExceptionAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeException, value: Double): Double = minOf(value, cap)
}

data class TransitTimeExceptionBatchSizeOffsetAdjuster(var weight: Double) : TransitTimeExceptionAdjuster {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: TransitTimeException, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class FlaggedTransitTimeExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TransitTimeExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TransitTimeException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTransitTimeExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimeExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimeException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the transit time exception steps in order and keeps a trace of each. */
data class TransitTimeExceptionEngine(
    var steps: MutableList<TransitTimeExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TransitTimeException): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TransitTimeExceptionAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TransitTimeException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTransitTimeExceptionEngine(flags: Registry): TransitTimeExceptionEngine {
    return TransitTimeExceptionEngine(
        steps = mutableListOf<TransitTimeExceptionAdjuster>(
            PercentTransitTimeExceptionAdjuster(0.8),
            LabelTransitTimeExceptionAdjuster(mutableMapOf<String, Double>("online" to 1.3, "west" to 1.2)),
            CapTransitTimeExceptionAdjuster(29192.0),
            TransitTimeExceptionBatchSizeOffsetAdjuster(1.16),
            FlaggedTransitTimeExceptionAdjuster(flags, "transit-time-exception-scheduled", -2.2),
            TieredTransitTimeExceptionAdjuster(
                mutableListOf<Double>(2026.0, 6849.0, 9965.0),
                mutableListOf<Double>(1.12, 1.06, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 75105.2,
    )
}
