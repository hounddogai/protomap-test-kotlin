package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.text.normalize

/** Where a transit time exception is in its life cycle. */
enum class TransitTimeExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a transit time exception and its current status. */
data class TransitTimeException(
    var code: String,
    var label: String,
    var currency: String,
    var approved: Boolean,
    var carrier: String,
    var checksum: String,
    var batchSize: Int,
    var shiftCode: String,
    var status: TransitTimeExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeExceptionStatus): Boolean {
        when (status) {
            TransitTimeExceptionStatus.NEW -> {
                when (next) {
                    TransitTimeExceptionStatus.RUNNING -> return true
                }
                return false
            }
            TransitTimeExceptionStatus.QUEUED -> return next == TransitTimeExceptionStatus.FAILED
            TransitTimeExceptionStatus.RUNNING -> {
                when (next) {
                    TransitTimeExceptionStatus.DONE, TransitTimeExceptionStatus.FAILED -> return true
                }
                return false
            }
            TransitTimeExceptionStatus.DONE -> return next == TransitTimeExceptionStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TransitTimeExceptionStatus) {
        if (!canMoveTo(next)) {
            error("transit time exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): TransitTimeException {
        return TransitTimeException(
            code = code,
            label = label,
            currency = currency,
            approved = approved,
            carrier = carrier,
            checksum = checksum,
            batchSize = batchSize,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a transit time exception in status new. */
fun newTransitTimeException(code: String, label: String): TransitTimeException {
    return TransitTimeException(
        code = code,
        label = label,
        currency = "",
        approved = false,
        carrier = "",
        checksum = "",
        batchSize = 0,
        shiftCode = "",
        status = TransitTimeExceptionStatus.NEW,
    )
}
