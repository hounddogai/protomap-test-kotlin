package com.acme.transport.fleetops.utilization.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual utilization claim calculation. */
interface ManualUtilizationClaimStep {
    fun describe(): String
    fun adjust(item: ManualUtilizationClaim, value: Double): Double
}

data class ManualUtilizationClaimBudgetOffsetAdjuster(var weight: Double) : ManualUtilizationClaimStep {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: ManualUtilizationClaim, value: Double): Double = value + item.budget * weight
}

data class FlaggedManualUtilizationClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualUtilizationClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualUtilizationClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelManualUtilizationClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualUtilizationClaimStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: ManualUtilizationClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumManualUtilizationClaimAdjuster(var minimum: Double) : ManualUtilizationClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualUtilizationClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual utilization claim steps in order and keeps a trace of each. */
data class ManualUtilizationClaimEngine(
    var steps: MutableList<ManualUtilizationClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualUtilizationClaim): Double {
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

    fun evaluateAll(items: MutableList<ManualUtilizationClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualUtilizationClaimEngine(flags: Registry): ManualUtilizationClaimEngine {
    return ManualUtilizationClaimEngine(
        steps = mutableListOf<ManualUtilizationClaimStep>(
            ManualUtilizationClaimBudgetOffsetAdjuster(0.7),
            FlaggedManualUtilizationClaimAdjuster(flags, "manual-utilization-claim-global", -1.4),
            ChannelManualUtilizationClaimAdjuster(
                mutableMapOf<String, Double>("retail" to 1.08, "east" to 0.94, "online" to 1.24),
            ),
            MinimumManualUtilizationClaimAdjuster(18.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 75263.9,
    )
}
