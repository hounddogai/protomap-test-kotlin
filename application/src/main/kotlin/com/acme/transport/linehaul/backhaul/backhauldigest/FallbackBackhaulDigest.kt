package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback backhaul digest records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a fallback backhaul digest is in its life cycle. */
enum class FallbackBackhaulDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One fallback backhaul digest, with what the planners need to act on it. */
data class FallbackBackhaulDigest(
    var code: String,
    var name: String,
    var stopCount: Int,
    var approved: Boolean,
    var region: String,
    var limit: Int,
    var status: FallbackBackhaulDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackBackhaulDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackBackhaulDigestStatus): Boolean {
        when (status) {
            FallbackBackhaulDigestStatus.REQUESTED -> return next == FallbackBackhaulDigestStatus.REVOKED
            FallbackBackhaulDigestStatus.GRANTED -> {
                when (next) {
                    FallbackBackhaulDigestStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackBackhaulDigestStatus.REVOKED -> return next == FallbackBackhaulDigestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FallbackBackhaulDigestStatus) {
        if (!canMoveTo(next)) {
            error("fallback backhaul digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round((stopCount.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): FallbackBackhaulDigest {
        return FallbackBackhaulDigest(
            code = code,
            name = name,
            stopCount = stopCount,
            approved = approved,
            region = region,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback backhaul digest in status requested. */
fun newFallbackBackhaulDigest(code: String, name: String): FallbackBackhaulDigest {
    return FallbackBackhaulDigest(
        code = code,
        name = name,
        stopCount = 0,
        approved = false,
        region = "",
        limit = 0,
        status = FallbackBackhaulDigestStatus.REQUESTED,
    )
}
