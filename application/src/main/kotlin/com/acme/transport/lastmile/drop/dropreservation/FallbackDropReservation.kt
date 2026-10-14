package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback drop reservation records of lastmile's drop work, and the rules, stores, and calculations
// around them.

/** Where a fallback drop reservation is in its life cycle. */
enum class FallbackDropReservationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One fallback drop reservation, with what the planners need to act on it. */
data class FallbackDropReservation(
    var code: String,
    var title: String,
    var revision: Int,
    var updatedAt: Int,
    var label: String,
    var timezone: String,
    var category: String,
    var status: FallbackDropReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDropReservationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDropReservationStatus): Boolean {
        when (status) {
            FallbackDropReservationStatus.NEW -> {
                when (next) {
                    FallbackDropReservationStatus.DONE -> return true
                }
                return false
            }
            FallbackDropReservationStatus.QUEUED -> return next == FallbackDropReservationStatus.DONE
            FallbackDropReservationStatus.RUNNING -> {
                when (next) {
                    FallbackDropReservationStatus.DONE, FallbackDropReservationStatus.FAILED -> return true
                }
                return false
            }
            FallbackDropReservationStatus.DONE -> return next == FallbackDropReservationStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackDropReservationStatus) {
        if (!canMoveTo(next)) {
            error("fallback drop reservation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"fallback drop reservation"} $code"

    fun weightedExposure(): Double = round((revision.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackDropReservation {
        return FallbackDropReservation(
            code = code,
            title = title,
            revision = revision,
            updatedAt = updatedAt,
            label = label,
            timezone = timezone,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fallback drop reservation in status new. */
fun newFallbackDropReservation(code: String, title: String): FallbackDropReservation {
    return FallbackDropReservation(
        code = code,
        title = title,
        revision = 0,
        updatedAt = 0,
        label = "",
        timezone = "",
        category = "",
        status = FallbackDropReservationStatus.NEW,
    )
}
