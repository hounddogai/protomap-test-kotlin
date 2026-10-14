package com.acme.transport.customs.tariff.cycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered tariff bundle calculation. */
interface TieredTariffBundleModifier {
    fun describe(): String
    fun adjust(item: TieredTariffBundle, value: Double): Double
}

data class MinimumTieredTariffBundleAdjuster(var minimum: Double) : TieredTariffBundleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredTariffBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingTieredTariffBundleAdjuster(var step: Double) : TieredTariffBundleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredTariffBundle, value: Double): Double = roundTo(value, step)
}

data class TieredTariffBundleEtaMinutesOffsetAdjuster(var weight: Double) : TieredTariffBundleModifier {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: TieredTariffBundle, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class PercentTieredTariffBundleAdjuster(var percent: Double) : TieredTariffBundleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredTariffBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tiered tariff bundle steps in order and keeps a trace of each. */
data class TieredTariffBundleEngine(
    var steps: MutableList<TieredTariffBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredTariffBundle): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredTariffBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredTariffBundleEngine(): TieredTariffBundleEngine {
    return TieredTariffBundleEngine(
        steps = mutableListOf<TieredTariffBundleModifier>(
            MinimumTieredTariffBundleAdjuster(11.0),
            RoundingTieredTariffBundleAdjuster(5.0),
            TieredTariffBundleEtaMinutesOffsetAdjuster(0.45),
            PercentTieredTariffBundleAdjuster(-1.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 11244.2,
    )
}
