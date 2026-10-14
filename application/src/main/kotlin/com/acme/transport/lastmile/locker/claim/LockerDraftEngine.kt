package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the locker draft calculation. */
interface LockerDraftFactor {
    fun describe(): String
    fun adjust(item: LockerDraft, value: Double): Double
}

data class PercentLockerDraftAdjuster(var percent: Double) : LockerDraftFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLockerDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerDraftFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLockerDraftAdjuster(var step: Double) : LockerDraftFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LockerDraft, value: Double): Double = roundTo(value, step)
}

data class CapLockerDraftAdjuster(var cap: Double) : LockerDraftFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LockerDraft, value: Double): Double = minOf(value, cap)
}

data class MinimumLockerDraftAdjuster(var minimum: Double) : LockerDraftFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleLockerDraftAdjuster(var factors: MutableMap<String, Double>) : LockerDraftFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: LockerDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the locker draft steps in order and keeps a trace of each. */
data class LockerDraftEngine(
    var steps: MutableList<LockerDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LockerDraft): Double {
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

    fun evaluateAll(items: MutableList<LockerDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLockerDraftEngine(): LockerDraftEngine {
    return LockerDraftEngine(
        steps = mutableListOf<LockerDraftFactor>(
            PercentLockerDraftAdjuster(-0.4),
            TieredLockerDraftAdjuster(
                mutableListOf<Double>(1637.0, 1724.0, 9086.0),
                mutableListOf<Double>(0.93, 0.92, 0.91),
            ),
            RoundingLockerDraftAdjuster(0.05),
            CapLockerDraftAdjuster(2078.0),
            MinimumLockerDraftAdjuster(41.1),
            LocaleLockerDraftAdjuster(
                mutableMapOf<String, Double>("retail" to 1.32, "online" to 1.05, "south" to 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 59139.7,
    )
}
