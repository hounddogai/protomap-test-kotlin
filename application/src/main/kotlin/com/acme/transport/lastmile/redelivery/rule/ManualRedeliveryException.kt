package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual redelivery exception is in its life cycle. */
enum class ManualRedeliveryExceptionStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a manual redelivery exception and its current status. */
data class ManualRedeliveryException(
    var code: String,
    var name: String,
    var locale: String,
    var vehicleId: String,
    var rate: Double,
    var carrier: String,
    var timezone: String,
    var depot: String,
    var capacity: Int,
    var region: String,
    var status: ManualRedeliveryExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRedeliveryExceptionStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRedeliveryExceptionStatus): Boolean {
        when (status) {
            ManualRedeliveryExceptionStatus.OPEN -> return next == ManualRedeliveryExceptionStatus.REOPENED || next == ManualRedeliveryExceptionStatus.ARCHIVED
            ManualRedeliveryExceptionStatus.IN_REVIEW -> {
                when (next) {
                    ManualRedeliveryExceptionStatus.REOPENED, ManualRedeliveryExceptionStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualRedeliveryExceptionStatus.RESOLVED -> return next == ManualRedeliveryExceptionStatus.REOPENED || next == ManualRedeliveryExceptionStatus.ARCHIVED
            ManualRedeliveryExceptionStatus.REOPENED -> return next == ManualRedeliveryExceptionStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ManualRedeliveryExceptionStatus) {
        if (!canMoveTo(next)) {
            error("manual redelivery exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netExposure(): Double = round((rate - capacity.toDouble()) * 100) / 100

    fun snapshot(): ManualRedeliveryException {
        return ManualRedeliveryException(
            code = code,
            name = name,
            locale = locale,
            vehicleId = vehicleId,
            rate = rate,
            carrier = carrier,
            timezone = timezone,
            depot = depot,
            capacity = capacity,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual redelivery exception in status open. */
fun newManualRedeliveryException(code: String, name: String): ManualRedeliveryException {
    return ManualRedeliveryException(
        code = code,
        name = name,
        locale = "",
        vehicleId = "",
        rate = 0.0,
        carrier = "",
        timezone = "",
        depot = "",
        capacity = 0,
        region = "",
        status = ManualRedeliveryExceptionStatus.OPEN,
    )
}
