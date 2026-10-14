package com.acme.transport.network.zone.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone exception is in its life cycle. */
enum class ZoneExceptionStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A zone exception. Its code is unique within a tenant. */
data class ZoneException(
    var code: String,
    var label: String,
    var quantity: Int,
    var margin: Double,
    var pallets: Int,
    var phone: String,
    var archived: Boolean,
    var version: Int,
    var country: String,
    var odometer: Int,
    var routeCode: String,
    var status: ZoneExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneExceptionStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneExceptionStatus): Boolean {
        when (status) {
            ZoneExceptionStatus.PROPOSED -> return next == ZoneExceptionStatus.REJECTED || next == ZoneExceptionStatus.WITHDRAWN
            ZoneExceptionStatus.APPROVED -> {
                when (next) {
                    ZoneExceptionStatus.REJECTED, ZoneExceptionStatus.WITHDRAWN -> return true
                }
                return false
            }
            ZoneExceptionStatus.REJECTED -> {
                when (next) {
                    ZoneExceptionStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneExceptionStatus) {
        if (!canMoveTo(next)) {
            error("zone exception $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"zone exception"} $code"

    fun weightedValue(): Double = round((margin - pallets.toDouble()) * 100) / 100

    fun snapshot(): ZoneException {
        return ZoneException(
            code = code,
            label = label,
            quantity = quantity,
            margin = margin,
            pallets = pallets,
            phone = phone,
            archived = archived,
            version = version,
            country = country,
            odometer = odometer,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a zone exception in status proposed. */
fun newZoneException(code: String, label: String): ZoneException {
    return ZoneException(
        code = code,
        label = label,
        quantity = 0,
        margin = 0.0,
        pallets = 0,
        phone = "",
        archived = false,
        version = 0,
        country = "",
        odometer = 0,
        routeCode = "",
        status = ZoneExceptionStatus.PROPOSED,
    )
}
