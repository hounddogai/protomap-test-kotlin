package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.kit.flags.Registry

/** One step of the dock door claim calculation. */
interface DockDoorClaimAdjuster {
    fun describe(): String
    fun adjust(item: DockDoorClaim, value: Double): Double
}

data class CapDockDoorClaimAdjuster(var cap: Double) : DockDoorClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorClaim, value: Double): Double = minOf(value, cap)
}

data class MinimumDockDoorClaimAdjuster(var minimum: Double) : DockDoorClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDockDoorClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDockDoorClaimAdjuster(var percent: Double) : DockDoorClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DockDoorClaimEtaMinutesOffsetAdjuster(var weight: Double) : DockDoorClaimAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: DockDoorClaim, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

/** Runs the dock door claim steps in order and keeps a trace of each. */
data class DockDoorClaimEngine(
    var steps: MutableList<DockDoorClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DockDoorClaim): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorClaimAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DockDoorClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDockDoorClaimEngine(flags: Registry): DockDoorClaimEngine {
    return DockDoorClaimEngine(
        steps = mutableListOf<DockDoorClaimAdjuster>(
            CapDockDoorClaimAdjuster(20591.0),
            MinimumDockDoorClaimAdjuster(40.7),
            FlaggedDockDoorClaimAdjuster(flags, "dock-door-claim-seasonal", 7.6),
            PercentDockDoorClaimAdjuster(24.7),
            DockDoorClaimEtaMinutesOffsetAdjuster(0.86),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 29649.1,
    )
}
