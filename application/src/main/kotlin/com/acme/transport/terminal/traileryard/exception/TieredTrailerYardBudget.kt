package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered trailer yard budget is in its life cycle. */
enum class TieredTrailerYardBudgetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One tiered trailer yard budget, with what the planners need to act on it. */
data class TieredTrailerYardBudget(
    var code: String,
    var label: String,
    var threshold: Double,
    var updatedAt: Int,
    var ownerId: String,
    var expiresAt: Int,
    var maximum: Double,
    var discount: Double,
    var segment: String,
    var quantity: Int,
    var status: TieredTrailerYardBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTrailerYardBudgetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTrailerYardBudgetStatus): Boolean {
        when (status) {
            TieredTrailerYardBudgetStatus.DRAFT -> {
                when (next) {
                    TieredTrailerYardBudgetStatus.PENDING -> return true
                }
                return false
            }
            TieredTrailerYardBudgetStatus.PENDING -> return next == TieredTrailerYardBudgetStatus.ACTIVE
            TieredTrailerYardBudgetStatus.ACTIVE -> return next == TieredTrailerYardBudgetStatus.SUSPENDED || next == TieredTrailerYardBudgetStatus.CLOSED
            TieredTrailerYardBudgetStatus.SUSPENDED -> return next == TieredTrailerYardBudgetStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TieredTrailerYardBudgetStatus) {
        if (!canMoveTo(next)) {
            error("tiered trailer yard budget $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"tiered trailer yard budget"} $code"

    fun netLoad(): Double = round(quantity.toDouble() * threshold * 100) / 100

    fun snapshot(): TieredTrailerYardBudget {
        return TieredTrailerYardBudget(
            code = code,
            label = label,
            threshold = threshold,
            updatedAt = updatedAt,
            ownerId = ownerId,
            expiresAt = expiresAt,
            maximum = maximum,
            discount = discount,
            segment = segment,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered trailer yard budget in status draft. */
fun newTieredTrailerYardBudget(code: String, label: String): TieredTrailerYardBudget {
    return TieredTrailerYardBudget(
        code = code,
        label = label,
        threshold = 0.0,
        updatedAt = 0,
        ownerId = "",
        expiresAt = 0,
        maximum = 0.0,
        discount = 0.0,
        segment = "",
        quantity = 0,
        status = TieredTrailerYardBudgetStatus.DRAFT,
    )
}
