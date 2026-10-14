package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling zone review is in its life cycle. */
enum class RollingZoneReviewStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a rolling zone review and its current status. */
data class RollingZoneReview(
    var code: String,
    var title: String,
    var pallets: Int,
    var version: Int,
    var batchSize: Int,
    var summary: String,
    var label: String,
    var quantity: Int,
    var status: RollingZoneReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingZoneReviewStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingZoneReviewStatus): Boolean {
        when (status) {
            RollingZoneReviewStatus.OPEN -> {
                when (next) {
                    RollingZoneReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingZoneReviewStatus.IN_REVIEW -> {
                when (next) {
                    RollingZoneReviewStatus.RESOLVED, RollingZoneReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingZoneReviewStatus.RESOLVED -> return next == RollingZoneReviewStatus.REOPENED
            RollingZoneReviewStatus.REOPENED -> return next == RollingZoneReviewStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingZoneReviewStatus) {
        if (!canMoveTo(next)) {
            error("rolling zone review $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $title"

    fun effectiveVolume(): Double = round((version.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): RollingZoneReview {
        return RollingZoneReview(
            code = code,
            title = title,
            pallets = pallets,
            version = version,
            batchSize = batchSize,
            summary = summary,
            label = label,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling zone review in status open. */
fun newRollingZoneReview(code: String, title: String): RollingZoneReview {
    return RollingZoneReview(
        code = code,
        title = title,
        pallets = 0,
        version = 0,
        batchSize = 0,
        summary = "",
        label = "",
        quantity = 0,
        status = RollingZoneReviewStatus.OPEN,
    )
}
