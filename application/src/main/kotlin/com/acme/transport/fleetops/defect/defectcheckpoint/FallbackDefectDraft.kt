package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback defect draft is in its life cycle. */
enum class FallbackDefectDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fallback defect draft, with what the planners need to act on it. */
data class FallbackDefectDraft(
    var code: String,
    var label: String,
    var locale: String,
    var threshold: Double,
    var fuelLiters: Double,
    var status: FallbackDefectDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDefectDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDefectDraftStatus): Boolean {
        when (status) {
            FallbackDefectDraftStatus.IDLE -> return next == FallbackDefectDraftStatus.WARMING || next == FallbackDefectDraftStatus.STOPPED
            FallbackDefectDraftStatus.WARMING -> return next == FallbackDefectDraftStatus.DRAINING
            FallbackDefectDraftStatus.READY -> return next == FallbackDefectDraftStatus.DRAINING || next == FallbackDefectDraftStatus.STOPPED
            FallbackDefectDraftStatus.DRAINING -> {
                when (next) {
                    FallbackDefectDraftStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDefectDraftStatus) {
        if (!canMoveTo(next)) {
            error("fallback defect draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveLoad(): Double = round(threshold * fuelLiters * 100) / 100

    fun snapshot(): FallbackDefectDraft {
        return FallbackDefectDraft(
            code = code,
            label = label,
            locale = locale,
            threshold = threshold,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback defect draft in status idle. */
fun newFallbackDefectDraft(code: String, label: String): FallbackDefectDraft {
    return FallbackDefectDraft(
        code = code,
        label = label,
        locale = "",
        threshold = 0.0,
        fuelLiters = 0.0,
        status = FallbackDefectDraftStatus.IDLE,
    )
}
