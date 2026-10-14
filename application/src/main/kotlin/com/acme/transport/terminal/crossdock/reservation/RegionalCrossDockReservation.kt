package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional cross dock reservation records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a regional cross dock reservation is in its life cycle. */
enum class RegionalCrossDockReservationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A regional cross dock reservation. Its code is unique within a tenant. */
data class RegionalCrossDockReservation(
    var code: String,
    var title: String,
    var reviewer: String,
    var sequence: Int,
    var revision: Int,
    var distanceKm: Double,
    var source: String,
    var status: RegionalCrossDockReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCrossDockReservationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCrossDockReservationStatus): Boolean {
        when (status) {
            RegionalCrossDockReservationStatus.PROPOSED -> return next == RegionalCrossDockReservationStatus.REJECTED || next == RegionalCrossDockReservationStatus.WITHDRAWN
            RegionalCrossDockReservationStatus.APPROVED -> {
                when (next) {
                    RegionalCrossDockReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalCrossDockReservationStatus.REJECTED -> {
                when (next) {
                    RegionalCrossDockReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCrossDockReservationStatus) {
        if (!canMoveTo(next)) {
            error("regional cross dock reservation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun projectedTotal(): Double = round(distanceKm * revision.toDouble() * 100) / 100

    fun snapshot(): RegionalCrossDockReservation {
        return RegionalCrossDockReservation(
            code = code,
            title = title,
            reviewer = reviewer,
            sequence = sequence,
            revision = revision,
            distanceKm = distanceKm,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional cross dock reservation in status proposed. */
fun newRegionalCrossDockReservation(code: String, title: String): RegionalCrossDockReservation {
    return RegionalCrossDockReservation(
        code = code,
        title = title,
        reviewer = "",
        sequence = 0,
        revision = 0,
        distanceKm = 0.0,
        source = "",
        status = RegionalCrossDockReservationStatus.PROPOSED,
    )
}
