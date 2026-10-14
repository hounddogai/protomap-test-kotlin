package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default dock door segment is in its life cycle. */
enum class DefaultDockDoorSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A default dock door segment. Its code is unique within a tenant. */
data class DefaultDockDoorSegment(
    var code: String,
    var title: String,
    var dock: String,
    var channel: String,
    var threshold: Double,
    var vehicleId: String,
    var revision: Int,
    var status: DefaultDockDoorSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDockDoorSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDockDoorSegmentStatus): Boolean {
        when (status) {
            DefaultDockDoorSegmentStatus.PROPOSED -> {
                when (next) {
                    DefaultDockDoorSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultDockDoorSegmentStatus.APPROVED -> return next == DefaultDockDoorSegmentStatus.WITHDRAWN
            DefaultDockDoorSegmentStatus.REJECTED -> {
                when (next) {
                    DefaultDockDoorSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDockDoorSegmentStatus) {
        if (!canMoveTo(next)) {
            error("default dock door segment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedTotal(): Double = round(revision.toDouble() * threshold * 100) / 100

    fun snapshot(): DefaultDockDoorSegment {
        return DefaultDockDoorSegment(
            code = code,
            title = title,
            dock = dock,
            channel = channel,
            threshold = threshold,
            vehicleId = vehicleId,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a default dock door segment in status proposed. */
fun newDefaultDockDoorSegment(code: String, title: String): DefaultDockDoorSegment {
    return DefaultDockDoorSegment(
        code = code,
        title = title,
        dock = "",
        channel = "",
        threshold = 0.0,
        vehicleId = "",
        revision = 0,
        status = DefaultDockDoorSegmentStatus.PROPOSED,
    )
}
