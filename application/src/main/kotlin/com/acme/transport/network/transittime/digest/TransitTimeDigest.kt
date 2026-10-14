package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The transit time digest records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time digest is in its life cycle. */
enum class TransitTimeDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One transit time digest, with what the planners need to act on it. */
data class TransitTimeDigest(
    var code: String,
    var label: String,
    var contactEmail: String,
    var pinned: Boolean,
    var fuelLiters: Double,
    var etaMinutes: Int,
    var startsAt: Int,
    var margin: Double,
    var score: Double,
    var category: String,
    var depot: String,
    var status: TransitTimeDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeDigestStatus): Boolean {
        when (status) {
            TransitTimeDigestStatus.REQUESTED -> return next == TransitTimeDigestStatus.EXPIRED
            TransitTimeDigestStatus.GRANTED -> return next == TransitTimeDigestStatus.REVOKED
            TransitTimeDigestStatus.REVOKED -> {
                when (next) {
                    TransitTimeDigestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeDigestStatus) {
        if (!canMoveTo(next)) {
            error("transit time digest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveValue(): Double = round((margin - fuelLiters) * 100) / 100

    fun snapshot(): TransitTimeDigest {
        return TransitTimeDigest(
            code = code,
            label = label,
            contactEmail = contactEmail,
            pinned = pinned,
            fuelLiters = fuelLiters,
            etaMinutes = etaMinutes,
            startsAt = startsAt,
            margin = margin,
            score = score,
            category = category,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a transit time digest in status requested. */
fun newTransitTimeDigest(code: String, label: String): TransitTimeDigest {
    return TransitTimeDigest(
        code = code,
        label = label,
        contactEmail = "",
        pinned = false,
        fuelLiters = 0.0,
        etaMinutes = 0,
        startsAt = 0,
        margin = 0.0,
        score = 0.0,
        category = "",
        depot = "",
        status = TransitTimeDigestStatus.REQUESTED,
    )
}
