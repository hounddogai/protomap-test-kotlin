package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered audit budget records of freightpay's audit work, and the rules, stores, and calculations around
// them.

/** Where a tiered audit budget is in its life cycle. */
enum class TieredAuditBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tiered audit budget and its current status. */
data class TieredAuditBudget(
    var code: String,
    var title: String,
    var minimum: Double,
    var country: String,
    var quantity: Int,
    var maximum: Double,
    var attempts: Int,
    var name: String,
    var dock: String,
    var status: TieredAuditBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAuditBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAuditBudgetStatus): Boolean {
        when (status) {
            TieredAuditBudgetStatus.REQUESTED -> return next == TieredAuditBudgetStatus.GRANTED || next == TieredAuditBudgetStatus.EXPIRED
            TieredAuditBudgetStatus.GRANTED -> {
                when (next) {
                    TieredAuditBudgetStatus.REVOKED, TieredAuditBudgetStatus.EXPIRED -> return true
                }
                return false
            }
            TieredAuditBudgetStatus.REVOKED -> return next == TieredAuditBudgetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TieredAuditBudgetStatus) {
        if (!canMoveTo(next)) {
            error("tiered audit budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedLoad(): Double = round(maximum * minimum * 100) / 100

    fun snapshot(): TieredAuditBudget {
        return TieredAuditBudget(
            code = code,
            title = title,
            minimum = minimum,
            country = country,
            quantity = quantity,
            maximum = maximum,
            attempts = attempts,
            name = name,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered audit budget in status requested. */
fun newTieredAuditBudget(code: String, title: String): TieredAuditBudget {
    return TieredAuditBudget(
        code = code,
        title = title,
        minimum = 0.0,
        country = "",
        quantity = 0,
        maximum = 0.0,
        attempts = 0,
        name = "",
        dock = "",
        status = TieredAuditBudgetStatus.REQUESTED,
    )
}
