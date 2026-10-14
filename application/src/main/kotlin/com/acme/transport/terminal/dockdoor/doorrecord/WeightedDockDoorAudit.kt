package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted dock door audit is in its life cycle. */
enum class WeightedDockDoorAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a weighted dock door audit and its current status. */
data class WeightedDockDoorAudit(
    var code: String,
    var label: String,
    var retries: Int,
    var channel: String,
    var ownerId: String,
    var minimum: Double,
    var segment: String,
    var status: WeightedDockDoorAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDockDoorAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDockDoorAuditStatus): Boolean {
        when (status) {
            WeightedDockDoorAuditStatus.IDLE -> return next == WeightedDockDoorAuditStatus.READY || next == WeightedDockDoorAuditStatus.DRAINING
            WeightedDockDoorAuditStatus.WARMING -> {
                when (next) {
                    WeightedDockDoorAuditStatus.STOPPED -> return true
                }
                return false
            }
            WeightedDockDoorAuditStatus.READY -> return next == WeightedDockDoorAuditStatus.STOPPED
            WeightedDockDoorAuditStatus.DRAINING -> {
                when (next) {
                    WeightedDockDoorAuditStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDockDoorAuditStatus) {
        if (!canMoveTo(next)) {
            error("weighted dock door audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted dock door audit"} $code"

    fun netTotal(): Double = round((minimum - retries.toDouble()) * 100) / 100

    fun snapshot(): WeightedDockDoorAudit {
        return WeightedDockDoorAudit(
            code = code,
            label = label,
            retries = retries,
            channel = channel,
            ownerId = ownerId,
            minimum = minimum,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted dock door audit in status idle. */
fun newWeightedDockDoorAudit(code: String, label: String): WeightedDockDoorAudit {
    return WeightedDockDoorAudit(
        code = code,
        label = label,
        retries = 0,
        channel = "",
        ownerId = "",
        minimum = 0.0,
        segment = "",
        status = WeightedDockDoorAuditStatus.IDLE,
    )
}
