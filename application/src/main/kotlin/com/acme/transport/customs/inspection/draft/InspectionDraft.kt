package com.acme.transport.customs.inspection.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection draft records of customs's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection draft is in its life cycle. */
enum class InspectionDraftStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A inspection draft. Its code is unique within a tenant. */
data class InspectionDraft(
    var code: String,
    var name: String,
    var loadKg: Double,
    var lane: String,
    var pinned: Boolean,
    var source: String,
    var ceiling: Double,
    var windowDays: Int,
    var status: InspectionDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionDraftStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionDraftStatus): Boolean {
        when (status) {
            InspectionDraftStatus.OPEN -> return next == InspectionDraftStatus.ARCHIVED
            InspectionDraftStatus.IN_REVIEW -> {
                when (next) {
                    InspectionDraftStatus.RESOLVED -> return true
                }
                return false
            }
            InspectionDraftStatus.RESOLVED -> {
                when (next) {
                    InspectionDraftStatus.REOPENED -> return true
                }
                return false
            }
            InspectionDraftStatus.REOPENED -> {
                when (next) {
                    InspectionDraftStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionDraftStatus) {
        if (!canMoveTo(next)) {
            error("inspection draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round((ceiling - windowDays.toDouble()) * 100) / 100

    fun snapshot(): InspectionDraft {
        return InspectionDraft(
            code = code,
            name = name,
            loadKg = loadKg,
            lane = lane,
            pinned = pinned,
            source = source,
            ceiling = ceiling,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a inspection draft in status open. */
fun newInspectionDraft(code: String, name: String): InspectionDraft {
    return InspectionDraft(
        code = code,
        name = name,
        loadKg = 0.0,
        lane = "",
        pinned = false,
        source = "",
        ceiling = 0.0,
        windowDays = 0,
        status = InspectionDraftStatus.OPEN,
    )
}
