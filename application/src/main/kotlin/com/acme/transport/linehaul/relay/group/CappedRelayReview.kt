package com.acme.transport.linehaul.relay.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped relay review is in its life cycle. */
enum class CappedRelayReviewStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One capped relay review, with what the planners need to act on it. */
data class CappedRelayReview(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var limit: Int,
    var capacity: Int,
    var ceiling: Double,
    var retries: Int,
    var locale: String,
    var description: String,
    var status: CappedRelayReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRelayReviewStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRelayReviewStatus): Boolean {
        when (status) {
            CappedRelayReviewStatus.OPEN -> {
                when (next) {
                    CappedRelayReviewStatus.IN_REVIEW, CappedRelayReviewStatus.REOPENED -> return true
                }
                return false
            }
            CappedRelayReviewStatus.IN_REVIEW -> return next == CappedRelayReviewStatus.REOPENED
            CappedRelayReviewStatus.RESOLVED -> return next == CappedRelayReviewStatus.REOPENED || next == CappedRelayReviewStatus.ARCHIVED
            CappedRelayReviewStatus.REOPENED -> {
                when (next) {
                    CappedRelayReviewStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedRelayReviewStatus) {
        if (!canMoveTo(next)) {
            error("capped relay review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netTotal(): Double = round(ceiling * retries.toDouble() * 100) / 100

    fun snapshot(): CappedRelayReview {
        return CappedRelayReview(
            code = code,
            name = name,
            distanceKm = distanceKm,
            limit = limit,
            capacity = capacity,
            ceiling = ceiling,
            retries = retries,
            locale = locale,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a capped relay review in status open. */
fun newCappedRelayReview(code: String, name: String): CappedRelayReview {
    return CappedRelayReview(
        code = code,
        name = name,
        distanceKm = 0.0,
        limit = 0,
        capacity = 0,
        ceiling = 0.0,
        retries = 0,
        locale = "",
        description = "",
        status = CappedRelayReviewStatus.OPEN,
    )
}
