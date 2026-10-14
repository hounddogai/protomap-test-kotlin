package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tariff request calculation. */
interface TariffRequestStep {
    fun describe(): String
    fun adjust(item: TariffRequest, value: Double): Double
}

data class TariffRequestBudgetOffsetAdjuster(var weight: Double) : TariffRequestStep {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: TariffRequest, value: Double): Double = value + item.budget * weight
}

data class MinimumTariffRequestAdjuster(var minimum: Double) : TariffRequestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TariffRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTariffRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TariffRequestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TariffRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tariff request steps in order and keeps a trace of each. */
data class TariffRequestEngine(
    var steps: MutableList<TariffRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TariffRequest): Double {
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

    fun evaluateAll(items: MutableList<TariffRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTariffRequestEngine(flags: Registry): TariffRequestEngine {
    return TariffRequestEngine(
        steps = mutableListOf<TariffRequestStep>(
            TariffRequestBudgetOffsetAdjuster(0.78),
            MinimumTariffRequestAdjuster(1.5),
            FlaggedTariffRequestAdjuster(flags, "tariff-request-rolling", 7.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 74792.8,
    )
}
