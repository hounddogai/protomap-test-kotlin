package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dock door rule records of terminal's dock door work, and the rules, stores, and calculations around
// them.

/** Where a dock door rule is in its life cycle. */
enum class DockDoorRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a dock door rule and its current status. */
data class DockDoorRule(
    var code: String,
    var name: String,
    var attempts: Int,
    var currency: String,
    var retries: Int,
    var status: DockDoorRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorRuleStatus): Boolean {
        when (status) {
            DockDoorRuleStatus.REQUESTED -> {
                when (next) {
                    DockDoorRuleStatus.GRANTED, DockDoorRuleStatus.REVOKED -> return true
                }
                return false
            }
            DockDoorRuleStatus.GRANTED -> {
                when (next) {
                    DockDoorRuleStatus.REVOKED, DockDoorRuleStatus.EXPIRED -> return true
                }
                return false
            }
            DockDoorRuleStatus.REVOKED -> return next == DockDoorRuleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DockDoorRuleStatus) {
        if (!canMoveTo(next)) {
            error("dock door rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dock door rule"} $code"

    fun effectiveTotal(): Double = round((attempts.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): DockDoorRule {
        return DockDoorRule(
            code = code,
            name = name,
            attempts = attempts,
            currency = currency,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dock door rule in status requested. */
fun newDockDoorRule(code: String, name: String): DockDoorRule {
    return DockDoorRule(
        code = code,
        name = name,
        attempts = 0,
        currency = "",
        retries = 0,
        status = DockDoorRuleStatus.REQUESTED,
    )
}
