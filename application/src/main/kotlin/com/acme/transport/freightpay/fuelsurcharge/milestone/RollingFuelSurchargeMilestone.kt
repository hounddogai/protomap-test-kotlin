package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling fuel surcharge milestone records of freightpay's fuel surcharge work, and the rules, stores,
// and calculations around them.

/** Where a rolling fuel surcharge milestone is in its life cycle. */
enum class RollingFuelSurchargeMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One rolling fuel surcharge milestone, with what the planners need to act on it. */
data class RollingFuelSurchargeMilestone(
    var code: String,
    var name: String,
    var label: String,
    var distanceKm: Double,
    var budget: Double,
    var contactEmail: String,
    var graceDays: Int,
    var windowDays: Int,
    var status: RollingFuelSurchargeMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFuelSurchargeMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFuelSurchargeMilestoneStatus): Boolean {
        when (status) {
            RollingFuelSurchargeMilestoneStatus.NEW -> {
                when (next) {
                    RollingFuelSurchargeMilestoneStatus.RUNNING, RollingFuelSurchargeMilestoneStatus.FAILED -> return true
                }
                return false
            }
            RollingFuelSurchargeMilestoneStatus.QUEUED -> return next == RollingFuelSurchargeMilestoneStatus.RUNNING || next == RollingFuelSurchargeMilestoneStatus.DONE
            RollingFuelSurchargeMilestoneStatus.RUNNING -> {
                when (next) {
                    RollingFuelSurchargeMilestoneStatus.FAILED -> return true
                }
                return false
            }
            RollingFuelSurchargeMilestoneStatus.DONE -> return next == RollingFuelSurchargeMilestoneStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingFuelSurchargeMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("rolling fuel surcharge milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $name"

    fun adjustedTotal(): Double = round((distanceKm - budget) * 100) / 100

    fun snapshot(): RollingFuelSurchargeMilestone {
        return RollingFuelSurchargeMilestone(
            code = code,
            name = name,
            label = label,
            distanceKm = distanceKm,
            budget = budget,
            contactEmail = contactEmail,
            graceDays = graceDays,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling fuel surcharge milestone in status new. */
fun newRollingFuelSurchargeMilestone(code: String, name: String): RollingFuelSurchargeMilestone {
    return RollingFuelSurchargeMilestone(
        code = code,
        name = name,
        label = "",
        distanceKm = 0.0,
        budget = 0.0,
        contactEmail = "",
        graceDays = 0,
        windowDays = 0,
        status = RollingFuelSurchargeMilestoneStatus.NEW,
    )
}
