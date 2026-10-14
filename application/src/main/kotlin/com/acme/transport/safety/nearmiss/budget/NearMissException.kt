package com.acme.transport.safety.nearmiss.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss exception is in its life cycle. */
enum class NearMissExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A near miss exception. Its code is unique within a tenant. */
data class NearMissException(
    var code: String,
    var name: String,
    var contactEmail: String,
    var limit: Int,
    var attempts: Int,
    var segment: String,
    var amount: Double,
    var pallets: Int,
    var channel: String,
    var status: NearMissExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissExceptionStatus): Boolean {
        when (status) {
            NearMissExceptionStatus.IDLE -> return next == NearMissExceptionStatus.WARMING || next == NearMissExceptionStatus.STOPPED
            NearMissExceptionStatus.WARMING -> return next == NearMissExceptionStatus.READY || next == NearMissExceptionStatus.STOPPED
            NearMissExceptionStatus.READY -> return next == NearMissExceptionStatus.DRAINING || next == NearMissExceptionStatus.STOPPED
            NearMissExceptionStatus.DRAINING -> return next == NearMissExceptionStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: NearMissExceptionStatus) {
        if (!canMoveTo(next)) {
            error("near miss exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"near miss exception"} $code"

    fun effectiveVolume(): Double = round((pallets.toDouble() - amount) * 100) / 100

    fun snapshot(): NearMissException {
        return NearMissException(
            code = code,
            name = name,
            contactEmail = contactEmail,
            limit = limit,
            attempts = attempts,
            segment = segment,
            amount = amount,
            pallets = pallets,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a near miss exception in status idle. */
fun newNearMissException(code: String, name: String): NearMissException {
    return NearMissException(
        code = code,
        name = name,
        contactEmail = "",
        limit = 0,
        attempts = 0,
        segment = "",
        amount = 0.0,
        pallets = 0,
        channel = "",
        status = NearMissExceptionStatus.IDLE,
    )
}
