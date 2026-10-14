package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending declaration reservation is in its life cycle. */
enum class PendingDeclarationReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending declaration reservation as the declaration team keeps it. */
data class PendingDeclarationReservation(
    var code: String,
    var label: String,
    var graceDays: Int,
    var updatedAt: Int,
    var rate: Double,
    var driverId: String,
    var discount: Double,
    var stopCount: Int,
    var limit: Int,
    var minimum: Double,
    var weight: Double,
    var status: PendingDeclarationReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDeclarationReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDeclarationReservationStatus): Boolean {
        when (status) {
            PendingDeclarationReservationStatus.REQUESTED -> return next == PendingDeclarationReservationStatus.GRANTED || next == PendingDeclarationReservationStatus.EXPIRED
            PendingDeclarationReservationStatus.GRANTED -> return next == PendingDeclarationReservationStatus.REVOKED || next == PendingDeclarationReservationStatus.EXPIRED
            PendingDeclarationReservationStatus.REVOKED -> return next == PendingDeclarationReservationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingDeclarationReservationStatus) {
        if (!canMoveTo(next)) {
            error("pending declaration reservation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedExposure(): Double = round((updatedAt.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): PendingDeclarationReservation {
        return PendingDeclarationReservation(
            code = code,
            label = label,
            graceDays = graceDays,
            updatedAt = updatedAt,
            rate = rate,
            driverId = driverId,
            discount = discount,
            stopCount = stopCount,
            limit = limit,
            minimum = minimum,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending declaration reservation in status requested. */
fun newPendingDeclarationReservation(code: String, label: String): PendingDeclarationReservation {
    return PendingDeclarationReservation(
        code = code,
        label = label,
        graceDays = 0,
        updatedAt = 0,
        rate = 0.0,
        driverId = "",
        discount = 0.0,
        stopCount = 0,
        limit = 0,
        minimum = 0.0,
        weight = 0.0,
        status = PendingDeclarationReservationStatus.REQUESTED,
    )
}
