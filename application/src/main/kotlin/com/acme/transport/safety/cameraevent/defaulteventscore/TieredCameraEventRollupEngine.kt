package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered camera event rollup calculation. */
interface TieredCameraEventRollupModifier {
    fun describe(): String
    fun adjust(item: TieredCameraEventRollup, value: Double): Double
}

data class MinimumTieredCameraEventRollupAdjuster(var minimum: Double) : TieredCameraEventRollupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredCameraEventRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentTieredCameraEventRollupAdjuster(var percent: Double) : TieredCameraEventRollupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredCameraEventRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTieredCameraEventRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredCameraEventRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredCameraEventRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneTieredCameraEventRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredCameraEventRollupModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: TieredCameraEventRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered camera event rollup steps in order and keeps a trace of each. */
data class TieredCameraEventRollupEngine(
    var steps: MutableList<TieredCameraEventRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredCameraEventRollup): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredCameraEventRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredCameraEventRollupEngine(flags: Registry): TieredCameraEventRollupEngine {
    return TieredCameraEventRollupEngine(
        steps = mutableListOf<TieredCameraEventRollupModifier>(
            MinimumTieredCameraEventRollupAdjuster(48.2),
            PercentTieredCameraEventRollupAdjuster(-24.3),
            FlaggedTieredCameraEventRollupAdjuster(flags, "tiered-camera-event-rollup-adaptive", 7.0),
            LaneTieredCameraEventRollupAdjuster(mutableMapOf<String, Double>("online" to 0.82)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 84612.5,
    )
}
