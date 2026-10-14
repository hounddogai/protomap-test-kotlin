package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary camera event target calculation. */
interface PrimaryCameraEventTargetModifier {
    fun describe(): String
    fun adjust(item: PrimaryCameraEventTarget, value: Double): Double
}

data class FlaggedPrimaryCameraEventTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryCameraEventTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryCameraEventTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPrimaryCameraEventTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryCameraEventTargetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryCameraEventTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPrimaryCameraEventTargetAdjuster(var percent: Double) : PrimaryCameraEventTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryCameraEventTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CategoryPrimaryCameraEventTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryCameraEventTargetModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: PrimaryCameraEventTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary camera event target steps in order and keeps a trace of each. */
data class PrimaryCameraEventTargetEngine(
    var steps: MutableList<PrimaryCameraEventTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryCameraEventTarget): Double {
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

    fun evaluateAll(items: MutableList<PrimaryCameraEventTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryCameraEventTargetEngine(flags: Registry): PrimaryCameraEventTargetEngine {
    return PrimaryCameraEventTargetEngine(
        steps = mutableListOf<PrimaryCameraEventTargetModifier>(
            FlaggedPrimaryCameraEventTargetAdjuster(flags, "primary-camera-event-target-adaptive", -3.9),
            TieredPrimaryCameraEventTargetAdjuster(
                mutableListOf<Double>(2769.0, 5343.0, 8761.0),
                mutableListOf<Double>(1.01, 1.12, 1.13),
            ),
            PercentPrimaryCameraEventTargetAdjuster(-15.3),
            CategoryPrimaryCameraEventTargetAdjuster(
                mutableMapOf<String, Double>("south" to 1.16, "online" to 1.37, "west" to 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 10735.9,
    )
}
