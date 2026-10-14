package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered parcel assignment calculation. */
interface TieredParcelAssignmentModifier {
    fun describe(): String
    fun adjust(item: TieredParcelAssignment, value: Double): Double
}

data class PercentTieredParcelAssignmentAdjuster(var percent: Double) : TieredParcelAssignmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredParcelAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTieredParcelAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredParcelAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredParcelAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTieredParcelAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredParcelAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredParcelAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTieredParcelAssignmentAdjuster(var step: Double) : TieredParcelAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredParcelAssignment, value: Double): Double = roundTo(value, step)
}

data class NameTieredParcelAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredParcelAssignmentModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredParcelAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered parcel assignment steps in order and keeps a trace of each. */
data class TieredParcelAssignmentEngine(
    var steps: MutableList<TieredParcelAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredParcelAssignment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredParcelAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredParcelAssignmentEngine(flags: Registry): TieredParcelAssignmentEngine {
    return TieredParcelAssignmentEngine(
        steps = mutableListOf<TieredParcelAssignmentModifier>(
            PercentTieredParcelAssignmentAdjuster(18.5),
            TieredTieredParcelAssignmentAdjuster(
                mutableListOf<Double>(6798.0, 8351.0),
                mutableListOf<Double>(0.89, 1.14),
            ),
            FlaggedTieredParcelAssignmentAdjuster(flags, "tiered-parcel-assignment-tiered", -8.7),
            RoundingTieredParcelAssignmentAdjuster(0.5),
            NameTieredParcelAssignmentAdjuster(mutableMapOf<String, Double>("north" to 0.81, "online" to 0.83)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 32312.7,
    )
}
