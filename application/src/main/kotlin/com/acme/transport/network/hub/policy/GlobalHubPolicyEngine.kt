package com.acme.transport.network.hub.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global hub policy calculation. */
interface GlobalHubPolicyStep {
    fun describe(): String
    fun adjust(item: GlobalHubPolicy, value: Double): Double
}

data class GlobalHubPolicyGraceDaysOffsetAdjuster(var weight: Double) : GlobalHubPolicyStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: GlobalHubPolicy, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class RoundingGlobalHubPolicyAdjuster(var step: Double) : GlobalHubPolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalHubPolicy, value: Double): Double = roundTo(value, step)
}

data class FlaggedGlobalHubPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalHubPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalHubPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleGlobalHubPolicyAdjuster(var factors: MutableMap<String, Double>) : GlobalHubPolicyStep {
    override fun describe(): String = "by title"

    override fun adjust(item: GlobalHubPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapGlobalHubPolicyAdjuster(var cap: Double) : GlobalHubPolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalHubPolicy, value: Double): Double = minOf(value, cap)
}

/** Runs the global hub policy steps in order and keeps a trace of each. */
data class GlobalHubPolicyEngine(
    var steps: MutableList<GlobalHubPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalHubPolicy): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalHubPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalHubPolicyEngine(flags: Registry): GlobalHubPolicyEngine {
    return GlobalHubPolicyEngine(
        steps = mutableListOf<GlobalHubPolicyStep>(
            GlobalHubPolicyGraceDaysOffsetAdjuster(0.94),
            RoundingGlobalHubPolicyAdjuster(1.0),
            FlaggedGlobalHubPolicyAdjuster(flags, "global-hub-policy-default", 1.7),
            TitleGlobalHubPolicyAdjuster(mutableMapOf<String, Double>("east" to 0.88, "online" to 1.09)),
            CapGlobalHubPolicyAdjuster(41603.0),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 87620.4,
    )
}
