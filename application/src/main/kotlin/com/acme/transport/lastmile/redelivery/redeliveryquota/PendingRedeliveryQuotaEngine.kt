package com.acme.transport.lastmile.redelivery.redeliveryquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending redelivery quota calculation. */
interface PendingRedeliveryQuotaFactor {
    fun describe(): String
    fun adjust(item: PendingRedeliveryQuota, value: Double): Double
}

data class FlaggedPendingRedeliveryQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingRedeliveryQuotaFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingRedeliveryQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DepotPendingRedeliveryQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingRedeliveryQuotaFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: PendingRedeliveryQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPendingRedeliveryQuotaAdjuster(var percent: Double) : PendingRedeliveryQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingRedeliveryQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPendingRedeliveryQuotaAdjuster(var cap: Double) : PendingRedeliveryQuotaFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingRedeliveryQuota, value: Double): Double = minOf(value, cap)
}

/** Runs the pending redelivery quota steps in order and keeps a trace of each. */
data class PendingRedeliveryQuotaEngine(
    var steps: MutableList<PendingRedeliveryQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingRedeliveryQuota): Double {
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

    fun evaluateAll(items: MutableList<PendingRedeliveryQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingRedeliveryQuotaEngine(flags: Registry): PendingRedeliveryQuotaEngine {
    return PendingRedeliveryQuotaEngine(
        steps = mutableListOf<PendingRedeliveryQuotaFactor>(
            FlaggedPendingRedeliveryQuotaAdjuster(flags, "pending-redelivery-quota-seasonal", 0.8),
            DepotPendingRedeliveryQuotaAdjuster(mutableMapOf<String, Double>("south" to 1.12, "retail" to 0.98)),
            PercentPendingRedeliveryQuotaAdjuster(11.1),
            CapPendingRedeliveryQuotaAdjuster(20665.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 24087.6,
    )
}
