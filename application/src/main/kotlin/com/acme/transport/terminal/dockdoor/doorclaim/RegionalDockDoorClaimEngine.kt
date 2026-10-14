package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional dock door claim calculation. */
interface RegionalDockDoorClaimModifier {
    fun describe(): String
    fun adjust(item: RegionalDockDoorClaim, value: Double): Double
}

data class CapRegionalDockDoorClaimAdjuster(var cap: Double) : RegionalDockDoorClaimModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDockDoorClaim, value: Double): Double = minOf(value, cap)
}

data class RegionalDockDoorClaimGraceDaysOffsetAdjuster(var weight: Double) : RegionalDockDoorClaimModifier {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: RegionalDockDoorClaim, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class PercentRegionalDockDoorClaimAdjuster(var percent: Double) : RegionalDockDoorClaimModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDockDoorClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneRegionalDockDoorClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalDockDoorClaimModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: RegionalDockDoorClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalDockDoorClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDockDoorClaimModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDockDoorClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional dock door claim steps in order and keeps a trace of each. */
data class RegionalDockDoorClaimEngine(
    var steps: MutableList<RegionalDockDoorClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalDockDoorClaim): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalDockDoorClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalDockDoorClaimEngine(flags: Registry): RegionalDockDoorClaimEngine {
    return RegionalDockDoorClaimEngine(
        steps = mutableListOf<RegionalDockDoorClaimModifier>(
            CapRegionalDockDoorClaimAdjuster(3719.0),
            RegionalDockDoorClaimGraceDaysOffsetAdjuster(0.87),
            PercentRegionalDockDoorClaimAdjuster(0.2),
            LaneRegionalDockDoorClaimAdjuster(mutableMapOf<String, Double>("online" to 1.36)),
            FlaggedRegionalDockDoorClaimAdjuster(flags, "regional-dock-door-claim-default", 5.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 42519.5,
    )
}
