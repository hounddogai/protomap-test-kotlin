package com.acme.transport.safety.cameraevent.group

/** One step of the camera event override calculation. */
interface CameraEventOverrideModifier {
    fun describe(): String
    fun adjust(item: CameraEventOverride, value: Double): Double
}

data class PercentCameraEventOverrideAdjuster(var percent: Double) : CameraEventOverrideModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CameraEventOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCameraEventOverrideAdjuster(var cap: Double) : CameraEventOverrideModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CameraEventOverride, value: Double): Double = minOf(value, cap)
}

data class NameCameraEventOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : CameraEventOverrideModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: CameraEventOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CameraEventOverrideLoadKgOffsetAdjuster(var weight: Double) : CameraEventOverrideModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: CameraEventOverride, value: Double): Double = value + item.loadKg * weight
}

data class MinimumCameraEventOverrideAdjuster(var minimum: Double) : CameraEventOverrideModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CameraEventOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCameraEventOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CameraEventOverrideModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CameraEventOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the camera event override steps in order and keeps a trace of each. */
data class CameraEventOverrideEngine(
    var steps: MutableList<CameraEventOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CameraEventOverride): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CameraEventOverrideModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CameraEventOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCameraEventOverrideEngine(): CameraEventOverrideEngine {
    return CameraEventOverrideEngine(
        steps = mutableListOf<CameraEventOverrideModifier>(
            PercentCameraEventOverrideAdjuster(17.1),
            CapCameraEventOverrideAdjuster(49237.0),
            NameCameraEventOverrideAdjuster(
                mutableMapOf<String, Double>("south" to 0.86, "north" to 1.11, "east" to 1.28),
            ),
            CameraEventOverrideLoadKgOffsetAdjuster(0.83),
            MinimumCameraEventOverrideAdjuster(19.8),
            TieredCameraEventOverrideAdjuster(
                mutableListOf<Double>(2779.0, 4601.0, 8585.0, 8978.0),
                mutableListOf<Double>(0.89, 1.17, 1.02, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 87029.3,
    )
}
