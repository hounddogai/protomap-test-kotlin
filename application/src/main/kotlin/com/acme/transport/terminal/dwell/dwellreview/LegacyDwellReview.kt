package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy dwell review records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a legacy dwell review is in its life cycle. */
enum class LegacyDwellReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a legacy dwell review and its current status. */
data class LegacyDwellReview(
    var code: String,
    var label: String,
    var batchSize: Int,
    var pallets: Int,
    var retries: Int,
    var routeCode: String,
    var unit: String,
    var visible: Boolean,
    var status: LegacyDwellReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDwellReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDwellReviewStatus): Boolean {
        when (status) {
            LegacyDwellReviewStatus.REQUESTED -> {
                when (next) {
                    LegacyDwellReviewStatus.REVOKED, LegacyDwellReviewStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyDwellReviewStatus.GRANTED -> return next == LegacyDwellReviewStatus.EXPIRED
            LegacyDwellReviewStatus.REVOKED -> return next == LegacyDwellReviewStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LegacyDwellReviewStatus) {
        if (!canMoveTo(next)) {
            error("legacy dwell review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy dwell review"} $code"

    fun netLoad(): Double = round((pallets.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): LegacyDwellReview {
        return LegacyDwellReview(
            code = code,
            label = label,
            batchSize = batchSize,
            pallets = pallets,
            retries = retries,
            routeCode = routeCode,
            unit = unit,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a legacy dwell review in status requested. */
fun newLegacyDwellReview(code: String, label: String): LegacyDwellReview {
    return LegacyDwellReview(
        code = code,
        label = label,
        batchSize = 0,
        pallets = 0,
        retries = 0,
        routeCode = "",
        unit = "",
        visible = false,
        status = LegacyDwellReviewStatus.REQUESTED,
    )
}
