package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer request records of linehaul's trailer work, and the rules, stores, and calculations around
// them.

/** Where a trailer request is in its life cycle. */
enum class TrailerRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One trailer request, with what the planners need to act on it. */
data class TrailerRequest(
    var code: String,
    var title: String,
    var attempts: Int,
    var notes: String,
    var country: String,
    var odometer: Int,
    var durationMinutes: Int,
    var status: TrailerRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerRequestStatus): Boolean {
        when (status) {
            TrailerRequestStatus.REQUESTED -> {
                when (next) {
                    TrailerRequestStatus.REVOKED, TrailerRequestStatus.EXPIRED -> return true
                }
                return false
            }
            TrailerRequestStatus.GRANTED -> {
                when (next) {
                    TrailerRequestStatus.EXPIRED -> return true
                }
                return false
            }
            TrailerRequestStatus.REVOKED -> return next == TrailerRequestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TrailerRequestStatus) {
        if (!canMoveTo(next)) {
            error("trailer request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedVolume(): Double = round((odometer.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): TrailerRequest {
        return TrailerRequest(
            code = code,
            title = title,
            attempts = attempts,
            notes = notes,
            country = country,
            odometer = odometer,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a trailer request in status requested. */
fun newTrailerRequest(code: String, title: String): TrailerRequest {
    return TrailerRequest(
        code = code,
        title = title,
        attempts = 0,
        notes = "",
        country = "",
        odometer = 0,
        durationMinutes = 0,
        status = TrailerRequestStatus.REQUESTED,
    )
}
