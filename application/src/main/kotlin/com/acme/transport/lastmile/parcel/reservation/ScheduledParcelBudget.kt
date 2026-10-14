package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled parcel budget is in its life cycle. */
enum class ScheduledParcelBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One scheduled parcel budget, with what the planners need to act on it. */
data class ScheduledParcelBudget(
    var code: String,
    var title: String,
    var locale: String,
    var createdAt: Int,
    var trailerId: String,
    var updatedAt: Int,
    var distanceKm: Double,
    var score: Double,
    var active: Boolean,
    var version: Int,
    var startsAt: Int,
    var status: ScheduledParcelBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledParcelBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledParcelBudgetStatus): Boolean {
        when (status) {
            ScheduledParcelBudgetStatus.REQUESTED -> {
                when (next) {
                    ScheduledParcelBudgetStatus.GRANTED, ScheduledParcelBudgetStatus.REVOKED -> return true
                }
                return false
            }
            ScheduledParcelBudgetStatus.GRANTED -> return next == ScheduledParcelBudgetStatus.REVOKED
            ScheduledParcelBudgetStatus.REVOKED -> {
                when (next) {
                    ScheduledParcelBudgetStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledParcelBudgetStatus) {
        if (!canMoveTo(next)) {
            error("scheduled parcel budget $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $title"

    fun netValue(): Double = round((updatedAt.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledParcelBudget {
        return ScheduledParcelBudget(
            code = code,
            title = title,
            locale = locale,
            createdAt = createdAt,
            trailerId = trailerId,
            updatedAt = updatedAt,
            distanceKm = distanceKm,
            score = score,
            active = active,
            version = version,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scheduled parcel budget in status requested. */
fun newScheduledParcelBudget(code: String, title: String): ScheduledParcelBudget {
    return ScheduledParcelBudget(
        code = code,
        title = title,
        locale = "",
        createdAt = 0,
        trailerId = "",
        updatedAt = 0,
        distanceKm = 0.0,
        score = 0.0,
        active = false,
        version = 0,
        startsAt = 0,
        status = ScheduledParcelBudgetStatus.REQUESTED,
    )
}
