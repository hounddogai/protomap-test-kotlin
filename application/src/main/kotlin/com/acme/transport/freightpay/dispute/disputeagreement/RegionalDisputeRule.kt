package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional dispute rule is in its life cycle. */
enum class RegionalDisputeRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional dispute rule. Its code is unique within a tenant. */
data class RegionalDisputeRule(
    var code: String,
    var label: String,
    var locale: String,
    var pinned: Boolean,
    var startsAt: Int,
    var floorValue: Double,
    var status: RegionalDisputeRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDisputeRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDisputeRuleStatus): Boolean {
        when (status) {
            RegionalDisputeRuleStatus.CREATED -> {
                when (next) {
                    RegionalDisputeRuleStatus.VALIDATED -> return true
                }
                return false
            }
            RegionalDisputeRuleStatus.VALIDATED -> return next == RegionalDisputeRuleStatus.PUBLISHED
            RegionalDisputeRuleStatus.PUBLISHED -> {
                when (next) {
                    RegionalDisputeRuleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDisputeRuleStatus) {
        if (!canMoveTo(next)) {
            error("regional dispute rule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedExposure(): Double = round((startsAt.toDouble() + floorValue) * 100) / 100

    fun snapshot(): RegionalDisputeRule {
        return RegionalDisputeRule(
            code = code,
            label = label,
            locale = locale,
            pinned = pinned,
            startsAt = startsAt,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a regional dispute rule in status created. */
fun newRegionalDisputeRule(code: String, label: String): RegionalDisputeRule {
    return RegionalDisputeRule(
        code = code,
        label = label,
        locale = "",
        pinned = false,
        startsAt = 0,
        floorValue = 0.0,
        status = RegionalDisputeRuleStatus.CREATED,
    )
}
