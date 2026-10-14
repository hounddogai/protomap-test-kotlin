package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending idling plan is in its life cycle. */
enum class PendingIdlingPlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One pending idling plan, with what the planners need to act on it. */
data class PendingIdlingPlan(
    var code: String,
    var title: String,
    var country: String,
    var driverId: String,
    var pinned: Boolean,
    var updatedAt: Int,
    var fuelLiters: Double,
    var archived: Boolean,
    var status: PendingIdlingPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingIdlingPlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingIdlingPlanStatus): Boolean {
        when (status) {
            PendingIdlingPlanStatus.DRAFT -> return next == PendingIdlingPlanStatus.SUSPENDED || next == PendingIdlingPlanStatus.CLOSED
            PendingIdlingPlanStatus.PENDING -> {
                when (next) {
                    PendingIdlingPlanStatus.SUSPENDED, PendingIdlingPlanStatus.CLOSED -> return true
                }
                return false
            }
            PendingIdlingPlanStatus.ACTIVE -> return next == PendingIdlingPlanStatus.CLOSED
            PendingIdlingPlanStatus.SUSPENDED -> {
                when (next) {
                    PendingIdlingPlanStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingIdlingPlanStatus) {
        if (!canMoveTo(next)) {
            error("pending idling plan $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netVolume(): Double = round(fuelLiters * updatedAt.toDouble() * 100) / 100

    fun snapshot(): PendingIdlingPlan {
        return PendingIdlingPlan(
            code = code,
            title = title,
            country = country,
            driverId = driverId,
            pinned = pinned,
            updatedAt = updatedAt,
            fuelLiters = fuelLiters,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending idling plan in status draft. */
fun newPendingIdlingPlan(code: String, title: String): PendingIdlingPlan {
    return PendingIdlingPlan(
        code = code,
        title = title,
        country = "",
        driverId = "",
        pinned = false,
        updatedAt = 0,
        fuelLiters = 0.0,
        archived = false,
        status = PendingIdlingPlanStatus.DRAFT,
    )
}
