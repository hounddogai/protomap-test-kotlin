package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the relay cycle calculation. */
interface RelayCycleFactor {
    fun describe(): String
    fun adjust(item: RelayCycle, value: Double): Double
}

data class CapRelayCycleAdjuster(var cap: Double) : RelayCycleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayCycle, value: Double): Double = minOf(value, cap)
}

data class FlaggedRelayCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayCycleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRelayCycleAdjuster(var step: Double) : RelayCycleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the relay cycle steps in order and keeps a trace of each. */
data class RelayCycleEngine(
    var steps: MutableList<RelayCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RelayCycle): Double {
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

    fun evaluateAll(items: MutableList<RelayCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRelayCycleEngine(flags: Registry): RelayCycleEngine {
    return RelayCycleEngine(
        steps = mutableListOf<RelayCycleFactor>(
            CapRelayCycleAdjuster(21305.0),
            FlaggedRelayCycleAdjuster(flags, "relay-cycle-scheduled", 4.8),
            RoundingRelayCycleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 17619.2,
    )
}
