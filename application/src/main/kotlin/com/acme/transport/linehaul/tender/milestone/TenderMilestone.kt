package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tender milestone records of linehaul's tender work, and the rules, stores, and calculations around
// them.

/** Where a tender milestone is in its life cycle. */
enum class TenderMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tender milestone as the tender team keeps it. */
data class TenderMilestone(
    var code: String,
    var label: String,
    var ownerId: String,
    var channel: String,
    var phone: String,
    var unit: String,
    var pallets: Int,
    var title: String,
    var vehicleId: String,
    var attempts: Int,
    var status: TenderMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderMilestoneStatus): Boolean {
        when (status) {
            TenderMilestoneStatus.NEW -> {
                when (next) {
                    TenderMilestoneStatus.QUEUED, TenderMilestoneStatus.RUNNING -> return true
                }
                return false
            }
            TenderMilestoneStatus.QUEUED -> {
                when (next) {
                    TenderMilestoneStatus.DONE, TenderMilestoneStatus.FAILED -> return true
                }
                return false
            }
            TenderMilestoneStatus.RUNNING -> return next == TenderMilestoneStatus.DONE || next == TenderMilestoneStatus.FAILED
            TenderMilestoneStatus.DONE -> return next == TenderMilestoneStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TenderMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("tender milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tender milestone"} $code"

    fun projectedValue(): Double = round((attempts.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): TenderMilestone {
        return TenderMilestone(
            code = code,
            label = label,
            ownerId = ownerId,
            channel = channel,
            phone = phone,
            unit = unit,
            pallets = pallets,
            title = title,
            vehicleId = vehicleId,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tender milestone in status new. */
fun newTenderMilestone(code: String, label: String): TenderMilestone {
    return TenderMilestone(
        code = code,
        label = label,
        ownerId = "",
        channel = "",
        phone = "",
        unit = "",
        pallets = 0,
        title = "",
        vehicleId = "",
        attempts = 0,
        status = TenderMilestoneStatus.NEW,
    )
}
