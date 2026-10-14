package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.flags.Registry

/** One step of the manual tariff checkpoint calculation. */
interface ManualTariffCheckpointModifier {
    fun describe(): String
    fun adjust(item: ManualTariffCheckpoint, value: Double): Double
}

data class FlaggedManualTariffCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualTariffCheckpointModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualTariffCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleManualTariffCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualTariffCheckpointModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualTariffCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapManualTariffCheckpointAdjuster(var cap: Double) : ManualTariffCheckpointModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTariffCheckpoint, value: Double): Double = minOf(value, cap)
}

data class MinimumManualTariffCheckpointAdjuster(var minimum: Double) : ManualTariffCheckpointModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualTariffCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredManualTariffCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualTariffCheckpointModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualTariffCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual tariff checkpoint steps in order and keeps a trace of each. */
data class ManualTariffCheckpointEngine(
    var steps: MutableList<ManualTariffCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualTariffCheckpoint): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualTariffCheckpointModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualTariffCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualTariffCheckpointEngine(flags: Registry): ManualTariffCheckpointEngine {
    return ManualTariffCheckpointEngine(
        steps = mutableListOf<ManualTariffCheckpointModifier>(
            FlaggedManualTariffCheckpointAdjuster(flags, "manual-tariff-checkpoint-lenient", -1.8),
            TitleManualTariffCheckpointAdjuster(
                mutableMapOf<String, Double>("north" to 1.14, "central" to 1.33),
            ),
            CapManualTariffCheckpointAdjuster(23527.0),
            MinimumManualTariffCheckpointAdjuster(36.0),
            TieredManualTariffCheckpointAdjuster(
                mutableListOf<Double>(760.0, 7016.0, 7200.0, 8664.0),
                mutableListOf<Double>(1.11, 1.05, 0.94, 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 6053.5,
    )
}
