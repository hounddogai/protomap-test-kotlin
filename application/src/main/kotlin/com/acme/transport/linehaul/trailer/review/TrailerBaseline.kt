package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer baseline is in its life cycle. */
enum class TrailerBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A trailer baseline as the trailer team keeps it. */
data class TrailerBaseline(
    var code: String,
    var title: String,
    var phone: String,
    var endsAt: Int,
    var pinned: Boolean,
    var expiresAt: Int,
    var channel: String,
    var attempts: Int,
    var rate: Double,
    var reviewer: String,
    var status: TrailerBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerBaselineStatus): Boolean {
        when (status) {
            TrailerBaselineStatus.OPEN -> {
                when (next) {
                    TrailerBaselineStatus.RESOLVED, TrailerBaselineStatus.REOPENED -> return true
                }
                return false
            }
            TrailerBaselineStatus.IN_REVIEW -> return next == TrailerBaselineStatus.REOPENED || next == TrailerBaselineStatus.ARCHIVED
            TrailerBaselineStatus.RESOLVED -> return next == TrailerBaselineStatus.REOPENED || next == TrailerBaselineStatus.ARCHIVED
            TrailerBaselineStatus.REOPENED -> {
                when (next) {
                    TrailerBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerBaselineStatus) {
        if (!canMoveTo(next)) {
            error("trailer baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((endsAt.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): TrailerBaseline {
        return TrailerBaseline(
            code = code,
            title = title,
            phone = phone,
            endsAt = endsAt,
            pinned = pinned,
            expiresAt = expiresAt,
            channel = channel,
            attempts = attempts,
            rate = rate,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a trailer baseline in status open. */
fun newTrailerBaseline(code: String, title: String): TrailerBaseline {
    return TrailerBaseline(
        code = code,
        title = title,
        phone = "",
        endsAt = 0,
        pinned = false,
        expiresAt = 0,
        channel = "",
        attempts = 0,
        rate = 0.0,
        reviewer = "",
        status = TrailerBaselineStatus.OPEN,
    )
}
