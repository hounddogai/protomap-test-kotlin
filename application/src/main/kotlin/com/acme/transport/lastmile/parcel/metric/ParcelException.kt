package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel exception is in its life cycle. */
enum class ParcelExceptionStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a parcel exception and its current status. */
data class ParcelException(
    var code: String,
    var label: String,
    var pallets: Int,
    var loadKg: Double,
    var shiftCode: String,
    var expiresAt: Int,
    var dock: String,
    var currency: String,
    var status: ParcelExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelExceptionStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelExceptionStatus): Boolean {
        when (status) {
            ParcelExceptionStatus.REQUESTED -> return next == ParcelExceptionStatus.REVOKED
            ParcelExceptionStatus.GRANTED -> {
                when (next) {
                    ParcelExceptionStatus.REVOKED, ParcelExceptionStatus.EXPIRED -> return true
                }
                return false
            }
            ParcelExceptionStatus.REVOKED -> {
                when (next) {
                    ParcelExceptionStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelExceptionStatus) {
        if (!canMoveTo(next)) {
            error("parcel exception $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedLoad(): Double = round((pallets.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ParcelException {
        return ParcelException(
            code = code,
            label = label,
            pallets = pallets,
            loadKg = loadKg,
            shiftCode = shiftCode,
            expiresAt = expiresAt,
            dock = dock,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a parcel exception in status requested. */
fun newParcelException(code: String, label: String): ParcelException {
    return ParcelException(
        code = code,
        label = label,
        pallets = 0,
        loadKg = 0.0,
        shiftCode = "",
        expiresAt = 0,
        dock = "",
        currency = "",
        status = ParcelExceptionStatus.REQUESTED,
    )
}
