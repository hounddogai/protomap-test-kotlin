package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay baseline is in its life cycle. */
enum class RelayBaselineStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a relay baseline and its current status. */
data class RelayBaseline(
    var code: String,
    var title: String,
    var capacity: Int,
    var summary: String,
    var ceiling: Double,
    var stopCount: Int,
    var updatedAt: Int,
    var vehicleId: String,
    var active: Boolean,
    var status: RelayBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayBaselineStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayBaselineStatus): Boolean {
        when (status) {
            RelayBaselineStatus.PROPOSED -> return next == RelayBaselineStatus.REJECTED || next == RelayBaselineStatus.WITHDRAWN
            RelayBaselineStatus.APPROVED -> return next == RelayBaselineStatus.REJECTED
            RelayBaselineStatus.REJECTED -> return next == RelayBaselineStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RelayBaselineStatus) {
        if (!canMoveTo(next)) {
            error("relay baseline $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"relay baseline"} $code"

    fun adjustedVolume(): Double = round((updatedAt.toDouble() + ceiling) * 100) / 100

    fun snapshot(): RelayBaseline {
        return RelayBaseline(
            code = code,
            title = title,
            capacity = capacity,
            summary = summary,
            ceiling = ceiling,
            stopCount = stopCount,
            updatedAt = updatedAt,
            vehicleId = vehicleId,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a relay baseline in status proposed. */
fun newRelayBaseline(code: String, title: String): RelayBaseline {
    return RelayBaseline(
        code = code,
        title = title,
        capacity = 0,
        summary = "",
        ceiling = 0.0,
        stopCount = 0,
        updatedAt = 0,
        vehicleId = "",
        active = false,
        status = RelayBaselineStatus.PROPOSED,
    )
}
