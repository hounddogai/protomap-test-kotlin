package com.acme.transport.customs.tariff.batch

import com.acme.transport.kit.flags.Registry

/** One step of the strict tariff batch calculation. */
interface StrictTariffBatchAdjuster {
    fun describe(): String
    fun adjust(item: StrictTariffBatch, value: Double): Double
}

data class StrictTariffBatchAttemptsOffsetAdjuster(var weight: Double) : StrictTariffBatchAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: StrictTariffBatch, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class MinimumStrictTariffBatchAdjuster(var minimum: Double) : StrictTariffBatchAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictTariffBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedStrictTariffBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictTariffBatchAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictTariffBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredStrictTariffBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictTariffBatchAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictTariffBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict tariff batch steps in order and keeps a trace of each. */
data class StrictTariffBatchEngine(
    var steps: MutableList<StrictTariffBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictTariffBatch): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTariffBatchAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTariffBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictTariffBatchEngine(flags: Registry): StrictTariffBatchEngine {
    return StrictTariffBatchEngine(
        steps = mutableListOf<StrictTariffBatchAdjuster>(
            StrictTariffBatchAttemptsOffsetAdjuster(1.67),
            MinimumStrictTariffBatchAdjuster(29.2),
            FlaggedStrictTariffBatchAdjuster(flags, "strict-tariff-batch-manual", 5.7),
            TieredStrictTariffBatchAdjuster(
                mutableListOf<Double>(1789.0, 6941.0, 8691.0),
                mutableListOf<Double>(1.0, 1.08, 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 84838.8,
    )
}
