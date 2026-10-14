package com.acme.transport.crewing.availability.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability rule records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability rule is in its life cycle. */
enum class AvailabilityRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A availability rule. Its code is unique within a tenant. */
data class AvailabilityRule(
    var code: String,
    var title: String,
    var label: String,
    var depot: String,
    var endsAt: Int,
    var timezone: String,
    var budget: Double,
    var limit: Int,
    var status: AvailabilityRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityRuleStatus): Boolean {
        when (status) {
            AvailabilityRuleStatus.REQUESTED -> {
                when (next) {
                    AvailabilityRuleStatus.REVOKED, AvailabilityRuleStatus.EXPIRED -> return true
                }
                return false
            }
            AvailabilityRuleStatus.GRANTED -> {
                when (next) {
                    AvailabilityRuleStatus.REVOKED -> return true
                }
                return false
            }
            AvailabilityRuleStatus.REVOKED -> return next == AvailabilityRuleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AvailabilityRuleStatus) {
        if (!canMoveTo(next)) {
            error("availability rule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"availability rule"} $code"

    fun effectiveLoad(): Double = round((budget + endsAt.toDouble()) * 100) / 100

    fun snapshot(): AvailabilityRule {
        return AvailabilityRule(
            code = code,
            title = title,
            label = label,
            depot = depot,
            endsAt = endsAt,
            timezone = timezone,
            budget = budget,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a availability rule in status requested. */
fun newAvailabilityRule(code: String, title: String): AvailabilityRule {
    return AvailabilityRule(
        code = code,
        title = title,
        label = "",
        depot = "",
        endsAt = 0,
        timezone = "",
        budget = 0.0,
        limit = 0,
        status = AvailabilityRuleStatus.REQUESTED,
    )
}
