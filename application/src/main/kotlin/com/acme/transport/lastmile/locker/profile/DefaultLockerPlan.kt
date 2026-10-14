package com.acme.transport.lastmile.locker.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default locker plan is in its life cycle. */
enum class DefaultLockerPlanStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A default locker plan. Its code is unique within a tenant. */
data class DefaultLockerPlan(
    var code: String,
    var title: String,
    var ownerId: String,
    var weight: Double,
    var summary: String,
    var expiresAt: Int,
    var contactEmail: String,
    var timezone: String,
    var maximum: Double,
    var capacity: Int,
    var status: DefaultLockerPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultLockerPlanStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultLockerPlanStatus): Boolean {
        when (status) {
            DefaultLockerPlanStatus.REQUESTED -> return next == DefaultLockerPlanStatus.GRANTED
            DefaultLockerPlanStatus.GRANTED -> {
                when (next) {
                    DefaultLockerPlanStatus.REVOKED -> return true
                }
                return false
            }
            DefaultLockerPlanStatus.REVOKED -> return next == DefaultLockerPlanStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultLockerPlanStatus) {
        if (!canMoveTo(next)) {
            error("default locker plan $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default locker plan"} $code"

    fun effectiveVolume(): Double = round((maximum + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultLockerPlan {
        return DefaultLockerPlan(
            code = code,
            title = title,
            ownerId = ownerId,
            weight = weight,
            summary = summary,
            expiresAt = expiresAt,
            contactEmail = contactEmail,
            timezone = timezone,
            maximum = maximum,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default locker plan in status requested. */
fun newDefaultLockerPlan(code: String, title: String): DefaultLockerPlan {
    return DefaultLockerPlan(
        code = code,
        title = title,
        ownerId = "",
        weight = 0.0,
        summary = "",
        expiresAt = 0,
        contactEmail = "",
        timezone = "",
        maximum = 0.0,
        capacity = 0,
        status = DefaultLockerPlanStatus.REQUESTED,
    )
}
