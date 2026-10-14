package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff reservation is in its life cycle. */
enum class CutoffReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A cutoff reservation as the cutoff team keeps it. */
data class CutoffReservation(
    var code: String,
    var label: String,
    var loadKg: Double,
    var category: String,
    var externalRef: String,
    var capacity: Int,
    var attempts: Int,
    var phone: String,
    var segment: String,
    var driverId: String,
    var status: CutoffReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffReservationStatus): Boolean {
        when (status) {
            CutoffReservationStatus.DRAFT -> {
                when (next) {
                    CutoffReservationStatus.PENDING, CutoffReservationStatus.ACTIVE -> return true
                }
                return false
            }
            CutoffReservationStatus.PENDING -> {
                when (next) {
                    CutoffReservationStatus.ACTIVE, CutoffReservationStatus.CLOSED -> return true
                }
                return false
            }
            CutoffReservationStatus.ACTIVE -> {
                when (next) {
                    CutoffReservationStatus.SUSPENDED, CutoffReservationStatus.CLOSED -> return true
                }
                return false
            }
            CutoffReservationStatus.SUSPENDED -> return next == CutoffReservationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CutoffReservationStatus) {
        if (!canMoveTo(next)) {
            error("cutoff reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round((loadKg + capacity.toDouble()) * 100) / 100

    fun snapshot(): CutoffReservation {
        return CutoffReservation(
            code = code,
            label = label,
            loadKg = loadKg,
            category = category,
            externalRef = externalRef,
            capacity = capacity,
            attempts = attempts,
            phone = phone,
            segment = segment,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a cutoff reservation in status draft. */
fun newCutoffReservation(code: String, label: String): CutoffReservation {
    return CutoffReservation(
        code = code,
        label = label,
        loadKg = 0.0,
        category = "",
        externalRef = "",
        capacity = 0,
        attempts = 0,
        phone = "",
        segment = "",
        driverId = "",
        status = CutoffReservationStatus.DRAFT,
    )
}
