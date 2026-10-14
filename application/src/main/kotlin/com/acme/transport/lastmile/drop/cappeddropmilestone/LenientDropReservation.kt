package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient drop reservation is in its life cycle. */
enum class LenientDropReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One lenient drop reservation, with what the planners need to act on it. */
data class LenientDropReservation(
    var code: String,
    var label: String,
    var batchSize: Int,
    var sequence: Int,
    var routeCode: String,
    var status: LenientDropReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDropReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDropReservationStatus): Boolean {
        when (status) {
            LenientDropReservationStatus.IDLE -> return next == LenientDropReservationStatus.READY || next == LenientDropReservationStatus.DRAINING
            LenientDropReservationStatus.WARMING -> return next == LenientDropReservationStatus.STOPPED
            LenientDropReservationStatus.READY -> {
                when (next) {
                    LenientDropReservationStatus.DRAINING -> return true
                }
                return false
            }
            LenientDropReservationStatus.DRAINING -> return next == LenientDropReservationStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LenientDropReservationStatus) {
        if (!canMoveTo(next)) {
            error("lenient drop reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round(sequence.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): LenientDropReservation {
        return LenientDropReservation(
            code = code,
            label = label,
            batchSize = batchSize,
            sequence = sequence,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient drop reservation in status idle. */
fun newLenientDropReservation(code: String, label: String): LenientDropReservation {
    return LenientDropReservation(
        code = code,
        label = label,
        batchSize = 0,
        sequence = 0,
        routeCode = "",
        status = LenientDropReservationStatus.IDLE,
    )
}
