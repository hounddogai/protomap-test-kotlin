package com.acme.transport.freightpay.audit.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the audit exception calculation. */
interface AuditExceptionModifier {
    fun describe(): String
    fun adjust(item: AuditException, value: Double): Double
}

data class RoundingAuditExceptionAdjuster(var step: Double) : AuditExceptionModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AuditException, value: Double): Double = roundTo(value, step)
}

data class TieredAuditExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditExceptionModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAuditExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AuditExceptionModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AuditException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AuditExceptionDurationMinutesOffsetAdjuster(var weight: Double) : AuditExceptionModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: AuditException, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class PercentAuditExceptionAdjuster(var percent: Double) : AuditExceptionModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AuditException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAuditExceptionAdjuster(var minimum: Double) : AuditExceptionModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AuditException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the audit exception steps in order and keeps a trace of each. */
data class AuditExceptionEngine(
    var steps: MutableList<AuditExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AuditException): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditExceptionModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AuditException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAuditExceptionEngine(flags: Registry): AuditExceptionEngine {
    return AuditExceptionEngine(
        steps = mutableListOf<AuditExceptionModifier>(
            RoundingAuditExceptionAdjuster(1.0),
            TieredAuditExceptionAdjuster(
                mutableListOf<Double>(6969.0, 8587.0, 9422.0),
                mutableListOf<Double>(1.17, 1.16, 1.1),
            ),
            FlaggedAuditExceptionAdjuster(flags, "audit-exception-seasonal", -9.6),
            AuditExceptionDurationMinutesOffsetAdjuster(1.43),
            PercentAuditExceptionAdjuster(21.9),
            MinimumAuditExceptionAdjuster(1.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 29593.6,
    )
}
