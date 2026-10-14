package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered route offset claim records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a tiered route offset claim is in its life cycle. */
enum class TieredRouteOffsetClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered route offset claim. Its code is unique within a tenant. */
data class TieredRouteOffsetClaim(
    var code: String,
    var title: String,
    var graceDays: Int,
    var segment: String,
    var currency: String,
    var priority: Int,
    var archived: Boolean,
    var endsAt: Int,
    var odometer: Int,
    var lane: String,
    var status: TieredRouteOffsetClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRouteOffsetClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRouteOffsetClaimStatus): Boolean {
        when (status) {
            TieredRouteOffsetClaimStatus.OPEN -> return next == TieredRouteOffsetClaimStatus.IN_REVIEW || next == TieredRouteOffsetClaimStatus.REOPENED
            TieredRouteOffsetClaimStatus.IN_REVIEW -> {
                when (next) {
                    TieredRouteOffsetClaimStatus.RESOLVED, TieredRouteOffsetClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredRouteOffsetClaimStatus.RESOLVED -> return next == TieredRouteOffsetClaimStatus.REOPENED || next == TieredRouteOffsetClaimStatus.ARCHIVED
            TieredRouteOffsetClaimStatus.REOPENED -> {
                when (next) {
                    TieredRouteOffsetClaimStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredRouteOffsetClaimStatus) {
        if (!canMoveTo(next)) {
            error("tiered route offset claim $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun weightedExposure(): Double = round((endsAt.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): TieredRouteOffsetClaim {
        return TieredRouteOffsetClaim(
            code = code,
            title = title,
            graceDays = graceDays,
            segment = segment,
            currency = currency,
            priority = priority,
            archived = archived,
            endsAt = endsAt,
            odometer = odometer,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered route offset claim in status open. */
fun newTieredRouteOffsetClaim(code: String, title: String): TieredRouteOffsetClaim {
    return TieredRouteOffsetClaim(
        code = code,
        title = title,
        graceDays = 0,
        segment = "",
        currency = "",
        priority = 0,
        archived = false,
        endsAt = 0,
        odometer = 0,
        lane = "",
        status = TieredRouteOffsetClaimStatus.OPEN,
    )
}
