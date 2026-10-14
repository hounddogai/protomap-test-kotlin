package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary broker snapshot calculation. */
interface PrimaryBrokerSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryBrokerSnapshot, value: Double): Double
}

data class CapPrimaryBrokerSnapshotAdjuster(var cap: Double) : PrimaryBrokerSnapshotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryBrokerSnapshot, value: Double): Double = minOf(value, cap)
}

data class FlaggedPrimaryBrokerSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryBrokerSnapshotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryBrokerSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPrimaryBrokerSnapshotAdjuster(var step: Double) : PrimaryBrokerSnapshotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryBrokerSnapshot, value: Double): Double = roundTo(value, step)
}

/** Runs the primary broker snapshot steps in order and keeps a trace of each. */
data class PrimaryBrokerSnapshotEngine(
    var steps: MutableList<PrimaryBrokerSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryBrokerSnapshot): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryBrokerSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryBrokerSnapshotEngine(flags: Registry): PrimaryBrokerSnapshotEngine {
    return PrimaryBrokerSnapshotEngine(
        steps = mutableListOf<PrimaryBrokerSnapshotAdjuster>(
            CapPrimaryBrokerSnapshotAdjuster(41522.0),
            FlaggedPrimaryBrokerSnapshotAdjuster(flags, "primary-broker-snapshot-batched", -9.7),
            RoundingPrimaryBrokerSnapshotAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 12941.1,
    )
}
