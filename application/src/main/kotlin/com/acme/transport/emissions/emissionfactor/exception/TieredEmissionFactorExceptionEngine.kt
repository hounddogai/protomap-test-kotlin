package com.acme.transport.emissions.emissionfactor.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered emission factor exception calculation. */
interface TieredEmissionFactorExceptionStep {
    fun describe(): String
    fun adjust(item: TieredEmissionFactorException, value: Double): Double
}

data class PercentTieredEmissionFactorExceptionAdjuster(
    var percent: Double,
) : TieredEmissionFactorExceptionStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredEmissionFactorException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTieredEmissionFactorExceptionAdjuster(var cap: Double) : TieredEmissionFactorExceptionStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredEmissionFactorException, value: Double): Double = minOf(value, cap)
}

data class CountryTieredEmissionFactorExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredEmissionFactorExceptionStep {
    override fun describe(): String = "by country"

    override fun adjust(item: TieredEmissionFactorException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTieredEmissionFactorExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredEmissionFactorExceptionStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredEmissionFactorException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered emission factor exception steps in order and keeps a trace of each. */
data class TieredEmissionFactorExceptionEngine(
    var steps: MutableList<TieredEmissionFactorExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredEmissionFactorException): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredEmissionFactorException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredEmissionFactorExceptionEngine(flags: Registry): TieredEmissionFactorExceptionEngine {
    return TieredEmissionFactorExceptionEngine(
        steps = mutableListOf<TieredEmissionFactorExceptionStep>(
            PercentTieredEmissionFactorExceptionAdjuster(3.9),
            CapTieredEmissionFactorExceptionAdjuster(23033.0),
            CountryTieredEmissionFactorExceptionAdjuster(
                mutableMapOf<String, Double>("west" to 1.07, "north" to 0.71, "east" to 1.35),
            ),
            FlaggedTieredEmissionFactorExceptionAdjuster(flags, "tiered-emission-factor-exception-lenient", 8.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 43766.0,
    )
}
