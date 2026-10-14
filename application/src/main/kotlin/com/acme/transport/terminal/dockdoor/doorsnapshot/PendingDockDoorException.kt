package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending dock door exception is in its life cycle. */
enum class PendingDockDoorExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One pending dock door exception, with what the planners need to act on it. */
data class PendingDockDoorException(
    var code: String,
    var title: String,
    var phone: String,
    var vehicleId: String,
    var ratio: Double,
    var windowDays: Int,
    var margin: Double,
    var lane: String,
    var status: PendingDockDoorExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDockDoorExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDockDoorExceptionStatus): Boolean {
        when (status) {
            PendingDockDoorExceptionStatus.CREATED -> return next == PendingDockDoorExceptionStatus.PUBLISHED
            PendingDockDoorExceptionStatus.VALIDATED -> {
                when (next) {
                    PendingDockDoorExceptionStatus.PUBLISHED -> return true
                }
                return false
            }
            PendingDockDoorExceptionStatus.PUBLISHED -> return next == PendingDockDoorExceptionStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingDockDoorExceptionStatus) {
        if (!canMoveTo(next)) {
            error("pending dock door exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedValue(): Double = round(windowDays.toDouble() * margin * 100) / 100

    fun snapshot(): PendingDockDoorException {
        return PendingDockDoorException(
            code = code,
            title = title,
            phone = phone,
            vehicleId = vehicleId,
            ratio = ratio,
            windowDays = windowDays,
            margin = margin,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a pending dock door exception in status created. */
fun newPendingDockDoorException(code: String, title: String): PendingDockDoorException {
    return PendingDockDoorException(
        code = code,
        title = title,
        phone = "",
        vehicleId = "",
        ratio = 0.0,
        windowDays = 0,
        margin = 0.0,
        lane = "",
        status = PendingDockDoorExceptionStatus.CREATED,
    )
}
