package com.acme.transport.linehaul.relay.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the relay audit calculation. */
interface RelayAuditModifier {
    fun describe(): String
    fun adjust(item: RelayAudit, value: Double): Double
}

data class CapRelayAuditAdjuster(var cap: Double) : RelayAuditModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayAudit, value: Double): Double = minOf(value, cap)
}

data class RelayAuditQuantityOffsetAdjuster(var weight: Double) : RelayAuditModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: RelayAudit, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class FlaggedRelayAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayAuditModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRelayAuditAdjuster(var percent: Double) : RelayAuditModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRelayAuditAdjuster(var step: Double) : RelayAuditModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayAudit, value: Double): Double = roundTo(value, step)
}

/** Runs the relay audit steps in order and keeps a trace of each. */
data class RelayAuditEngine(
    var steps: MutableList<RelayAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RelayAudit): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelayAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRelayAuditEngine(flags: Registry): RelayAuditEngine {
    return RelayAuditEngine(
        steps = mutableListOf<RelayAuditModifier>(
            CapRelayAuditAdjuster(13728.0),
            RelayAuditQuantityOffsetAdjuster(1.69),
            FlaggedRelayAuditAdjuster(flags, "relay-audit-strict", 3.4),
            PercentRelayAuditAdjuster(23.9),
            RoundingRelayAuditAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 50928.4,
    )
}
