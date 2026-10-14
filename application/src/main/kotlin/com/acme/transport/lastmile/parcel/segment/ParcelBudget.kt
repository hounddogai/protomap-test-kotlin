package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel budget is in its life cycle. */
enum class ParcelBudgetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A parcel budget as the parcel team keeps it. */
data class ParcelBudget(
    var code: String,
    var name: String,
    var maximum: Double,
    var createdAt: Int,
    var quantity: Int,
    var attributes: MutableMap<String, String>,
    var status: ParcelBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelBudgetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelBudgetStatus): Boolean {
        when (status) {
            ParcelBudgetStatus.PROPOSED -> {
                when (next) {
                    ParcelBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
            ParcelBudgetStatus.APPROVED -> {
                when (next) {
                    ParcelBudgetStatus.REJECTED -> return true
                }
                return false
            }
            ParcelBudgetStatus.REJECTED -> return next == ParcelBudgetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ParcelBudgetStatus) {
        if (!canMoveTo(next)) {
            error("parcel budget $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round((maximum + createdAt.toDouble()) * 100) / 100

    fun snapshot(): ParcelBudget {
        return ParcelBudget(
            code = code,
            name = name,
            maximum = maximum,
            createdAt = createdAt,
            quantity = quantity,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a parcel budget in status proposed. */
fun newParcelBudget(code: String, name: String): ParcelBudget {
    return ParcelBudget(
        code = code,
        name = name,
        maximum = 0.0,
        createdAt = 0,
        quantity = 0,
        attributes = mutableMapOf<String, String>(),
        status = ParcelBudgetStatus.PROPOSED,
    )
}
