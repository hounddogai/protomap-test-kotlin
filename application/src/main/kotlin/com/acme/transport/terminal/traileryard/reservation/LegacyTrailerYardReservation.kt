package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy trailer yard reservation records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a legacy trailer yard reservation is in its life cycle. */
enum class LegacyTrailerYardReservationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One legacy trailer yard reservation, with what the planners need to act on it. */
data class LegacyTrailerYardReservation(
    var code: String,
    var name: String,
    var rate: Double,
    var summary: String,
    var discount: Double,
    var status: LegacyTrailerYardReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTrailerYardReservationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTrailerYardReservationStatus): Boolean {
        when (status) {
            LegacyTrailerYardReservationStatus.NEW -> {
                when (next) {
                    LegacyTrailerYardReservationStatus.RUNNING -> return true
                }
                return false
            }
            LegacyTrailerYardReservationStatus.QUEUED -> {
                when (next) {
                    LegacyTrailerYardReservationStatus.DONE, LegacyTrailerYardReservationStatus.FAILED -> return true
                }
                return false
            }
            LegacyTrailerYardReservationStatus.RUNNING -> return next == LegacyTrailerYardReservationStatus.FAILED
            LegacyTrailerYardReservationStatus.DONE -> {
                when (next) {
                    LegacyTrailerYardReservationStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyTrailerYardReservationStatus) {
        if (!canMoveTo(next)) {
            error("legacy trailer yard reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netValue(): Double = round((discount + rate) * 100) / 100

    fun snapshot(): LegacyTrailerYardReservation {
        return LegacyTrailerYardReservation(
            code = code,
            name = name,
            rate = rate,
            summary = summary,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy trailer yard reservation in status new. */
fun newLegacyTrailerYardReservation(code: String, name: String): LegacyTrailerYardReservation {
    return LegacyTrailerYardReservation(
        code = code,
        name = name,
        rate = 0.0,
        summary = "",
        discount = 0.0,
        status = LegacyTrailerYardReservationStatus.NEW,
    )
}
