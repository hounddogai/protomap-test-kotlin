package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual dwell override calculation. */
interface ManualDwellOverrideAdjuster {
    fun describe(): String
    fun adjust(item: ManualDwellOverride, value: Double): Double
}

data class FlaggedManualDwellOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDwellOverrideAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDwellOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameManualDwellOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualDwellOverrideAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualDwellOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ManualDwellOverrideDiscountOffsetAdjuster(var weight: Double) : ManualDwellOverrideAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: ManualDwellOverride, value: Double): Double = value + item.discount * weight
}

data class TieredManualDwellOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualDwellOverrideAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualDwellOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentManualDwellOverrideAdjuster(var percent: Double) : ManualDwellOverrideAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualDwellOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the manual dwell override steps in order and keeps a trace of each. */
data class ManualDwellOverrideEngine(
    var steps: MutableList<ManualDwellOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualDwellOverride): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualDwellOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualDwellOverrideEngine(flags: Registry): ManualDwellOverrideEngine {
    return ManualDwellOverrideEngine(
        steps = mutableListOf<ManualDwellOverrideAdjuster>(
            FlaggedManualDwellOverrideAdjuster(flags, "manual-dwell-override-regional", 7.7),
            NameManualDwellOverrideAdjuster(mutableMapOf<String, Double>("north" to 0.88, "central" to 0.96)),
            ManualDwellOverrideDiscountOffsetAdjuster(1.3),
            TieredManualDwellOverrideAdjuster(
                mutableListOf<Double>(1876.0, 6543.0),
                mutableListOf<Double>(0.93, 1.01),
            ),
            PercentManualDwellOverrideAdjuster(-21.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 76387.5,
    )
}
