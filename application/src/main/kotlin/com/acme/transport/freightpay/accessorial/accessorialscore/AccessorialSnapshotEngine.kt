package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the accessorial snapshot calculation. */
interface AccessorialSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: AccessorialSnapshot, value: Double): Double
}

data class AccessorialSnapshotCreatedAtOffsetAdjuster(var weight: Double) : AccessorialSnapshotAdjuster {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: AccessorialSnapshot, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class PercentAccessorialSnapshotAdjuster(var percent: Double) : AccessorialSnapshotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AccessorialSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingAccessorialSnapshotAdjuster(var step: Double) : AccessorialSnapshotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialSnapshot, value: Double): Double = roundTo(value, step)
}

/** Runs the accessorial snapshot steps in order and keeps a trace of each. */
data class AccessorialSnapshotEngine(
    var steps: MutableList<AccessorialSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AccessorialSnapshot): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAccessorialSnapshotEngine(): AccessorialSnapshotEngine {
    return AccessorialSnapshotEngine(
        steps = mutableListOf<AccessorialSnapshotAdjuster>(
            AccessorialSnapshotCreatedAtOffsetAdjuster(0.83),
            PercentAccessorialSnapshotAdjuster(7.7),
            RoundingAccessorialSnapshotAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 22828.4,
    )
}
