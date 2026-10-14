package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The relay review records of linehaul's relay work, and the rules, stores, and calculations around them.

/** Where a relay review is in its life cycle. */
enum class RelayReviewStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One relay review, with what the planners need to act on it. */
data class RelayReview(
    var code: String,
    var name: String,
    var margin: Double,
    var graceDays: Int,
    var ceiling: Double,
    var retries: Int,
    var region: String,
    var pallets: Int,
    var vehicleId: String,
    var status: RelayReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayReviewStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayReviewStatus): Boolean {
        when (status) {
            RelayReviewStatus.OPEN -> {
                when (next) {
                    RelayReviewStatus.RESOLVED -> return true
                }
                return false
            }
            RelayReviewStatus.IN_REVIEW -> {
                when (next) {
                    RelayReviewStatus.REOPENED -> return true
                }
                return false
            }
            RelayReviewStatus.RESOLVED -> {
                when (next) {
                    RelayReviewStatus.REOPENED, RelayReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            RelayReviewStatus.REOPENED -> return next == RelayReviewStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RelayReviewStatus) {
        if (!canMoveTo(next)) {
            error("relay review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round((margin - graceDays.toDouble()) * 100) / 100

    fun snapshot(): RelayReview {
        return RelayReview(
            code = code,
            name = name,
            margin = margin,
            graceDays = graceDays,
            ceiling = ceiling,
            retries = retries,
            region = region,
            pallets = pallets,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a relay review in status open. */
fun newRelayReview(code: String, name: String): RelayReview {
    return RelayReview(
        code = code,
        name = name,
        margin = 0.0,
        graceDays = 0,
        ceiling = 0.0,
        retries = 0,
        region = "",
        pallets = 0,
        vehicleId = "",
        status = RelayReviewStatus.OPEN,
    )
}
