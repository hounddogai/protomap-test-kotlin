package com.acme.transport.fleetops.tire.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tire plan records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a tire plan is in its life cycle. */
enum class TirePlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tire plan and its current status. */
data class TirePlan(
    var code: String,
    var label: String,
    var carrier: String,
    var cost: Double,
    var channel: String,
    var phone: String,
    var title: String,
    var quantity: Int,
    var updatedAt: Int,
    var ownerId: String,
    var limit: Int,
    var status: TirePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TirePlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TirePlanStatus): Boolean {
        when (status) {
            TirePlanStatus.OPEN -> {
                when (next) {
                    TirePlanStatus.IN_REVIEW, TirePlanStatus.REOPENED -> return true
                }
                return false
            }
            TirePlanStatus.IN_REVIEW -> return next == TirePlanStatus.RESOLVED || next == TirePlanStatus.ARCHIVED
            TirePlanStatus.RESOLVED -> return next == TirePlanStatus.ARCHIVED
            TirePlanStatus.REOPENED -> {
                when (next) {
                    TirePlanStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TirePlanStatus) {
        if (!canMoveTo(next)) {
            error("tire plan $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"tire plan"} $code"

    fun adjustedLoad(): Double = round((quantity.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): TirePlan {
        return TirePlan(
            code = code,
            label = label,
            carrier = carrier,
            cost = cost,
            channel = channel,
            phone = phone,
            title = title,
            quantity = quantity,
            updatedAt = updatedAt,
            ownerId = ownerId,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tire plan in status open. */
fun newTirePlan(code: String, label: String): TirePlan {
    return TirePlan(
        code = code,
        label = label,
        carrier = "",
        cost = 0.0,
        channel = "",
        phone = "",
        title = "",
        quantity = 0,
        updatedAt = 0,
        ownerId = "",
        limit = 0,
        status = TirePlanStatus.OPEN,
    )
}
