package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped dock door exception calculation. */
interface CappedDockDoorExceptionAdjuster {
    fun describe(): String
    fun adjust(item: CappedDockDoorException, value: Double): Double
}

data class CappedDockDoorExceptionCeilingOffsetAdjuster(var weight: Double) : CappedDockDoorExceptionAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: CappedDockDoorException, value: Double): Double = value + item.ceiling * weight
}

data class TieredCappedDockDoorExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedDockDoorExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedDockDoorException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCappedDockDoorExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedDockDoorExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedDockDoorException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleCappedDockDoorExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedDockDoorExceptionAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: CappedDockDoorException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCappedDockDoorExceptionAdjuster(var percent: Double) : CappedDockDoorExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedDockDoorException, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped dock door exception steps in order and keeps a trace of each. */
data class CappedDockDoorExceptionEngine(
    var steps: MutableList<CappedDockDoorExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedDockDoorException): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedDockDoorException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedDockDoorExceptionEngine(flags: Registry): CappedDockDoorExceptionEngine {
    return CappedDockDoorExceptionEngine(
        steps = mutableListOf<CappedDockDoorExceptionAdjuster>(
            CappedDockDoorExceptionCeilingOffsetAdjuster(0.89),
            TieredCappedDockDoorExceptionAdjuster(
                mutableListOf<Double>(168.0, 1953.0, 6883.0),
                mutableListOf<Double>(1.17, 0.98, 0.81),
            ),
            FlaggedCappedDockDoorExceptionAdjuster(flags, "capped-dock-door-exception-seasonal", -7.9),
            TitleCappedDockDoorExceptionAdjuster(
                mutableMapOf<String, Double>("east" to 1.31, "central" to 0.85, "west" to 0.75),
            ),
            PercentCappedDockDoorExceptionAdjuster(3.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 59822.5,
    )
}
