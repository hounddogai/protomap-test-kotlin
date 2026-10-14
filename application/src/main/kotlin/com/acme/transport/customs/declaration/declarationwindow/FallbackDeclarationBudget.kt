package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback declaration budget is in its life cycle. */
enum class FallbackDeclarationBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fallback declaration budget. Its code is unique within a tenant. */
data class FallbackDeclarationBudget(
    var code: String,
    var name: String,
    var channel: String,
    var dock: String,
    var version: Int,
    var cost: Double,
    var updatedAt: Int,
    var floorValue: Double,
    var threshold: Double,
    var status: FallbackDeclarationBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDeclarationBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDeclarationBudgetStatus): Boolean {
        when (status) {
            FallbackDeclarationBudgetStatus.REQUESTED -> {
                when (next) {
                    FallbackDeclarationBudgetStatus.GRANTED, FallbackDeclarationBudgetStatus.REVOKED -> return true
                }
                return false
            }
            FallbackDeclarationBudgetStatus.GRANTED -> return next == FallbackDeclarationBudgetStatus.EXPIRED
            FallbackDeclarationBudgetStatus.REVOKED -> return next == FallbackDeclarationBudgetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FallbackDeclarationBudgetStatus) {
        if (!canMoveTo(next)) {
            error("fallback declaration budget $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round((floorValue + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackDeclarationBudget {
        return FallbackDeclarationBudget(
            code = code,
            name = name,
            channel = channel,
            dock = dock,
            version = version,
            cost = cost,
            updatedAt = updatedAt,
            floorValue = floorValue,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fallback declaration budget in status requested. */
fun newFallbackDeclarationBudget(code: String, name: String): FallbackDeclarationBudget {
    return FallbackDeclarationBudget(
        code = code,
        name = name,
        channel = "",
        dock = "",
        version = 0,
        cost = 0.0,
        updatedAt = 0,
        floorValue = 0.0,
        threshold = 0.0,
        status = FallbackDeclarationBudgetStatus.REQUESTED,
    )
}
