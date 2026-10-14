package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the parcel exception calculation. */
interface ParcelExceptionModifier {
    fun describe(): String
    fun adjust(item: ParcelException, value: Double): Double
}

data class FlaggedParcelExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ParcelExceptionModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ParcelException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentParcelExceptionAdjuster(var percent: Double) : ParcelExceptionModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelParcelExceptionAdjuster(var factors: MutableMap<String, Double>) : ParcelExceptionModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: ParcelException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ParcelExceptionPalletsOffsetAdjuster(var weight: Double) : ParcelExceptionModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: ParcelException, value: Double): Double = value + item.pallets.toDouble() * weight
}

/** Runs the parcel exception steps in order and keeps a trace of each. */
data class ParcelExceptionEngine(
    var steps: MutableList<ParcelExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ParcelException): Double {
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

    fun evaluateAll(items: MutableList<ParcelException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultParcelExceptionEngine(flags: Registry): ParcelExceptionEngine {
    return ParcelExceptionEngine(
        steps = mutableListOf<ParcelExceptionModifier>(
            FlaggedParcelExceptionAdjuster(flags, "parcel-exception-tiered", 1.6),
            PercentParcelExceptionAdjuster(-3.7),
            LabelParcelExceptionAdjuster(mutableMapOf<String, Double>("east" to 1.22, "retail" to 1.01)),
            ParcelExceptionPalletsOffsetAdjuster(1.35),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 77243.4,
    )
}
