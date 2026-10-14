package com.acme.transport.customs.tariff.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling tariff batch calculation. */
interface RollingTariffBatchModifier {
    fun describe(): String
    fun adjust(item: RollingTariffBatch, value: Double): Double
}

data class CapRollingTariffBatchAdjuster(var cap: Double) : RollingTariffBatchModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingTariffBatch, value: Double): Double = minOf(value, cap)
}

data class PercentRollingTariffBatchAdjuster(var percent: Double) : RollingTariffBatchModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingTariffBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RollingTariffBatchDistanceKmOffsetAdjuster(var weight: Double) : RollingTariffBatchModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: RollingTariffBatch, value: Double): Double = value + item.distanceKm * weight
}

data class FlaggedRollingTariffBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingTariffBatchModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingTariffBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRollingTariffBatchAdjuster(var step: Double) : RollingTariffBatchModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingTariffBatch, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling tariff batch steps in order and keeps a trace of each. */
data class RollingTariffBatchEngine(
    var steps: MutableList<RollingTariffBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingTariffBatch): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingTariffBatchModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RollingTariffBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingTariffBatchEngine(flags: Registry): RollingTariffBatchEngine {
    return RollingTariffBatchEngine(
        steps = mutableListOf<RollingTariffBatchModifier>(
            CapRollingTariffBatchAdjuster(28118.0),
            PercentRollingTariffBatchAdjuster(-9.3),
            RollingTariffBatchDistanceKmOffsetAdjuster(1.24),
            FlaggedRollingTariffBatchAdjuster(flags, "rolling-tariff-batch-tiered", 4.1),
            RoundingRollingTariffBatchAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 59580.2,
    )
}
