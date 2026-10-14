package com.acme.transport.crewing.qualification.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional qualification draft is in its life cycle. */
enum class RegionalQualificationDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a regional qualification draft and its current status. */
data class RegionalQualificationDraft(
    var code: String,
    var name: String,
    var label: String,
    var active: Boolean,
    var stopCount: Int,
    var loadKg: Double,
    var durationMinutes: Int,
    var status: RegionalQualificationDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalQualificationDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalQualificationDraftStatus): Boolean {
        when (status) {
            RegionalQualificationDraftStatus.PROPOSED -> return next == RegionalQualificationDraftStatus.APPROVED
            RegionalQualificationDraftStatus.APPROVED -> {
                when (next) {
                    RegionalQualificationDraftStatus.REJECTED, RegionalQualificationDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalQualificationDraftStatus.REJECTED -> return next == RegionalQualificationDraftStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RegionalQualificationDraftStatus) {
        if (!canMoveTo(next)) {
            error("regional qualification draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $name"

    fun weightedLoad(): Double = round(stopCount.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): RegionalQualificationDraft {
        return RegionalQualificationDraft(
            code = code,
            name = name,
            label = label,
            active = active,
            stopCount = stopCount,
            loadKg = loadKg,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional qualification draft in status proposed. */
fun newRegionalQualificationDraft(code: String, name: String): RegionalQualificationDraft {
    return RegionalQualificationDraft(
        code = code,
        name = name,
        label = "",
        active = false,
        stopCount = 0,
        loadKg = 0.0,
        durationMinutes = 0,
        status = RegionalQualificationDraftStatus.PROPOSED,
    )
}
