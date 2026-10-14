package com.acme.transport.customs.broker.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling broker adjustment calculation. */
interface RollingBrokerAdjustmentModifier {
    fun describe(): String
    fun adjust(item: RollingBrokerAdjustment, value: Double): Double
}

data class FlaggedRollingBrokerAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingBrokerAdjustmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingBrokerAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRollingBrokerAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingBrokerAdjustmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingBrokerAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingBrokerAdjustmentMaximumOffsetAdjuster(var weight: Double) : RollingBrokerAdjustmentModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: RollingBrokerAdjustment, value: Double): Double = value + item.maximum * weight
}

/** Runs the rolling broker adjustment steps in order and keeps a trace of each. */
data class RollingBrokerAdjustmentEngine(
    var steps: MutableList<RollingBrokerAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingBrokerAdjustment): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingBrokerAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingBrokerAdjustmentEngine(flags: Registry): RollingBrokerAdjustmentEngine {
    return RollingBrokerAdjustmentEngine(
        steps = mutableListOf<RollingBrokerAdjustmentModifier>(
            FlaggedRollingBrokerAdjustmentAdjuster(flags, "rolling-broker-adjustment-tiered", -1.9),
            TieredRollingBrokerAdjustmentAdjuster(
                mutableListOf<Double>(1871.0, 5663.0, 8462.0),
                mutableListOf<Double>(1.04, 0.9, 1.04),
            ),
            RollingBrokerAdjustmentMaximumOffsetAdjuster(1.37),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 88753.0,
    )
}
