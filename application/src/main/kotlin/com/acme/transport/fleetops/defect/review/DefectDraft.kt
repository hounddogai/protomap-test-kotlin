package com.acme.transport.fleetops.defect.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect draft is in its life cycle. */
enum class DefectDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One defect draft, with what the planners need to act on it. */
data class DefectDraft(
    var code: String,
    var label: String,
    var loadKg: Double,
    var shiftCode: String,
    var segment: String,
    var durationMinutes: Int,
    var routeCode: String,
    var summary: String,
    var status: DefectDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectDraftStatus): Boolean {
        when (status) {
            DefectDraftStatus.DRAFT -> {
                when (next) {
                    DefectDraftStatus.ACTIVE, DefectDraftStatus.CLOSED -> return true
                }
                return false
            }
            DefectDraftStatus.PENDING -> return next == DefectDraftStatus.ACTIVE || next == DefectDraftStatus.CLOSED
            DefectDraftStatus.ACTIVE -> {
                when (next) {
                    DefectDraftStatus.SUSPENDED -> return true
                }
                return false
            }
            DefectDraftStatus.SUSPENDED -> {
                when (next) {
                    DefectDraftStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectDraftStatus) {
        if (!canMoveTo(next)) {
            error("defect draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedExposure(): Double = round((loadKg + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): DefectDraft {
        return DefectDraft(
            code = code,
            label = label,
            loadKg = loadKg,
            shiftCode = shiftCode,
            segment = segment,
            durationMinutes = durationMinutes,
            routeCode = routeCode,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a defect draft in status draft. */
fun newDefectDraft(code: String, label: String): DefectDraft {
    return DefectDraft(
        code = code,
        label = label,
        loadKg = 0.0,
        shiftCode = "",
        segment = "",
        durationMinutes = 0,
        routeCode = "",
        summary = "",
        status = DefectDraftStatus.DRAFT,
    )
}
