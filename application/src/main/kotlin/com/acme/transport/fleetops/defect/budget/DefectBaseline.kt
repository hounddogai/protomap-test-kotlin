package com.acme.transport.fleetops.defect.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect baseline is in its life cycle. */
enum class DefectBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One defect baseline, with what the planners need to act on it. */
data class DefectBaseline(
    var code: String,
    var label: String,
    var visible: Boolean,
    var amount: Double,
    var name: String,
    var timezone: String,
    var durationMinutes: Int,
    var reviewer: String,
    var locale: String,
    var expiresAt: Int,
    var etaMinutes: Int,
    var status: DefectBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectBaselineStatus): Boolean {
        when (status) {
            DefectBaselineStatus.PLANNED -> {
                when (next) {
                    DefectBaselineStatus.IN_PROGRESS -> return true
                }
                return false
            }
            DefectBaselineStatus.SCHEDULED -> return next == DefectBaselineStatus.COMPLETED || next == DefectBaselineStatus.CANCELLED
            DefectBaselineStatus.IN_PROGRESS -> return next == DefectBaselineStatus.CANCELLED
            DefectBaselineStatus.COMPLETED -> return next == DefectBaselineStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DefectBaselineStatus) {
        if (!canMoveTo(next)) {
            error("defect baseline $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveValue(): Double = round((etaMinutes.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): DefectBaseline {
        return DefectBaseline(
            code = code,
            label = label,
            visible = visible,
            amount = amount,
            name = name,
            timezone = timezone,
            durationMinutes = durationMinutes,
            reviewer = reviewer,
            locale = locale,
            expiresAt = expiresAt,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a defect baseline in status planned. */
fun newDefectBaseline(code: String, label: String): DefectBaseline {
    return DefectBaseline(
        code = code,
        label = label,
        visible = false,
        amount = 0.0,
        name = "",
        timezone = "",
        durationMinutes = 0,
        reviewer = "",
        locale = "",
        expiresAt = 0,
        etaMinutes = 0,
        status = DefectBaselineStatus.PLANNED,
    )
}
