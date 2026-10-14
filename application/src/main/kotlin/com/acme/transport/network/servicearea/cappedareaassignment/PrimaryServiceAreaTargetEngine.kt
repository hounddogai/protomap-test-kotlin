package com.acme.transport.network.servicearea.cappedareaassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary service area target calculation. */
interface PrimaryServiceAreaTargetStep {
    fun describe(): String
    fun adjust(item: PrimaryServiceAreaTarget, value: Double): Double
}

data class FlaggedPrimaryServiceAreaTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryServiceAreaTargetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryServiceAreaTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPrimaryServiceAreaTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryServiceAreaTargetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryServiceAreaTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPrimaryServiceAreaTargetAdjuster(var step: Double) : PrimaryServiceAreaTargetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryServiceAreaTarget, value: Double): Double = roundTo(value, step)
}

data class UnitPrimaryServiceAreaTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryServiceAreaTargetStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: PrimaryServiceAreaTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PrimaryServiceAreaTargetThresholdOffsetAdjuster(var weight: Double) : PrimaryServiceAreaTargetStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: PrimaryServiceAreaTarget, value: Double): Double = value + item.threshold * weight
}

data class CapPrimaryServiceAreaTargetAdjuster(var cap: Double) : PrimaryServiceAreaTargetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryServiceAreaTarget, value: Double): Double = minOf(value, cap)
}

/** Runs the primary service area target steps in order and keeps a trace of each. */
data class PrimaryServiceAreaTargetEngine(
    var steps: MutableList<PrimaryServiceAreaTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryServiceAreaTarget): Double {
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

    fun evaluateAll(items: MutableList<PrimaryServiceAreaTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryServiceAreaTargetEngine(flags: Registry): PrimaryServiceAreaTargetEngine {
    return PrimaryServiceAreaTargetEngine(
        steps = mutableListOf<PrimaryServiceAreaTargetStep>(
            FlaggedPrimaryServiceAreaTargetAdjuster(flags, "primary-service-area-target-strict", -4.6),
            TieredPrimaryServiceAreaTargetAdjuster(
                mutableListOf<Double>(976.0, 2468.0),
                mutableListOf<Double>(0.91, 0.95),
            ),
            RoundingPrimaryServiceAreaTargetAdjuster(5.0),
            UnitPrimaryServiceAreaTargetAdjuster(
                mutableMapOf<String, Double>("north" to 1.11, "online" to 1.11, "west" to 1.29),
            ),
            PrimaryServiceAreaTargetThresholdOffsetAdjuster(0.23),
            CapPrimaryServiceAreaTargetAdjuster(47721.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 9982.5,
    )
}
