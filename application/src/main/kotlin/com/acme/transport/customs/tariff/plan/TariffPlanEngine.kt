package com.acme.transport.customs.tariff.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tariff plan calculation. */
interface TariffPlanAdjuster {
    fun describe(): String
    fun adjust(item: TariffPlan, value: Double): Double
}

data class PercentTariffPlanAdjuster(var percent: Double) : TariffPlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TariffPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTariffPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TariffPlanAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TariffPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTariffPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TariffPlanAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TariffPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTariffPlanAdjuster(var step: Double) : TariffPlanAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffPlan, value: Double): Double = roundTo(value, step)
}

/** Runs the tariff plan steps in order and keeps a trace of each. */
data class TariffPlanEngine(
    var steps: MutableList<TariffPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TariffPlan): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TariffPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTariffPlanEngine(flags: Registry): TariffPlanEngine {
    return TariffPlanEngine(
        steps = mutableListOf<TariffPlanAdjuster>(
            PercentTariffPlanAdjuster(22.8),
            FlaggedTariffPlanAdjuster(flags, "tariff-plan-default", -2.6),
            TieredTariffPlanAdjuster(
                mutableListOf<Double>(4375.0, 4718.0, 7192.0),
                mutableListOf<Double>(1.01, 1.11, 0.85),
            ),
            RoundingTariffPlanAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 83892.1,
    )
}
