package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback route offset reservation is in its life cycle. */
enum class FallbackRouteOffsetReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a fallback route offset reservation and its current status. */
data class FallbackRouteOffsetReservation(
    var code: String,
    var title: String,
    var archived: Boolean,
    var loadKg: Double,
    var updatedAt: Int,
    var status: FallbackRouteOffsetReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRouteOffsetReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRouteOffsetReservationStatus): Boolean {
        when (status) {
            FallbackRouteOffsetReservationStatus.DRAFT -> {
                when (next) {
                    FallbackRouteOffsetReservationStatus.SUSPENDED -> return true
                }
                return false
            }
            FallbackRouteOffsetReservationStatus.PENDING -> {
                when (next) {
                    FallbackRouteOffsetReservationStatus.SUSPENDED -> return true
                }
                return false
            }
            FallbackRouteOffsetReservationStatus.ACTIVE -> return next == FallbackRouteOffsetReservationStatus.SUSPENDED || next == FallbackRouteOffsetReservationStatus.CLOSED
            FallbackRouteOffsetReservationStatus.SUSPENDED -> {
                when (next) {
                    FallbackRouteOffsetReservationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackRouteOffsetReservationStatus) {
        if (!canMoveTo(next)) {
            error("fallback route offset reservation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback route offset reservation"} $code"

    fun effectiveValue(): Double = round(updatedAt.toDouble() * loadKg * 100) / 100

    fun snapshot(): FallbackRouteOffsetReservation {
        return FallbackRouteOffsetReservation(
            code = code,
            title = title,
            archived = archived,
            loadKg = loadKg,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback route offset reservation in status draft. */
fun newFallbackRouteOffsetReservation(code: String, title: String): FallbackRouteOffsetReservation {
    return FallbackRouteOffsetReservation(
        code = code,
        title = title,
        archived = false,
        loadKg = 0.0,
        updatedAt = 0,
        status = FallbackRouteOffsetReservationStatus.DRAFT,
    )
}
