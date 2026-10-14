package com.acme.transport.freightpay.accessorial.accessorialcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial milestone is in its life cycle. */
enum class AccessorialMilestoneStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a accessorial milestone and its current status. */
data class AccessorialMilestone(
    var code: String,
    var name: String,
    var ratio: Double,
    var active: Boolean,
    var durationMinutes: Int,
    var priority: Int,
    var retries: Int,
    var contactEmail: String,
    var unit: String,
    var capacity: Int,
    var status: AccessorialMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialMilestoneStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialMilestoneStatus): Boolean {
        when (status) {
            AccessorialMilestoneStatus.PROPOSED -> {
                when (next) {
                    AccessorialMilestoneStatus.APPROVED, AccessorialMilestoneStatus.WITHDRAWN -> return true
                }
                return false
            }
            AccessorialMilestoneStatus.APPROVED -> {
                when (next) {
                    AccessorialMilestoneStatus.WITHDRAWN -> return true
                }
                return false
            }
            AccessorialMilestoneStatus.REJECTED -> return next == AccessorialMilestoneStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AccessorialMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("accessorial milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round((capacity.toDouble() - ratio) * 100) / 100

    fun snapshot(): AccessorialMilestone {
        return AccessorialMilestone(
            code = code,
            name = name,
            ratio = ratio,
            active = active,
            durationMinutes = durationMinutes,
            priority = priority,
            retries = retries,
            contactEmail = contactEmail,
            unit = unit,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a accessorial milestone in status proposed. */
fun newAccessorialMilestone(code: String, name: String): AccessorialMilestone {
    return AccessorialMilestone(
        code = code,
        name = name,
        ratio = 0.0,
        active = false,
        durationMinutes = 0,
        priority = 0,
        retries = 0,
        contactEmail = "",
        unit = "",
        capacity = 0,
        status = AccessorialMilestoneStatus.PROPOSED,
    )
}
