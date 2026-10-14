package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy accessorial draft is in its life cycle. */
enum class LegacyAccessorialDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy accessorial draft. Its code is unique within a tenant. */
data class LegacyAccessorialDraft(
    var code: String,
    var title: String,
    var notes: String,
    var threshold: Double,
    var durationMinutes: Int,
    var stopCount: Int,
    var sequence: Int,
    var status: LegacyAccessorialDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAccessorialDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAccessorialDraftStatus): Boolean {
        when (status) {
            LegacyAccessorialDraftStatus.PROPOSED -> return next == LegacyAccessorialDraftStatus.APPROVED || next == LegacyAccessorialDraftStatus.WITHDRAWN
            LegacyAccessorialDraftStatus.APPROVED -> {
                when (next) {
                    LegacyAccessorialDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyAccessorialDraftStatus.REJECTED -> return next == LegacyAccessorialDraftStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyAccessorialDraftStatus) {
        if (!canMoveTo(next)) {
            error("legacy accessorial draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedExposure(): Double = round((threshold - sequence.toDouble()) * 100) / 100

    fun snapshot(): LegacyAccessorialDraft {
        return LegacyAccessorialDraft(
            code = code,
            title = title,
            notes = notes,
            threshold = threshold,
            durationMinutes = durationMinutes,
            stopCount = stopCount,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy accessorial draft in status proposed. */
fun newLegacyAccessorialDraft(code: String, title: String): LegacyAccessorialDraft {
    return LegacyAccessorialDraft(
        code = code,
        title = title,
        notes = "",
        threshold = 0.0,
        durationMinutes = 0,
        stopCount = 0,
        sequence = 0,
        status = LegacyAccessorialDraftStatus.PROPOSED,
    )
}
