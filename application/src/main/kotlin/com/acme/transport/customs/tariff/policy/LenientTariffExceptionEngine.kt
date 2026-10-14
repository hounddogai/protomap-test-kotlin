package com.acme.transport.customs.tariff.policy

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient tariff exception calculation. */
interface LenientTariffExceptionAdjuster {
    fun describe(): String
    fun adjust(item: LenientTariffException, value: Double): Double
}

data class LaneLenientTariffExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientTariffExceptionAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: LenientTariffException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LenientTariffExceptionMarginOffsetAdjuster(var weight: Double) : LenientTariffExceptionAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: LenientTariffException, value: Double): Double = value + item.margin * weight
}

data class PercentLenientTariffExceptionAdjuster(var percent: Double) : LenientTariffExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientTariffException, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lenient tariff exception steps in order and keeps a trace of each. */
data class LenientTariffExceptionEngine(
    var steps: MutableList<LenientTariffExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientTariffException): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientTariffException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientTariffExceptionEngine(): LenientTariffExceptionEngine {
    return LenientTariffExceptionEngine(
        steps = mutableListOf<LenientTariffExceptionAdjuster>(
            LaneLenientTariffExceptionAdjuster(
                mutableMapOf<String, Double>("north" to 0.94, "retail" to 1.26, "west" to 1.33),
            ),
            LenientTariffExceptionMarginOffsetAdjuster(1.53),
            PercentLenientTariffExceptionAdjuster(-8.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 53641.8,
    )
}
