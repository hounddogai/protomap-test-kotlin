package com.acme.transport.fleetops.tire.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending tire schedule is in its life cycle. */
enum class PendingTireScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One pending tire schedule, with what the planners need to act on it. */
data class PendingTireSchedule(
    var code: String,
    var label: String,
    var ownerId: String,
    var visible: Boolean,
    var distanceKm: Double,
    var timezone: String,
    var quantity: Int,
    var cost: Double,
    var createdAt: Int,
    var externalRef: String,
    var status: PendingTireScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTireScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTireScheduleStatus): Boolean {
        when (status) {
            PendingTireScheduleStatus.CREATED -> return next == PendingTireScheduleStatus.PUBLISHED
            PendingTireScheduleStatus.VALIDATED -> return next == PendingTireScheduleStatus.RETIRED
            PendingTireScheduleStatus.PUBLISHED -> {
                when (next) {
                    PendingTireScheduleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingTireScheduleStatus) {
        if (!canMoveTo(next)) {
            error("pending tire schedule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveVolume(): Double = round((cost - distanceKm) * 100) / 100

    fun snapshot(): PendingTireSchedule {
        return PendingTireSchedule(
            code = code,
            label = label,
            ownerId = ownerId,
            visible = visible,
            distanceKm = distanceKm,
            timezone = timezone,
            quantity = quantity,
            cost = cost,
            createdAt = createdAt,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending tire schedule in status created. */
fun newPendingTireSchedule(code: String, label: String): PendingTireSchedule {
    return PendingTireSchedule(
        code = code,
        label = label,
        ownerId = "",
        visible = false,
        distanceKm = 0.0,
        timezone = "",
        quantity = 0,
        cost = 0.0,
        createdAt = 0,
        externalRef = "",
        status = PendingTireScheduleStatus.CREATED,
    )
}
