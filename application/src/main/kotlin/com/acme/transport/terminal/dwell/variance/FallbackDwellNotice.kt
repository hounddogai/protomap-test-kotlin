package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback dwell notice is in its life cycle. */
enum class FallbackDwellNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fallback dwell notice as the dwell team keeps it. */
data class FallbackDwellNotice(
    var code: String,
    var label: String,
    var priority: Int,
    var revision: Int,
    var threshold: Double,
    var score: Double,
    var status: FallbackDwellNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDwellNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDwellNoticeStatus): Boolean {
        when (status) {
            FallbackDwellNoticeStatus.PLANNED -> {
                when (next) {
                    FallbackDwellNoticeStatus.COMPLETED -> return true
                }
                return false
            }
            FallbackDwellNoticeStatus.SCHEDULED -> return next == FallbackDwellNoticeStatus.COMPLETED || next == FallbackDwellNoticeStatus.CANCELLED
            FallbackDwellNoticeStatus.IN_PROGRESS -> {
                when (next) {
                    FallbackDwellNoticeStatus.COMPLETED, FallbackDwellNoticeStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackDwellNoticeStatus.COMPLETED -> return next == FallbackDwellNoticeStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FallbackDwellNoticeStatus) {
        if (!canMoveTo(next)) {
            error("fallback dwell notice $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedLoad(): Double = round(score * revision.toDouble() * 100) / 100

    fun snapshot(): FallbackDwellNotice {
        return FallbackDwellNotice(
            code = code,
            label = label,
            priority = priority,
            revision = revision,
            threshold = threshold,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback dwell notice in status planned. */
fun newFallbackDwellNotice(code: String, label: String): FallbackDwellNotice {
    return FallbackDwellNotice(
        code = code,
        label = label,
        priority = 0,
        revision = 0,
        threshold = 0.0,
        score = 0.0,
        status = FallbackDwellNoticeStatus.PLANNED,
    )
}
