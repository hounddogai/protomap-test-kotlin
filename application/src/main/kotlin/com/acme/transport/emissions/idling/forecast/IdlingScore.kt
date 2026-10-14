package com.acme.transport.emissions.idling.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling score is in its life cycle. */
enum class IdlingScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a idling score and its current status. */
data class IdlingScore(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var checksum: String,
    var driverId: String,
    var fuelLiters: Double,
    var sequence: Int,
    var contactEmail: String,
    var status: IdlingScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingScoreStatus): Boolean {
        when (status) {
            IdlingScoreStatus.DRAFT -> return next == IdlingScoreStatus.PENDING || next == IdlingScoreStatus.CLOSED
            IdlingScoreStatus.PENDING -> return next == IdlingScoreStatus.ACTIVE || next == IdlingScoreStatus.CLOSED
            IdlingScoreStatus.ACTIVE -> {
                when (next) {
                    IdlingScoreStatus.SUSPENDED, IdlingScoreStatus.CLOSED -> return true
                }
                return false
            }
            IdlingScoreStatus.SUSPENDED -> return next == IdlingScoreStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: IdlingScoreStatus) {
        if (!canMoveTo(next)) {
            error("idling score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedValue(): Double = round((distanceKm + sequence.toDouble()) * 100) / 100

    fun snapshot(): IdlingScore {
        return IdlingScore(
            code = code,
            label = label,
            distanceKm = distanceKm,
            checksum = checksum,
            driverId = driverId,
            fuelLiters = fuelLiters,
            sequence = sequence,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a idling score in status draft. */
fun newIdlingScore(code: String, label: String): IdlingScore {
    return IdlingScore(
        code = code,
        label = label,
        distanceKm = 0.0,
        checksum = "",
        driverId = "",
        fuelLiters = 0.0,
        sequence = 0,
        contactEmail = "",
        status = IdlingScoreStatus.DRAFT,
    )
}
