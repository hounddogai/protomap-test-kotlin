package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the near miss request calculation. */
interface NearMissRequestFactor {
    fun describe(): String
    fun adjust(item: NearMissRequest, value: Double): Double
}

data class LaneNearMissRequestAdjuster(var factors: MutableMap<String, Double>) : NearMissRequestFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: NearMissRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentNearMissRequestAdjuster(var percent: Double) : NearMissRequestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredNearMissRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissRequestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NearMissRequestOdometerOffsetAdjuster(var weight: Double) : NearMissRequestFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: NearMissRequest, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class FlaggedNearMissRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NearMissRequestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NearMissRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the near miss request steps in order and keeps a trace of each. */
data class NearMissRequestEngine(
    var steps: MutableList<NearMissRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NearMissRequest): Double {
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

    fun evaluateAll(items: MutableList<NearMissRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNearMissRequestEngine(flags: Registry): NearMissRequestEngine {
    return NearMissRequestEngine(
        steps = mutableListOf<NearMissRequestFactor>(
            LaneNearMissRequestAdjuster(mutableMapOf<String, Double>("south" to 1.17, "retail" to 1.06)),
            PercentNearMissRequestAdjuster(13.5),
            TieredNearMissRequestAdjuster(
                mutableListOf<Double>(634.0, 7747.0),
                mutableListOf<Double>(1.06, 0.81),
            ),
            NearMissRequestOdometerOffsetAdjuster(1.73),
            FlaggedNearMissRequestAdjuster(flags, "near-miss-request-strict", -1.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 62394.3,
    )
}
