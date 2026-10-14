package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul plan is in its life cycle. */
enum class BackhaulPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One backhaul plan, with what the planners need to act on it. */
data class BackhaulPlan(
    var code: String,
    var title: String,
    var startsAt: Int,
    var fuelLiters: Double,
    var dock: String,
    var pinned: Boolean,
    var description: String,
    var distanceKm: Double,
    var budget: Double,
    var updatedAt: Int,
    var discount: Double,
    var status: BackhaulPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulPlanStatus): Boolean {
        when (status) {
            BackhaulPlanStatus.PLANNED -> {
                when (next) {
                    BackhaulPlanStatus.IN_PROGRESS -> return true
                }
                return false
            }
            BackhaulPlanStatus.SCHEDULED -> {
                when (next) {
                    BackhaulPlanStatus.CANCELLED -> return true
                }
                return false
            }
            BackhaulPlanStatus.IN_PROGRESS -> return next == BackhaulPlanStatus.COMPLETED
            BackhaulPlanStatus.COMPLETED -> {
                when (next) {
                    BackhaulPlanStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BackhaulPlanStatus) {
        if (!canMoveTo(next)) {
            error("backhaul plan $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netVolume(): Double = round((fuelLiters + budget) * 100) / 100

    fun snapshot(): BackhaulPlan {
        return BackhaulPlan(
            code = code,
            title = title,
            startsAt = startsAt,
            fuelLiters = fuelLiters,
            dock = dock,
            pinned = pinned,
            description = description,
            distanceKm = distanceKm,
            budget = budget,
            updatedAt = updatedAt,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a backhaul plan in status planned. */
fun newBackhaulPlan(code: String, title: String): BackhaulPlan {
    return BackhaulPlan(
        code = code,
        title = title,
        startsAt = 0,
        fuelLiters = 0.0,
        dock = "",
        pinned = false,
        description = "",
        distanceKm = 0.0,
        budget = 0.0,
        updatedAt = 0,
        discount = 0.0,
        status = BackhaulPlanStatus.PLANNED,
    )
}
