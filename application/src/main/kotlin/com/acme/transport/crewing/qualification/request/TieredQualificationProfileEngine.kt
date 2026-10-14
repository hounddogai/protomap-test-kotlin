package com.acme.transport.crewing.qualification.request

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered qualification profile calculation. */
interface TieredQualificationProfileModifier {
    fun describe(): String
    fun adjust(item: TieredQualificationProfile, value: Double): Double
}

data class TieredTieredQualificationProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredQualificationProfileModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredQualificationProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TieredQualificationProfileScoreOffsetAdjuster(
    var weight: Double,
) : TieredQualificationProfileModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: TieredQualificationProfile, value: Double): Double = value + item.score * weight
}

data class CapTieredQualificationProfileAdjuster(var cap: Double) : TieredQualificationProfileModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredQualificationProfile, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered qualification profile steps in order and keeps a trace of each. */
data class TieredQualificationProfileEngine(
    var steps: MutableList<TieredQualificationProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredQualificationProfile): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredQualificationProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredQualificationProfileEngine(): TieredQualificationProfileEngine {
    return TieredQualificationProfileEngine(
        steps = mutableListOf<TieredQualificationProfileModifier>(
            TieredTieredQualificationProfileAdjuster(
                mutableListOf<Double>(5671.0, 7390.0, 9209.0, 9243.0),
                mutableListOf<Double>(1.09, 1.2, 0.86, 0.98),
            ),
            TieredQualificationProfileScoreOffsetAdjuster(1.67),
            CapTieredQualificationProfileAdjuster(13461.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 19631.1,
    )
}
