package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional attempt rule is in its life cycle. */
enum class RegionalAttemptRuleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A regional attempt rule. Its code is unique within a tenant. */
data class RegionalAttemptRule(
    var code: String,
    var name: String,
    var pallets: Int,
    var minimum: Double,
    var dock: String,
    var attempts: Int,
    var category: String,
    var stopCount: Int,
    var status: RegionalAttemptRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalAttemptRuleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalAttemptRuleStatus): Boolean {
        when (status) {
            RegionalAttemptRuleStatus.PLANNED -> return next == RegionalAttemptRuleStatus.SCHEDULED || next == RegionalAttemptRuleStatus.CANCELLED
            RegionalAttemptRuleStatus.SCHEDULED -> {
                when (next) {
                    RegionalAttemptRuleStatus.COMPLETED, RegionalAttemptRuleStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalAttemptRuleStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalAttemptRuleStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalAttemptRuleStatus.COMPLETED -> {
                when (next) {
                    RegionalAttemptRuleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalAttemptRuleStatus) {
        if (!canMoveTo(next)) {
            error("regional attempt rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveTotal(): Double = round((minimum - stopCount.toDouble()) * 100) / 100

    fun snapshot(): RegionalAttemptRule {
        return RegionalAttemptRule(
            code = code,
            name = name,
            pallets = pallets,
            minimum = minimum,
            dock = dock,
            attempts = attempts,
            category = category,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a regional attempt rule in status planned. */
fun newRegionalAttemptRule(code: String, name: String): RegionalAttemptRule {
    return RegionalAttemptRule(
        code = code,
        name = name,
        pallets = 0,
        minimum = 0.0,
        dock = "",
        attempts = 0,
        category = "",
        stopCount = 0,
        status = RegionalAttemptRuleStatus.PLANNED,
    )
}
