package com.acme.transport.customs.hold.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped hold override calculation. */
interface CappedHoldOverrideFactor {
    fun describe(): String
    fun adjust(item: CappedHoldOverride, value: Double): Double
}

data class FlaggedCappedHoldOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedHoldOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedHoldOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCappedHoldOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedHoldOverrideFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedHoldOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentCappedHoldOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedHoldOverrideFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: CappedHoldOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCappedHoldOverrideAdjuster(var cap: Double) : CappedHoldOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedHoldOverride, value: Double): Double = minOf(value, cap)
}

data class RoundingCappedHoldOverrideAdjuster(var step: Double) : CappedHoldOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedHoldOverride, value: Double): Double = roundTo(value, step)
}

/** Runs the capped hold override steps in order and keeps a trace of each. */
data class CappedHoldOverrideEngine(
    var steps: MutableList<CappedHoldOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedHoldOverride): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedHoldOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedHoldOverrideEngine(flags: Registry): CappedHoldOverrideEngine {
    return CappedHoldOverrideEngine(
        steps = mutableListOf<CappedHoldOverrideFactor>(
            FlaggedCappedHoldOverrideAdjuster(flags, "capped-hold-override-regional", -6.2),
            TieredCappedHoldOverrideAdjuster(
                mutableListOf<Double>(914.0, 1697.0, 2534.0, 3813.0),
                mutableListOf<Double>(0.84, 0.94, 0.99, 0.99),
            ),
            SegmentCappedHoldOverrideAdjuster(mutableMapOf<String, Double>("north" to 1.26)),
            CapCappedHoldOverrideAdjuster(36894.0),
            RoundingCappedHoldOverrideAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 83382.7,
    )
}
