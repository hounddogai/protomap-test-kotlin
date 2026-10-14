package com.acme.transport.freightpay.audit.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict audit rule records of freightpay's audit work, and the rules, stores, and calculations around
// them.

/** Where a strict audit rule is in its life cycle. */
enum class StrictAuditRuleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A strict audit rule as the audit team keeps it. */
data class StrictAuditRule(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var contactEmail: String,
    var priority: Int,
    var carrier: String,
    var attributes: MutableMap<String, String>,
    var budget: Double,
    var channel: String,
    var maximum: Double,
    var status: StrictAuditRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAuditRuleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAuditRuleStatus): Boolean {
        when (status) {
            StrictAuditRuleStatus.PROPOSED -> {
                when (next) {
                    StrictAuditRuleStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictAuditRuleStatus.APPROVED -> {
                when (next) {
                    StrictAuditRuleStatus.REJECTED -> return true
                }
                return false
            }
            StrictAuditRuleStatus.REJECTED -> return next == StrictAuditRuleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictAuditRuleStatus) {
        if (!canMoveTo(next)) {
            error("strict audit rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((maximum - priority.toDouble()) * 100) / 100

    fun snapshot(): StrictAuditRule {
        return StrictAuditRule(
            code = code,
            label = label,
            distanceKm = distanceKm,
            contactEmail = contactEmail,
            priority = priority,
            carrier = carrier,
            attributes = attributes,
            budget = budget,
            channel = channel,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a strict audit rule in status proposed. */
fun newStrictAuditRule(code: String, label: String): StrictAuditRule {
    return StrictAuditRule(
        code = code,
        label = label,
        distanceKm = 0.0,
        contactEmail = "",
        priority = 0,
        carrier = "",
        attributes = mutableMapOf<String, String>(),
        budget = 0.0,
        channel = "",
        maximum = 0.0,
        status = StrictAuditRuleStatus.PROPOSED,
    )
}
