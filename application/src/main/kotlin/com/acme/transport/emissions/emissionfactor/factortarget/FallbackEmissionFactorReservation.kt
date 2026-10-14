package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback emission factor reservation is in its life cycle. */
enum class FallbackEmissionFactorReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fallback emission factor reservation, with what the planners need to act on it. */
data class FallbackEmissionFactorReservation(
    var code: String,
    var name: String,
    var archived: Boolean,
    var etaMinutes: Int,
    var attempts: Int,
    var status: FallbackEmissionFactorReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackEmissionFactorReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackEmissionFactorReservationStatus): Boolean {
        when (status) {
            FallbackEmissionFactorReservationStatus.OPEN -> return next == FallbackEmissionFactorReservationStatus.IN_REVIEW || next == FallbackEmissionFactorReservationStatus.RESOLVED
            FallbackEmissionFactorReservationStatus.IN_REVIEW -> {
                when (next) {
                    FallbackEmissionFactorReservationStatus.RESOLVED, FallbackEmissionFactorReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackEmissionFactorReservationStatus.RESOLVED -> {
                when (next) {
                    FallbackEmissionFactorReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackEmissionFactorReservationStatus.REOPENED -> return next == FallbackEmissionFactorReservationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackEmissionFactorReservationStatus) {
        if (!canMoveTo(next)) {
            error("fallback emission factor reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netTotal(): Double = round((attempts.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): FallbackEmissionFactorReservation {
        return FallbackEmissionFactorReservation(
            code = code,
            name = name,
            archived = archived,
            etaMinutes = etaMinutes,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback emission factor reservation in status open. */
fun newFallbackEmissionFactorReservation(code: String, name: String): FallbackEmissionFactorReservation {
    return FallbackEmissionFactorReservation(
        code = code,
        name = name,
        archived = false,
        etaMinutes = 0,
        attempts = 0,
        status = FallbackEmissionFactorReservationStatus.OPEN,
    )
}
