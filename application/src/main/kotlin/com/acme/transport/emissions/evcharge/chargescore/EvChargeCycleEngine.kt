package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the ev charge cycle calculation. */
interface EvChargeCycleStep {
    fun describe(): String
    fun adjust(item: EvChargeCycle, value: Double): Double
}

data class RoundingEvChargeCycleAdjuster(var step: Double) : EvChargeCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeCycle, value: Double): Double = roundTo(value, step)
}

data class PercentEvChargeCycleAdjuster(var percent: Double) : EvChargeCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargeCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UnitEvChargeCycleAdjuster(var factors: MutableMap<String, Double>) : EvChargeCycleStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: EvChargeCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapEvChargeCycleAdjuster(var cap: Double) : EvChargeCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeCycle, value: Double): Double = minOf(value, cap)
}

data class FlaggedEvChargeCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeCycleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredEvChargeCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the ev charge cycle steps in order and keeps a trace of each. */
data class EvChargeCycleEngine(
    var steps: MutableList<EvChargeCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EvChargeCycle): Double {
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

    fun evaluateAll(items: MutableList<EvChargeCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEvChargeCycleEngine(flags: Registry): EvChargeCycleEngine {
    return EvChargeCycleEngine(
        steps = mutableListOf<EvChargeCycleStep>(
            RoundingEvChargeCycleAdjuster(0.5),
            PercentEvChargeCycleAdjuster(14.4),
            UnitEvChargeCycleAdjuster(
                mutableMapOf<String, Double>("west" to 0.82, "north" to 1.39, "online" to 1.19),
            ),
            CapEvChargeCycleAdjuster(15274.0),
            FlaggedEvChargeCycleAdjuster(flags, "ev-charge-cycle-lenient", 7.2),
            TieredEvChargeCycleAdjuster(
                mutableListOf<Double>(1425.0, 3552.0, 7974.0),
                mutableListOf<Double>(1.03, 1.05, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 81206.9,
    )
}
