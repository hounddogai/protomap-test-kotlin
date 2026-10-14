package com.acme.transport.linehaul.relay.relayquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled relay rule is in its life cycle. */
enum class ScheduledRelayRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scheduled relay rule as the relay team keeps it. */
data class ScheduledRelayRule(
    var code: String,
    var label: String,
    var revision: Int,
    var etaMinutes: Int,
    var loadKg: Double,
    var budget: Double,
    var graceDays: Int,
    var segment: String,
    var ceiling: Double,
    var status: ScheduledRelayRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRelayRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRelayRuleStatus): Boolean {
        when (status) {
            ScheduledRelayRuleStatus.REQUESTED -> {
                when (next) {
                    ScheduledRelayRuleStatus.GRANTED, ScheduledRelayRuleStatus.REVOKED -> return true
                }
                return false
            }
            ScheduledRelayRuleStatus.GRANTED -> {
                when (next) {
                    ScheduledRelayRuleStatus.REVOKED, ScheduledRelayRuleStatus.EXPIRED -> return true
                }
                return false
            }
            ScheduledRelayRuleStatus.REVOKED -> {
                when (next) {
                    ScheduledRelayRuleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRelayRuleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled relay rule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round((revision.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): ScheduledRelayRule {
        return ScheduledRelayRule(
            code = code,
            label = label,
            revision = revision,
            etaMinutes = etaMinutes,
            loadKg = loadKg,
            budget = budget,
            graceDays = graceDays,
            segment = segment,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled relay rule in status requested. */
fun newScheduledRelayRule(code: String, label: String): ScheduledRelayRule {
    return ScheduledRelayRule(
        code = code,
        label = label,
        revision = 0,
        etaMinutes = 0,
        loadKg = 0.0,
        budget = 0.0,
        graceDays = 0,
        segment = "",
        ceiling = 0.0,
        status = ScheduledRelayRuleStatus.REQUESTED,
    )
}
