package com.acme.transport.terminal.dwell.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default dwell review records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a default dwell review is in its life cycle. */
enum class DefaultDwellReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One default dwell review, with what the planners need to act on it. */
data class DefaultDwellReview(
    var code: String,
    var title: String,
    var lane: String,
    var channel: String,
    var externalRef: String,
    var attributes: MutableMap<String, String>,
    var version: Int,
    var priority: Int,
    var source: String,
    var ratio: Double,
    var amount: Double,
    var status: DefaultDwellReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDwellReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDwellReviewStatus): Boolean {
        when (status) {
            DefaultDwellReviewStatus.REQUESTED -> {
                when (next) {
                    DefaultDwellReviewStatus.GRANTED, DefaultDwellReviewStatus.REVOKED -> return true
                }
                return false
            }
            DefaultDwellReviewStatus.GRANTED -> return next == DefaultDwellReviewStatus.REVOKED || next == DefaultDwellReviewStatus.EXPIRED
            DefaultDwellReviewStatus.REVOKED -> return next == DefaultDwellReviewStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultDwellReviewStatus) {
        if (!canMoveTo(next)) {
            error("default dwell review $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedExposure(): Double = round((priority.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): DefaultDwellReview {
        return DefaultDwellReview(
            code = code,
            title = title,
            lane = lane,
            channel = channel,
            externalRef = externalRef,
            attributes = attributes,
            version = version,
            priority = priority,
            source = source,
            ratio = ratio,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default dwell review in status requested. */
fun newDefaultDwellReview(code: String, title: String): DefaultDwellReview {
    return DefaultDwellReview(
        code = code,
        title = title,
        lane = "",
        channel = "",
        externalRef = "",
        attributes = mutableMapOf<String, String>(),
        version = 0,
        priority = 0,
        source = "",
        ratio = 0.0,
        amount = 0.0,
        status = DefaultDwellReviewStatus.REQUESTED,
    )
}
