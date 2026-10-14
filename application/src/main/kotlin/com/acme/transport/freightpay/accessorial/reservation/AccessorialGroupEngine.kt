package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the accessorial group calculation. */
interface AccessorialGroupAdjuster {
    fun describe(): String
    fun adjust(item: AccessorialGroup, value: Double): Double
}

data class AccessorialGroupCapacityOffsetAdjuster(var weight: Double) : AccessorialGroupAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: AccessorialGroup, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class RoundingAccessorialGroupAdjuster(var step: Double) : AccessorialGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialGroup, value: Double): Double = roundTo(value, step)
}

data class PercentAccessorialGroupAdjuster(var percent: Double) : AccessorialGroupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AccessorialGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAccessorialGroupAdjuster(var minimum: Double) : AccessorialGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the accessorial group steps in order and keeps a trace of each. */
data class AccessorialGroupEngine(
    var steps: MutableList<AccessorialGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AccessorialGroup): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAccessorialGroupEngine(): AccessorialGroupEngine {
    return AccessorialGroupEngine(
        steps = mutableListOf<AccessorialGroupAdjuster>(
            AccessorialGroupCapacityOffsetAdjuster(1.56),
            RoundingAccessorialGroupAdjuster(0.5),
            PercentAccessorialGroupAdjuster(-23.7),
            MinimumAccessorialGroupAdjuster(32.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 36349.1,
    )
}
