package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling fuel burn request records of emissions's fuel burn work, and the rules, stores, and
// calculations around them.

/** Where a rolling fuel burn request is in its life cycle. */
enum class RollingFuelBurnRequestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One rolling fuel burn request, with what the planners need to act on it. */
data class RollingFuelBurnRequest(
    var code: String,
    var label: String,
    var ratio: Double,
    var channel: String,
    var ownerId: String,
    var batchSize: Int,
    var graceDays: Int,
    var shiftCode: String,
    var status: RollingFuelBurnRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFuelBurnRequestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFuelBurnRequestStatus): Boolean {
        when (status) {
            RollingFuelBurnRequestStatus.OPEN -> return next == RollingFuelBurnRequestStatus.RESOLVED || next == RollingFuelBurnRequestStatus.ARCHIVED
            RollingFuelBurnRequestStatus.IN_REVIEW -> return next == RollingFuelBurnRequestStatus.REOPENED
            RollingFuelBurnRequestStatus.RESOLVED -> {
                when (next) {
                    RollingFuelBurnRequestStatus.REOPENED -> return true
                }
                return false
            }
            RollingFuelBurnRequestStatus.REOPENED -> return next == RollingFuelBurnRequestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingFuelBurnRequestStatus) {
        if (!canMoveTo(next)) {
            error("rolling fuel burn request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netValue(): Double = round((batchSize.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): RollingFuelBurnRequest {
        return RollingFuelBurnRequest(
            code = code,
            label = label,
            ratio = ratio,
            channel = channel,
            ownerId = ownerId,
            batchSize = batchSize,
            graceDays = graceDays,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rolling fuel burn request in status open. */
fun newRollingFuelBurnRequest(code: String, label: String): RollingFuelBurnRequest {
    return RollingFuelBurnRequest(
        code = code,
        label = label,
        ratio = 0.0,
        channel = "",
        ownerId = "",
        batchSize = 0,
        graceDays = 0,
        shiftCode = "",
        status = RollingFuelBurnRequestStatus.OPEN,
    )
}
