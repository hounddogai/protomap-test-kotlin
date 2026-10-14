package com.acme.transport.customs.declaration.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration reservation is in its life cycle. */
enum class DeclarationReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A declaration reservation as the declaration team keeps it. */
data class DeclarationReservation(
    var code: String,
    var label: String,
    var locale: String,
    var trailerId: String,
    var attempts: Int,
    var durationMinutes: Int,
    var title: String,
    var segment: String,
    var currency: String,
    var rate: Double,
    var status: DeclarationReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationReservationStatus): Boolean {
        when (status) {
            DeclarationReservationStatus.DRAFT -> return next == DeclarationReservationStatus.SUSPENDED
            DeclarationReservationStatus.PENDING -> return next == DeclarationReservationStatus.ACTIVE || next == DeclarationReservationStatus.CLOSED
            DeclarationReservationStatus.ACTIVE -> return next == DeclarationReservationStatus.SUSPENDED || next == DeclarationReservationStatus.CLOSED
            DeclarationReservationStatus.SUSPENDED -> return next == DeclarationReservationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DeclarationReservationStatus) {
        if (!canMoveTo(next)) {
            error("declaration reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedVolume(): Double = round(rate * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): DeclarationReservation {
        return DeclarationReservation(
            code = code,
            label = label,
            locale = locale,
            trailerId = trailerId,
            attempts = attempts,
            durationMinutes = durationMinutes,
            title = title,
            segment = segment,
            currency = currency,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a declaration reservation in status draft. */
fun newDeclarationReservation(code: String, label: String): DeclarationReservation {
    return DeclarationReservation(
        code = code,
        label = label,
        locale = "",
        trailerId = "",
        attempts = 0,
        durationMinutes = 0,
        title = "",
        segment = "",
        currency = "",
        rate = 0.0,
        status = DeclarationReservationStatus.DRAFT,
    )
}
