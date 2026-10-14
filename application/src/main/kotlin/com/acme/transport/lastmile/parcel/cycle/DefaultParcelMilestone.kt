package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default parcel milestone is in its life cycle. */
enum class DefaultParcelMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a default parcel milestone and its current status. */
data class DefaultParcelMilestone(
    var code: String,
    var label: String,
    var source: String,
    var contactEmail: String,
    var weight: Double,
    var retries: Int,
    var expiresAt: Int,
    var status: DefaultParcelMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultParcelMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultParcelMilestoneStatus): Boolean {
        when (status) {
            DefaultParcelMilestoneStatus.IDLE -> {
                when (next) {
                    DefaultParcelMilestoneStatus.READY -> return true
                }
                return false
            }
            DefaultParcelMilestoneStatus.WARMING -> return next == DefaultParcelMilestoneStatus.READY || next == DefaultParcelMilestoneStatus.STOPPED
            DefaultParcelMilestoneStatus.READY -> {
                when (next) {
                    DefaultParcelMilestoneStatus.DRAINING, DefaultParcelMilestoneStatus.STOPPED -> return true
                }
                return false
            }
            DefaultParcelMilestoneStatus.DRAINING -> return next == DefaultParcelMilestoneStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultParcelMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("default parcel milestone $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedExposure(): Double = round((weight - retries.toDouble()) * 100) / 100

    fun snapshot(): DefaultParcelMilestone {
        return DefaultParcelMilestone(
            code = code,
            label = label,
            source = source,
            contactEmail = contactEmail,
            weight = weight,
            retries = retries,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a default parcel milestone in status idle. */
fun newDefaultParcelMilestone(code: String, label: String): DefaultParcelMilestone {
    return DefaultParcelMilestone(
        code = code,
        label = label,
        source = "",
        contactEmail = "",
        weight = 0.0,
        retries = 0,
        expiresAt = 0,
        status = DefaultParcelMilestoneStatus.IDLE,
    )
}
