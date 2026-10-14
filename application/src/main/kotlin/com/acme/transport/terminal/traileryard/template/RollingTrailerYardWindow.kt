package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.text.normalize

/** Where a rolling trailer yard window is in its life cycle. */
enum class RollingTrailerYardWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One rolling trailer yard window, with what the planners need to act on it. */
data class RollingTrailerYardWindow(
    var code: String,
    var label: String,
    var channel: String,
    var checksum: String,
    var segment: String,
    var budget: Double,
    var vehicleId: String,
    var status: RollingTrailerYardWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTrailerYardWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTrailerYardWindowStatus): Boolean {
        when (status) {
            RollingTrailerYardWindowStatus.DRAFT -> return next == RollingTrailerYardWindowStatus.SUSPENDED
            RollingTrailerYardWindowStatus.PENDING -> {
                when (next) {
                    RollingTrailerYardWindowStatus.CLOSED -> return true
                }
                return false
            }
            RollingTrailerYardWindowStatus.ACTIVE -> {
                when (next) {
                    RollingTrailerYardWindowStatus.CLOSED -> return true
                }
                return false
            }
            RollingTrailerYardWindowStatus.SUSPENDED -> {
                when (next) {
                    RollingTrailerYardWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTrailerYardWindowStatus) {
        if (!canMoveTo(next)) {
            error("rolling trailer yard window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = budget

    fun snapshot(): RollingTrailerYardWindow {
        return RollingTrailerYardWindow(
            code = code,
            label = label,
            channel = channel,
            checksum = checksum,
            segment = segment,
            budget = budget,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rolling trailer yard window in status draft. */
fun newRollingTrailerYardWindow(code: String, label: String): RollingTrailerYardWindow {
    return RollingTrailerYardWindow(
        code = code,
        label = label,
        channel = "",
        checksum = "",
        segment = "",
        budget = 0.0,
        vehicleId = "",
        status = RollingTrailerYardWindowStatus.DRAFT,
    )
}
