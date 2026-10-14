package com.acme.transport.safety.cameraevent.eventslot

/** One step of the fallback camera event snapshot calculation. */
interface FallbackCameraEventSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: FallbackCameraEventSnapshot, value: Double): Double
}

data class TieredFallbackCameraEventSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackCameraEventSnapshotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackCameraEventSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FallbackCameraEventSnapshotFloorValueOffsetAdjuster(
    var weight: Double,
) : FallbackCameraEventSnapshotAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: FallbackCameraEventSnapshot, value: Double): Double = value + item.floorValue * weight
}

data class PercentFallbackCameraEventSnapshotAdjuster(
    var percent: Double,
) : FallbackCameraEventSnapshotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackCameraEventSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback camera event snapshot steps in order and keeps a trace of each. */
data class FallbackCameraEventSnapshotEngine(
    var steps: MutableList<FallbackCameraEventSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackCameraEventSnapshot): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackCameraEventSnapshotAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackCameraEventSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackCameraEventSnapshotEngine(): FallbackCameraEventSnapshotEngine {
    return FallbackCameraEventSnapshotEngine(
        steps = mutableListOf<FallbackCameraEventSnapshotAdjuster>(
            TieredFallbackCameraEventSnapshotAdjuster(
                mutableListOf<Double>(242.0, 5343.0, 6538.0),
                mutableListOf<Double>(1.12, 0.84, 0.93),
            ),
            FallbackCameraEventSnapshotFloorValueOffsetAdjuster(1.63),
            PercentFallbackCameraEventSnapshotAdjuster(1.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 40066.5,
    )
}
