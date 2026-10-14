package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered ev charge plan calculation. */
interface TieredEvChargePlanAdjuster {
    fun describe(): String
    fun adjust(item: TieredEvChargePlan, value: Double): Double
}

data class TieredTieredEvChargePlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredEvChargePlanAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredEvChargePlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleTieredEvChargePlanAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredEvChargePlanAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: TieredEvChargePlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTieredEvChargePlanAdjuster(var step: Double) : TieredEvChargePlanAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredEvChargePlan, value: Double): Double = roundTo(value, step)
}

data class FlaggedTieredEvChargePlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredEvChargePlanAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredEvChargePlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered ev charge plan steps in order and keeps a trace of each. */
data class TieredEvChargePlanEngine(
    var steps: MutableList<TieredEvChargePlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredEvChargePlan): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredEvChargePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredEvChargePlanEngine(flags: Registry): TieredEvChargePlanEngine {
    return TieredEvChargePlanEngine(
        steps = mutableListOf<TieredEvChargePlanAdjuster>(
            TieredTieredEvChargePlanAdjuster(
                mutableListOf<Double>(1140.0, 5660.0),
                mutableListOf<Double>(0.82, 0.81),
            ),
            TitleTieredEvChargePlanAdjuster(mutableMapOf<String, Double>("north" to 1.31)),
            RoundingTieredEvChargePlanAdjuster(0.25),
            FlaggedTieredEvChargePlanAdjuster(flags, "tiered-ev-charge-plan-weighted", -9.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 57373.3,
    )
}
