package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient dock door claim calculation. */
interface LenientDockDoorClaimFactor {
    fun describe(): String
    fun adjust(item: LenientDockDoorClaim, value: Double): Double
}

data class PercentLenientDockDoorClaimAdjuster(var percent: Double) : LenientDockDoorClaimFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientDockDoorClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLenientDockDoorClaimAdjuster(var step: Double) : LenientDockDoorClaimFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientDockDoorClaim, value: Double): Double = roundTo(value, step)
}

data class TieredLenientDockDoorClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientDockDoorClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientDockDoorClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLenientDockDoorClaimAdjuster(var minimum: Double) : LenientDockDoorClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientDockDoorClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientDockDoorClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientDockDoorClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientDockDoorClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SegmentLenientDockDoorClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDockDoorClaimFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: LenientDockDoorClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient dock door claim steps in order and keeps a trace of each. */
data class LenientDockDoorClaimEngine(
    var steps: MutableList<LenientDockDoorClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientDockDoorClaim): Double {
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

    fun evaluateAll(items: MutableList<LenientDockDoorClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientDockDoorClaimEngine(flags: Registry): LenientDockDoorClaimEngine {
    return LenientDockDoorClaimEngine(
        steps = mutableListOf<LenientDockDoorClaimFactor>(
            PercentLenientDockDoorClaimAdjuster(1.9),
            RoundingLenientDockDoorClaimAdjuster(5.0),
            TieredLenientDockDoorClaimAdjuster(
                mutableListOf<Double>(1715.0, 7749.0, 9305.0),
                mutableListOf<Double>(0.84, 0.9, 0.95),
            ),
            MinimumLenientDockDoorClaimAdjuster(14.5),
            FlaggedLenientDockDoorClaimAdjuster(flags, "lenient-dock-door-claim-legacy", -0.7),
            SegmentLenientDockDoorClaimAdjuster(
                mutableMapOf<String, Double>("south" to 0.72, "east" to 0.98, "online" to 0.75),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 72201.9,
    )
}
