package com.acme.transport.customs.tariff.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tariff exception calculation. */
interface TariffExceptionStep {
    fun describe(): String
    fun adjust(item: TariffException, value: Double): Double
}

data class RoundingTariffExceptionAdjuster(var step: Double) : TariffExceptionStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffException, value: Double): Double = roundTo(value, step)
}

data class MinimumTariffExceptionAdjuster(var minimum: Double) : TariffExceptionStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TariffException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTariffExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TariffExceptionStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TariffException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTariffExceptionAdjuster(var cap: Double) : TariffExceptionStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TariffException, value: Double): Double = minOf(value, cap)
}

/** Runs the tariff exception steps in order and keeps a trace of each. */
data class TariffExceptionEngine(
    var steps: MutableList<TariffExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TariffException): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TariffException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTariffExceptionEngine(flags: Registry): TariffExceptionEngine {
    return TariffExceptionEngine(
        steps = mutableListOf<TariffExceptionStep>(
            RoundingTariffExceptionAdjuster(0.05),
            MinimumTariffExceptionAdjuster(27.3),
            FlaggedTariffExceptionAdjuster(flags, "tariff-exception-seasonal", 0.1),
            CapTariffExceptionAdjuster(19573.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 82854.4,
    )
}
