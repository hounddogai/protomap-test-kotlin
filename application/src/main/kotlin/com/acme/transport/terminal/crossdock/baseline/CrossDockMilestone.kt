package com.acme.transport.terminal.crossdock.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock milestone is in its life cycle. */
enum class CrossDockMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A cross dock milestone as the cross dock team keeps it. */
data class CrossDockMilestone(
    var code: String,
    var label: String,
    var checksum: String,
    var sequence: Int,
    var vehicleId: String,
    var carrier: String,
    var dock: String,
    var country: String,
    var ceiling: Double,
    var pallets: Int,
    var status: CrossDockMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockMilestoneStatus): Boolean {
        when (status) {
            CrossDockMilestoneStatus.IDLE -> return next == CrossDockMilestoneStatus.READY
            CrossDockMilestoneStatus.WARMING -> return next == CrossDockMilestoneStatus.READY || next == CrossDockMilestoneStatus.STOPPED
            CrossDockMilestoneStatus.READY -> return next == CrossDockMilestoneStatus.STOPPED
            CrossDockMilestoneStatus.DRAINING -> return next == CrossDockMilestoneStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CrossDockMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("cross dock milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"cross dock milestone"} $code"

    fun netTotal(): Double = round((pallets.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): CrossDockMilestone {
        return CrossDockMilestone(
            code = code,
            label = label,
            checksum = checksum,
            sequence = sequence,
            vehicleId = vehicleId,
            carrier = carrier,
            dock = dock,
            country = country,
            ceiling = ceiling,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a cross dock milestone in status idle. */
fun newCrossDockMilestone(code: String, label: String): CrossDockMilestone {
    return CrossDockMilestone(
        code = code,
        label = label,
        checksum = "",
        sequence = 0,
        vehicleId = "",
        carrier = "",
        dock = "",
        country = "",
        ceiling = 0.0,
        pallets = 0,
        status = CrossDockMilestoneStatus.IDLE,
    )
}
