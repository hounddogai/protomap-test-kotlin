package com.acme.transport.linehaul.tender.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary tender exception is in its life cycle. */
enum class PrimaryTenderExceptionStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One primary tender exception, with what the planners need to act on it. */
data class PrimaryTenderException(
    var code: String,
    var title: String,
    var attempts: Int,
    var retries: Int,
    var country: String,
    var status: PrimaryTenderExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTenderExceptionStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTenderExceptionStatus): Boolean {
        when (status) {
            PrimaryTenderExceptionStatus.REQUESTED -> {
                when (next) {
                    PrimaryTenderExceptionStatus.GRANTED -> return true
                }
                return false
            }
            PrimaryTenderExceptionStatus.GRANTED -> return next == PrimaryTenderExceptionStatus.EXPIRED
            PrimaryTenderExceptionStatus.REVOKED -> {
                when (next) {
                    PrimaryTenderExceptionStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTenderExceptionStatus) {
        if (!canMoveTo(next)) {
            error("primary tender exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary tender exception"} $code"

    fun effectiveExposure(): Double = round((retries.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): PrimaryTenderException {
        return PrimaryTenderException(
            code = code,
            title = title,
            attempts = attempts,
            retries = retries,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary tender exception in status requested. */
fun newPrimaryTenderException(code: String, title: String): PrimaryTenderException {
    return PrimaryTenderException(
        code = code,
        title = title,
        attempts = 0,
        retries = 0,
        country = "",
        status = PrimaryTenderExceptionStatus.REQUESTED,
    )
}
