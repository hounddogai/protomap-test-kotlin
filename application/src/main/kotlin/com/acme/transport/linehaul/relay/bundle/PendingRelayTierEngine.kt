package com.acme.transport.linehaul.relay.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending relay tier calculation. */
interface PendingRelayTierModifier {
    fun describe(): String
    fun adjust(item: PendingRelayTier, value: Double): Double
}

data class RoundingPendingRelayTierAdjuster(var step: Double) : PendingRelayTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingRelayTier, value: Double): Double = roundTo(value, step)
}

data class PercentPendingRelayTierAdjuster(var percent: Double) : PendingRelayTierModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingRelayTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingRelayTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingRelayTierModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingRelayTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending relay tier steps in order and keeps a trace of each. */
data class PendingRelayTierEngine(
    var steps: MutableList<PendingRelayTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingRelayTier): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingRelayTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingRelayTierEngine(flags: Registry): PendingRelayTierEngine {
    return PendingRelayTierEngine(
        steps = mutableListOf<PendingRelayTierModifier>(
            RoundingPendingRelayTierAdjuster(0.5),
            PercentPendingRelayTierAdjuster(1.6),
            FlaggedPendingRelayTierAdjuster(flags, "pending-relay-tier-rolling", -2.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 33713.0,
    )
}
