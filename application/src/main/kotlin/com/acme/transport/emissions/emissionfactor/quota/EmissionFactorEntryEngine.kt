package com.acme.transport.emissions.emissionfactor.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the emission factor entry calculation. */
interface EmissionFactorEntryModifier {
    fun describe(): String
    fun adjust(item: EmissionFactorEntry, value: Double): Double
}

data class FlaggedEmissionFactorEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorEntryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SourceEmissionFactorEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorEntryModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: EmissionFactorEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingEmissionFactorEntryAdjuster(var step: Double) : EmissionFactorEntryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorEntry, value: Double): Double = roundTo(value, step)
}

data class MinimumEmissionFactorEntryAdjuster(var minimum: Double) : EmissionFactorEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapEmissionFactorEntryAdjuster(var cap: Double) : EmissionFactorEntryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorEntry, value: Double): Double = minOf(value, cap)
}

/** Runs the emission factor entry steps in order and keeps a trace of each. */
data class EmissionFactorEntryEngine(
    var steps: MutableList<EmissionFactorEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EmissionFactorEntry): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EmissionFactorEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEmissionFactorEntryEngine(flags: Registry): EmissionFactorEntryEngine {
    return EmissionFactorEntryEngine(
        steps = mutableListOf<EmissionFactorEntryModifier>(
            FlaggedEmissionFactorEntryAdjuster(flags, "emission-factor-entry-lenient", 5.1),
            SourceEmissionFactorEntryAdjuster(
                mutableMapOf<String, Double>("central" to 1.31, "east" to 0.96, "south" to 1.07),
            ),
            RoundingEmissionFactorEntryAdjuster(5.0),
            MinimumEmissionFactorEntryAdjuster(35.4),
            CapEmissionFactorEntryAdjuster(23768.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 94859.7,
    )
}
