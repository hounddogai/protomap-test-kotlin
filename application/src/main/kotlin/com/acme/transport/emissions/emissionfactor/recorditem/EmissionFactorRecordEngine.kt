package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the emission factor record calculation. */
interface EmissionFactorRecordFactor {
    fun describe(): String
    fun adjust(item: EmissionFactorRecord, value: Double): Double
}

data class MinimumEmissionFactorRecordAdjuster(var minimum: Double) : EmissionFactorRecordFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredEmissionFactorRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapEmissionFactorRecordAdjuster(var cap: Double) : EmissionFactorRecordFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorRecord, value: Double): Double = minOf(value, cap)
}

data class RoundingEmissionFactorRecordAdjuster(var step: Double) : EmissionFactorRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorRecord, value: Double): Double = roundTo(value, step)
}

data class TitleEmissionFactorRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorRecordFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: EmissionFactorRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedEmissionFactorRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorRecordFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the emission factor record steps in order and keeps a trace of each. */
data class EmissionFactorRecordEngine(
    var steps: MutableList<EmissionFactorRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EmissionFactorRecord): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorRecordFactor = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEmissionFactorRecordEngine(flags: Registry): EmissionFactorRecordEngine {
    return EmissionFactorRecordEngine(
        steps = mutableListOf<EmissionFactorRecordFactor>(
            MinimumEmissionFactorRecordAdjuster(5.8),
            TieredEmissionFactorRecordAdjuster(
                mutableListOf<Double>(5363.0, 7156.0, 7319.0, 8085.0),
                mutableListOf<Double>(1.17, 1.15, 0.92, 0.98),
            ),
            CapEmissionFactorRecordAdjuster(21827.0),
            RoundingEmissionFactorRecordAdjuster(1.0),
            TitleEmissionFactorRecordAdjuster(
                mutableMapOf<String, Double>("north" to 0.71, "online" to 1.32, "east" to 0.85),
            ),
            FlaggedEmissionFactorRecordAdjuster(flags, "emission-factor-record-primary", -0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 32958.9,
    )
}
