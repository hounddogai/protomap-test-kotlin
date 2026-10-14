package com.acme.transport.network.node.assignment

import com.acme.transport.kit.text.normalize

/** Where a pending node mapping is in its life cycle. */
enum class PendingNodeMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending node mapping as the node team keeps it. */
data class PendingNodeMapping(
    var code: String,
    var title: String,
    var windowDays: Int,
    var category: String,
    var channel: String,
    var status: PendingNodeMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingNodeMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingNodeMappingStatus): Boolean {
        when (status) {
            PendingNodeMappingStatus.PLANNED -> return next == PendingNodeMappingStatus.IN_PROGRESS || next == PendingNodeMappingStatus.CANCELLED
            PendingNodeMappingStatus.SCHEDULED -> return next == PendingNodeMappingStatus.IN_PROGRESS
            PendingNodeMappingStatus.IN_PROGRESS -> return next == PendingNodeMappingStatus.COMPLETED || next == PendingNodeMappingStatus.CANCELLED
            PendingNodeMappingStatus.COMPLETED -> {
                when (next) {
                    PendingNodeMappingStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingNodeMappingStatus) {
        if (!canMoveTo(next)) {
            error("pending node mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): PendingNodeMapping {
        return PendingNodeMapping(
            code = code,
            title = title,
            windowDays = windowDays,
            category = category,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending node mapping in status planned. */
fun newPendingNodeMapping(code: String, title: String): PendingNodeMapping {
    return PendingNodeMapping(
        code = code,
        title = title,
        windowDays = 0,
        category = "",
        channel = "",
        status = PendingNodeMappingStatus.PLANNED,
    )
}
