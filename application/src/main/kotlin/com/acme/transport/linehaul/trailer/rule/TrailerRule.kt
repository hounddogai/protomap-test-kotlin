package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer rule records of linehaul's trailer work, and the rules, stores, and calculations around them.

/** Where a trailer rule is in its life cycle. */
enum class TrailerRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A trailer rule as the trailer team keeps it. */
data class TrailerRule(
    var code: String,
    var label: String,
    var amount: Double,
    var sequence: Int,
    var attempts: Int,
    var status: TrailerRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerRuleStatus): Boolean {
        when (status) {
            TrailerRuleStatus.REQUESTED -> return next == TrailerRuleStatus.GRANTED || next == TrailerRuleStatus.REVOKED
            TrailerRuleStatus.GRANTED -> {
                when (next) {
                    TrailerRuleStatus.REVOKED, TrailerRuleStatus.EXPIRED -> return true
                }
                return false
            }
            TrailerRuleStatus.REVOKED -> return next == TrailerRuleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TrailerRuleStatus) {
        if (!canMoveTo(next)) {
            error("trailer rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedTotal(): Double = round(amount * attempts.toDouble() * 100) / 100

    fun snapshot(): TrailerRule {
        return TrailerRule(
            code = code,
            label = label,
            amount = amount,
            sequence = sequence,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer rule in status requested. */
fun newTrailerRule(code: String, label: String): TrailerRule {
    return TrailerRule(
        code = code,
        label = label,
        amount = 0.0,
        sequence = 0,
        attempts = 0,
        status = TrailerRuleStatus.REQUESTED,
    )
}
