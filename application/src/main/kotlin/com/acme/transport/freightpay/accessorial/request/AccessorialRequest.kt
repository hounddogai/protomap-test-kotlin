package com.acme.transport.freightpay.accessorial.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The accessorial request records of freightpay's accessorial work, and the rules, stores, and calculations
// around them.

/** Where a accessorial request is in its life cycle. */
enum class AccessorialRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A accessorial request as the accessorial team keeps it. */
data class AccessorialRequest(
    var code: String,
    var name: String,
    var currency: String,
    var locale: String,
    var cost: Double,
    var retries: Int,
    var contactEmail: String,
    var graceDays: Int,
    var trailerId: String,
    var channel: String,
    var attempts: Int,
    var status: AccessorialRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialRequestStatus): Boolean {
        when (status) {
            AccessorialRequestStatus.REQUESTED -> return next == AccessorialRequestStatus.GRANTED || next == AccessorialRequestStatus.EXPIRED
            AccessorialRequestStatus.GRANTED -> {
                when (next) {
                    AccessorialRequestStatus.REVOKED, AccessorialRequestStatus.EXPIRED -> return true
                }
                return false
            }
            AccessorialRequestStatus.REVOKED -> return next == AccessorialRequestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AccessorialRequestStatus) {
        if (!canMoveTo(next)) {
            error("accessorial request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"accessorial request"} $code"

    fun weightedExposure(): Double = round((cost + graceDays.toDouble()) * 100) / 100

    fun snapshot(): AccessorialRequest {
        return AccessorialRequest(
            code = code,
            name = name,
            currency = currency,
            locale = locale,
            cost = cost,
            retries = retries,
            contactEmail = contactEmail,
            graceDays = graceDays,
            trailerId = trailerId,
            channel = channel,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a accessorial request in status requested. */
fun newAccessorialRequest(code: String, name: String): AccessorialRequest {
    return AccessorialRequest(
        code = code,
        name = name,
        currency = "",
        locale = "",
        cost = 0.0,
        retries = 0,
        contactEmail = "",
        graceDays = 0,
        trailerId = "",
        channel = "",
        attempts = 0,
        status = AccessorialRequestStatus.REQUESTED,
    )
}
