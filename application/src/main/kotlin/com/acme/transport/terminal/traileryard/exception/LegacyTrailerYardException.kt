package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy trailer yard exception records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a legacy trailer yard exception is in its life cycle. */
enum class LegacyTrailerYardExceptionStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One legacy trailer yard exception, with what the planners need to act on it. */
data class LegacyTrailerYardException(
    var code: String,
    var title: String,
    var externalRef: String,
    var routeCode: String,
    var margin: Double,
    var segment: String,
    var checksum: String,
    var depot: String,
    var retries: Int,
    var durationMinutes: Int,
    var status: LegacyTrailerYardExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTrailerYardExceptionStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTrailerYardExceptionStatus): Boolean {
        when (status) {
            LegacyTrailerYardExceptionStatus.REQUESTED -> {
                when (next) {
                    LegacyTrailerYardExceptionStatus.GRANTED, LegacyTrailerYardExceptionStatus.REVOKED -> return true
                }
                return false
            }
            LegacyTrailerYardExceptionStatus.GRANTED -> return next == LegacyTrailerYardExceptionStatus.EXPIRED
            LegacyTrailerYardExceptionStatus.REVOKED -> return next == LegacyTrailerYardExceptionStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LegacyTrailerYardExceptionStatus) {
        if (!canMoveTo(next)) {
            error("legacy trailer yard exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveValue(): Double = round((retries.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): LegacyTrailerYardException {
        return LegacyTrailerYardException(
            code = code,
            title = title,
            externalRef = externalRef,
            routeCode = routeCode,
            margin = margin,
            segment = segment,
            checksum = checksum,
            depot = depot,
            retries = retries,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a legacy trailer yard exception in status requested. */
fun newLegacyTrailerYardException(code: String, title: String): LegacyTrailerYardException {
    return LegacyTrailerYardException(
        code = code,
        title = title,
        externalRef = "",
        routeCode = "",
        margin = 0.0,
        segment = "",
        checksum = "",
        depot = "",
        retries = 0,
        durationMinutes = 0,
        status = LegacyTrailerYardExceptionStatus.REQUESTED,
    )
}
