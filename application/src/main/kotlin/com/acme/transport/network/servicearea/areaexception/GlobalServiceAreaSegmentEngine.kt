package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global service area segment calculation. */
interface GlobalServiceAreaSegmentModifier {
    fun describe(): String
    fun adjust(item: GlobalServiceAreaSegment, value: Double): Double
}

data class TieredGlobalServiceAreaSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalServiceAreaSegmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalServiceAreaSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedGlobalServiceAreaSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalServiceAreaSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalServiceAreaSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class GlobalServiceAreaSegmentEtaMinutesOffsetAdjuster(
    var weight: Double,
) : GlobalServiceAreaSegmentModifier {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: GlobalServiceAreaSegment, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

/** Runs the global service area segment steps in order and keeps a trace of each. */
data class GlobalServiceAreaSegmentEngine(
    var steps: MutableList<GlobalServiceAreaSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalServiceAreaSegment): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalServiceAreaSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalServiceAreaSegmentEngine(flags: Registry): GlobalServiceAreaSegmentEngine {
    return GlobalServiceAreaSegmentEngine(
        steps = mutableListOf<GlobalServiceAreaSegmentModifier>(
            TieredGlobalServiceAreaSegmentAdjuster(
                mutableListOf<Double>(1302.0, 4340.0),
                mutableListOf<Double>(0.85, 0.94),
            ),
            FlaggedGlobalServiceAreaSegmentAdjuster(flags, "global-service-area-segment-tiered", -9.1),
            GlobalServiceAreaSegmentEtaMinutesOffsetAdjuster(1.54),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 58163.9,
    )
}
