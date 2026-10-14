package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time rule is in its life cycle. */
enum class TransitTimeRuleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a transit time rule and its current status. */
data class TransitTimeRule(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var expiresAt: Int,
    var windowDays: Int,
    var graceDays: Int,
    var active: Boolean,
    var cost: Double,
    var status: TransitTimeRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeRuleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeRuleStatus): Boolean {
        when (status) {
            TransitTimeRuleStatus.OPEN -> return next == TransitTimeRuleStatus.IN_REVIEW || next == TransitTimeRuleStatus.REOPENED
            TransitTimeRuleStatus.IN_REVIEW -> {
                when (next) {
                    TransitTimeRuleStatus.REOPENED, TransitTimeRuleStatus.ARCHIVED -> return true
                }
                return false
            }
            TransitTimeRuleStatus.RESOLVED -> {
                when (next) {
                    TransitTimeRuleStatus.REOPENED -> return true
                }
                return false
            }
            TransitTimeRuleStatus.REOPENED -> return next == TransitTimeRuleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TransitTimeRuleStatus) {
        if (!canMoveTo(next)) {
            error("transit time rule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedExposure(): Double = round((etaMinutes.toDouble() - cost) * 100) / 100

    fun snapshot(): TransitTimeRule {
        return TransitTimeRule(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            expiresAt = expiresAt,
            windowDays = windowDays,
            graceDays = graceDays,
            active = active,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a transit time rule in status open. */
fun newTransitTimeRule(code: String, name: String): TransitTimeRule {
    return TransitTimeRule(
        code = code,
        name = name,
        etaMinutes = 0,
        expiresAt = 0,
        windowDays = 0,
        graceDays = 0,
        active = false,
        cost = 0.0,
        status = TransitTimeRuleStatus.OPEN,
    )
}
