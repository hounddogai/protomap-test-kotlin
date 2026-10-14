package com.acme.transport.lastmile.redelivery.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery digest records of lastmile's redelivery work, and the rules, stores, and calculations around
// them.

/** Where a redelivery digest is in its life cycle. */
enum class RedeliveryDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a redelivery digest and its current status. */
data class RedeliveryDigest(
    var code: String,
    var title: String,
    var locale: String,
    var windowDays: Int,
    var distanceKm: Double,
    var budget: Double,
    var status: RedeliveryDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryDigestStatus): Boolean {
        when (status) {
            RedeliveryDigestStatus.OPEN -> {
                when (next) {
                    RedeliveryDigestStatus.IN_REVIEW, RedeliveryDigestStatus.RESOLVED -> return true
                }
                return false
            }
            RedeliveryDigestStatus.IN_REVIEW -> return next == RedeliveryDigestStatus.RESOLVED || next == RedeliveryDigestStatus.ARCHIVED
            RedeliveryDigestStatus.RESOLVED -> return next == RedeliveryDigestStatus.REOPENED
            RedeliveryDigestStatus.REOPENED -> return next == RedeliveryDigestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RedeliveryDigestStatus) {
        if (!canMoveTo(next)) {
            error("redelivery digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedExposure(): Double = round((budget + windowDays.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryDigest {
        return RedeliveryDigest(
            code = code,
            title = title,
            locale = locale,
            windowDays = windowDays,
            distanceKm = distanceKm,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a redelivery digest in status open. */
fun newRedeliveryDigest(code: String, title: String): RedeliveryDigest {
    return RedeliveryDigest(
        code = code,
        title = title,
        locale = "",
        windowDays = 0,
        distanceKm = 0.0,
        budget = 0.0,
        status = RedeliveryDigestStatus.OPEN,
    )
}
