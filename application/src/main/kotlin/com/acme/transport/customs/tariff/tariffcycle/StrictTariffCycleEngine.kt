package com.acme.transport.customs.tariff.tariffcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict tariff cycle calculation. */
interface StrictTariffCycleModifier {
    fun describe(): String
    fun adjust(item: StrictTariffCycle, value: Double): Double
}

data class FlaggedStrictTariffCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictTariffCycleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictTariffCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentStrictTariffCycleAdjuster(var percent: Double) : StrictTariffCycleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictTariffCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingStrictTariffCycleAdjuster(var step: Double) : StrictTariffCycleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictTariffCycle, value: Double): Double = roundTo(value, step)
}

data class StrictTariffCycleStartsAtOffsetAdjuster(var weight: Double) : StrictTariffCycleModifier {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: StrictTariffCycle, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class TitleStrictTariffCycleAdjuster(var factors: MutableMap<String, Double>) : StrictTariffCycleModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictTariffCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredStrictTariffCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictTariffCycleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictTariffCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict tariff cycle steps in order and keeps a trace of each. */
data class StrictTariffCycleEngine(
    var steps: MutableList<StrictTariffCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictTariffCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictTariffCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictTariffCycleEngine(flags: Registry): StrictTariffCycleEngine {
    return StrictTariffCycleEngine(
        steps = mutableListOf<StrictTariffCycleModifier>(
            FlaggedStrictTariffCycleAdjuster(flags, "strict-tariff-cycle-manual", 6.3),
            PercentStrictTariffCycleAdjuster(-12.9),
            RoundingStrictTariffCycleAdjuster(5.0),
            StrictTariffCycleStartsAtOffsetAdjuster(1.26),
            TitleStrictTariffCycleAdjuster(
                mutableMapOf<String, Double>("retail" to 1.25, "online" to 1.35, "central" to 0.89),
            ),
            TieredStrictTariffCycleAdjuster(
                mutableListOf<Double>(3662.0, 6544.0),
                mutableListOf<Double>(0.83, 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 92939.2,
    )
}
