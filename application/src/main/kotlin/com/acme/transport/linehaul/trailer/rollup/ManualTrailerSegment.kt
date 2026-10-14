package com.acme.transport.linehaul.trailer.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual trailer segment is in its life cycle. */
enum class ManualTrailerSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a manual trailer segment and its current status. */
data class ManualTrailerSegment(
    var code: String,
    var label: String,
    var batchSize: Int,
    var trailerId: String,
    var threshold: Double,
    var status: ManualTrailerSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTrailerSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTrailerSegmentStatus): Boolean {
        when (status) {
            ManualTrailerSegmentStatus.PROPOSED -> {
                when (next) {
                    ManualTrailerSegmentStatus.APPROVED -> return true
                }
                return false
            }
            ManualTrailerSegmentStatus.APPROVED -> return next == ManualTrailerSegmentStatus.REJECTED || next == ManualTrailerSegmentStatus.WITHDRAWN
            ManualTrailerSegmentStatus.REJECTED -> {
                when (next) {
                    ManualTrailerSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTrailerSegmentStatus) {
        if (!canMoveTo(next)) {
            error("manual trailer segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedExposure(): Double = round(batchSize.toDouble() * threshold * 100) / 100

    fun snapshot(): ManualTrailerSegment {
        return ManualTrailerSegment(
            code = code,
            label = label,
            batchSize = batchSize,
            trailerId = trailerId,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual trailer segment in status proposed. */
fun newManualTrailerSegment(code: String, label: String): ManualTrailerSegment {
    return ManualTrailerSegment(
        code = code,
        label = label,
        batchSize = 0,
        trailerId = "",
        threshold = 0.0,
        status = ManualTrailerSegmentStatus.PROPOSED,
    )
}
