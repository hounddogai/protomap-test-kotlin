package com.acme.transport.emissions.idling.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered idling digest records of emissions's idling work, and the rules, stores, and calculations around
// them.

/** Where a tiered idling digest is in its life cycle. */
enum class TieredIdlingDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tiered idling digest and its current status. */
data class TieredIdlingDigest(
    var code: String,
    var name: String,
    var sequence: Int,
    var locale: String,
    var shiftCode: String,
    var rate: Double,
    var channel: String,
    var graceDays: Int,
    var status: TieredIdlingDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredIdlingDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredIdlingDigestStatus): Boolean {
        when (status) {
            TieredIdlingDigestStatus.OPEN -> {
                when (next) {
                    TieredIdlingDigestStatus.REOPENED -> return true
                }
                return false
            }
            TieredIdlingDigestStatus.IN_REVIEW -> return next == TieredIdlingDigestStatus.RESOLVED
            TieredIdlingDigestStatus.RESOLVED -> return next == TieredIdlingDigestStatus.ARCHIVED
            TieredIdlingDigestStatus.REOPENED -> {
                when (next) {
                    TieredIdlingDigestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredIdlingDigestStatus) {
        if (!canMoveTo(next)) {
            error("tiered idling digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveExposure(): Double = round((sequence.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): TieredIdlingDigest {
        return TieredIdlingDigest(
            code = code,
            name = name,
            sequence = sequence,
            locale = locale,
            shiftCode = shiftCode,
            rate = rate,
            channel = channel,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tiered idling digest in status open. */
fun newTieredIdlingDigest(code: String, name: String): TieredIdlingDigest {
    return TieredIdlingDigest(
        code = code,
        name = name,
        sequence = 0,
        locale = "",
        shiftCode = "",
        rate = 0.0,
        channel = "",
        graceDays = 0,
        status = TieredIdlingDigestStatus.OPEN,
    )
}
