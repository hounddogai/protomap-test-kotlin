package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered cutoff reservation is in its life cycle. */
enum class TieredCutoffReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered cutoff reservation. Its code is unique within a tenant. */
data class TieredCutoffReservation(
    var code: String,
    var name: String,
    var odometer: Int,
    var createdAt: Int,
    var shiftCode: String,
    var currency: String,
    var locale: String,
    var title: String,
    var minimum: Double,
    var margin: Double,
    var approved: Boolean,
    var status: TieredCutoffReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCutoffReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCutoffReservationStatus): Boolean {
        when (status) {
            TieredCutoffReservationStatus.OPEN -> {
                when (next) {
                    TieredCutoffReservationStatus.REOPENED -> return true
                }
                return false
            }
            TieredCutoffReservationStatus.IN_REVIEW -> {
                when (next) {
                    TieredCutoffReservationStatus.RESOLVED, TieredCutoffReservationStatus.REOPENED -> return true
                }
                return false
            }
            TieredCutoffReservationStatus.RESOLVED -> {
                when (next) {
                    TieredCutoffReservationStatus.REOPENED, TieredCutoffReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredCutoffReservationStatus.REOPENED -> {
                when (next) {
                    TieredCutoffReservationStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredCutoffReservationStatus) {
        if (!canMoveTo(next)) {
            error("tiered cutoff reservation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered cutoff reservation"} $code"

    fun weightedTotal(): Double = round((margin - createdAt.toDouble()) * 100) / 100

    fun snapshot(): TieredCutoffReservation {
        return TieredCutoffReservation(
            code = code,
            name = name,
            odometer = odometer,
            createdAt = createdAt,
            shiftCode = shiftCode,
            currency = currency,
            locale = locale,
            title = title,
            minimum = minimum,
            margin = margin,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tiered cutoff reservation in status open. */
fun newTieredCutoffReservation(code: String, name: String): TieredCutoffReservation {
    return TieredCutoffReservation(
        code = code,
        name = name,
        odometer = 0,
        createdAt = 0,
        shiftCode = "",
        currency = "",
        locale = "",
        title = "",
        minimum = 0.0,
        margin = 0.0,
        approved = false,
        status = TieredCutoffReservationStatus.OPEN,
    )
}
