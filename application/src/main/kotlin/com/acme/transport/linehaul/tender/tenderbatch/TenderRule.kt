package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.text.normalize

/** Where a tender rule is in its life cycle. */
enum class TenderRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tender rule and its current status. */
data class TenderRule(
    var code: String,
    var label: String,
    var reviewer: String,
    var fuelLiters: Double,
    var active: Boolean,
    var status: TenderRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderRuleStatus): Boolean {
        when (status) {
            TenderRuleStatus.REQUESTED -> return next == TenderRuleStatus.GRANTED || next == TenderRuleStatus.REVOKED
            TenderRuleStatus.GRANTED -> return next == TenderRuleStatus.REVOKED || next == TenderRuleStatus.EXPIRED
            TenderRuleStatus.REVOKED -> {
                when (next) {
                    TenderRuleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderRuleStatus) {
        if (!canMoveTo(next)) {
            error("tender rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): TenderRule {
        return TenderRule(
            code = code,
            label = label,
            reviewer = reviewer,
            fuelLiters = fuelLiters,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tender rule in status requested. */
fun newTenderRule(code: String, label: String): TenderRule {
    return TenderRule(
        code = code,
        label = label,
        reviewer = "",
        fuelLiters = 0.0,
        active = false,
        status = TenderRuleStatus.REQUESTED,
    )
}
