package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled camera event milestone is in its life cycle. */
enum class ScheduledCameraEventMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scheduled camera event milestone. Its code is unique within a tenant. */
data class ScheduledCameraEventMilestone(
    var code: String,
    var name: String,
    var weight: Double,
    var quantity: Int,
    var shiftCode: String,
    var score: Double,
    var status: ScheduledCameraEventMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCameraEventMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCameraEventMilestoneStatus): Boolean {
        when (status) {
            ScheduledCameraEventMilestoneStatus.REQUESTED -> {
                when (next) {
                    ScheduledCameraEventMilestoneStatus.REVOKED -> return true
                }
                return false
            }
            ScheduledCameraEventMilestoneStatus.GRANTED -> {
                when (next) {
                    ScheduledCameraEventMilestoneStatus.REVOKED, ScheduledCameraEventMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
            ScheduledCameraEventMilestoneStatus.REVOKED -> {
                when (next) {
                    ScheduledCameraEventMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledCameraEventMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("scheduled camera event milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round((score - weight) * 100) / 100

    fun snapshot(): ScheduledCameraEventMilestone {
        return ScheduledCameraEventMilestone(
            code = code,
            name = name,
            weight = weight,
            quantity = quantity,
            shiftCode = shiftCode,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled camera event milestone in status requested. */
fun newScheduledCameraEventMilestone(code: String, name: String): ScheduledCameraEventMilestone {
    return ScheduledCameraEventMilestone(
        code = code,
        name = name,
        weight = 0.0,
        quantity = 0,
        shiftCode = "",
        score = 0.0,
        status = ScheduledCameraEventMilestoneStatus.REQUESTED,
    )
}
