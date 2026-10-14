package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling camera event audit calculation. */
interface RollingCameraEventAuditStep {
    fun describe(): String
    fun adjust(item: RollingCameraEventAudit, value: Double): Double
}

data class RollingCameraEventAuditRetriesOffsetAdjuster(var weight: Double) : RollingCameraEventAuditStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RollingCameraEventAudit, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class RoundingRollingCameraEventAuditAdjuster(var step: Double) : RollingCameraEventAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingCameraEventAudit, value: Double): Double = roundTo(value, step)
}

data class DepotRollingCameraEventAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingCameraEventAuditStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: RollingCameraEventAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRollingCameraEventAuditAdjuster(var percent: Double) : RollingCameraEventAuditStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingCameraEventAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRollingCameraEventAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingCameraEventAuditStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingCameraEventAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling camera event audit steps in order and keeps a trace of each. */
data class RollingCameraEventAuditEngine(
    var steps: MutableList<RollingCameraEventAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingCameraEventAudit): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingCameraEventAuditStep = steps[index]
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

    fun evaluateAll(items: MutableList<RollingCameraEventAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingCameraEventAuditEngine(flags: Registry): RollingCameraEventAuditEngine {
    return RollingCameraEventAuditEngine(
        steps = mutableListOf<RollingCameraEventAuditStep>(
            RollingCameraEventAuditRetriesOffsetAdjuster(0.41),
            RoundingRollingCameraEventAuditAdjuster(0.5),
            DepotRollingCameraEventAuditAdjuster(
                mutableMapOf<String, Double>("south" to 1.28, "retail" to 1.36, "west" to 0.86),
            ),
            PercentRollingCameraEventAuditAdjuster(10.0),
            FlaggedRollingCameraEventAuditAdjuster(flags, "rolling-camera-event-audit-regional", 8.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 64703.6,
    )
}
