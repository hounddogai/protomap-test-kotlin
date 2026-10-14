package com.acme.transport.safety.cameraevent.eventbundle

/** One step of the pending camera event bundle calculation. */
interface PendingCameraEventBundleModifier {
    fun describe(): String
    fun adjust(item: PendingCameraEventBundle, value: Double): Double
}

data class PercentPendingCameraEventBundleAdjuster(var percent: Double) : PendingCameraEventBundleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingCameraEventBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPendingCameraEventBundleAdjuster(var cap: Double) : PendingCameraEventBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingCameraEventBundle, value: Double): Double = minOf(value, cap)
}

data class MinimumPendingCameraEventBundleAdjuster(var minimum: Double) : PendingCameraEventBundleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingCameraEventBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SourcePendingCameraEventBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingCameraEventBundleModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: PendingCameraEventBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending camera event bundle steps in order and keeps a trace of each. */
data class PendingCameraEventBundleEngine(
    var steps: MutableList<PendingCameraEventBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingCameraEventBundle): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingCameraEventBundleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingCameraEventBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingCameraEventBundleEngine(): PendingCameraEventBundleEngine {
    return PendingCameraEventBundleEngine(
        steps = mutableListOf<PendingCameraEventBundleModifier>(
            PercentPendingCameraEventBundleAdjuster(14.1),
            CapPendingCameraEventBundleAdjuster(38182.0),
            MinimumPendingCameraEventBundleAdjuster(7.2),
            SourcePendingCameraEventBundleAdjuster(mutableMapOf<String, Double>("online" to 0.82)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 28410.1,
    )
}
