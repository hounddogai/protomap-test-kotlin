package com.acme.transport.fleetops.inspection.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal inspection draft is in its life cycle. */
enum class SeasonalInspectionDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal inspection draft as the inspection team keeps it. */
data class SeasonalInspectionDraft(
    var code: String,
    var title: String,
    var limit: Int,
    var durationMinutes: Int,
    var depot: String,
    var pinned: Boolean,
    var priority: Int,
    var notes: String,
    var reviewer: String,
    var status: SeasonalInspectionDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalInspectionDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalInspectionDraftStatus): Boolean {
        when (status) {
            SeasonalInspectionDraftStatus.REQUESTED -> {
                when (next) {
                    SeasonalInspectionDraftStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalInspectionDraftStatus.GRANTED -> {
                when (next) {
                    SeasonalInspectionDraftStatus.REVOKED, SeasonalInspectionDraftStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalInspectionDraftStatus.REVOKED -> {
                when (next) {
                    SeasonalInspectionDraftStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalInspectionDraftStatus) {
        if (!canMoveTo(next)) {
            error("seasonal inspection draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedValue(): Double = round((priority.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): SeasonalInspectionDraft {
        return SeasonalInspectionDraft(
            code = code,
            title = title,
            limit = limit,
            durationMinutes = durationMinutes,
            depot = depot,
            pinned = pinned,
            priority = priority,
            notes = notes,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a seasonal inspection draft in status requested. */
fun newSeasonalInspectionDraft(code: String, title: String): SeasonalInspectionDraft {
    return SeasonalInspectionDraft(
        code = code,
        title = title,
        limit = 0,
        durationMinutes = 0,
        depot = "",
        pinned = false,
        priority = 0,
        notes = "",
        reviewer = "",
        status = SeasonalInspectionDraftStatus.REQUESTED,
    )
}
