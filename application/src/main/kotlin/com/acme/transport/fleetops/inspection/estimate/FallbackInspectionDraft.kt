package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback inspection draft is in its life cycle. */
enum class FallbackInspectionDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback inspection draft. Its code is unique within a tenant. */
data class FallbackInspectionDraft(
    var code: String,
    var title: String,
    var country: String,
    var externalRef: String,
    var windowDays: Int,
    var etaMinutes: Int,
    var ownerId: String,
    var reviewer: String,
    var retries: Int,
    var updatedAt: Int,
    var status: FallbackInspectionDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackInspectionDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackInspectionDraftStatus): Boolean {
        when (status) {
            FallbackInspectionDraftStatus.IDLE -> return next == FallbackInspectionDraftStatus.STOPPED
            FallbackInspectionDraftStatus.WARMING -> return next == FallbackInspectionDraftStatus.READY || next == FallbackInspectionDraftStatus.DRAINING
            FallbackInspectionDraftStatus.READY -> return next == FallbackInspectionDraftStatus.STOPPED
            FallbackInspectionDraftStatus.DRAINING -> return next == FallbackInspectionDraftStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackInspectionDraftStatus) {
        if (!canMoveTo(next)) {
            error("fallback inspection draft $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback inspection draft"} $code"

    fun weightedTotal(): Double = round((retries.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): FallbackInspectionDraft {
        return FallbackInspectionDraft(
            code = code,
            title = title,
            country = country,
            externalRef = externalRef,
            windowDays = windowDays,
            etaMinutes = etaMinutes,
            ownerId = ownerId,
            reviewer = reviewer,
            retries = retries,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a fallback inspection draft in status idle. */
fun newFallbackInspectionDraft(code: String, title: String): FallbackInspectionDraft {
    return FallbackInspectionDraft(
        code = code,
        title = title,
        country = "",
        externalRef = "",
        windowDays = 0,
        etaMinutes = 0,
        ownerId = "",
        reviewer = "",
        retries = 0,
        updatedAt = 0,
        status = FallbackInspectionDraftStatus.IDLE,
    )
}
