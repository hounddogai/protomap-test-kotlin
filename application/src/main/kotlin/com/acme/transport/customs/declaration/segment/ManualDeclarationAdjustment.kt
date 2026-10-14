package com.acme.transport.customs.declaration.segment

import com.acme.transport.kit.text.normalize

/** Where a manual declaration adjustment is in its life cycle. */
enum class ManualDeclarationAdjustmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A manual declaration adjustment as the declaration team keeps it. */
data class ManualDeclarationAdjustment(
    var code: String,
    var label: String,
    var driverId: String,
    var locale: String,
    var lane: String,
    var score: Double,
    var status: ManualDeclarationAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDeclarationAdjustmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDeclarationAdjustmentStatus): Boolean {
        when (status) {
            ManualDeclarationAdjustmentStatus.PLANNED -> {
                when (next) {
                    ManualDeclarationAdjustmentStatus.COMPLETED, ManualDeclarationAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            ManualDeclarationAdjustmentStatus.SCHEDULED -> {
                when (next) {
                    ManualDeclarationAdjustmentStatus.COMPLETED, ManualDeclarationAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
            ManualDeclarationAdjustmentStatus.IN_PROGRESS -> return next == ManualDeclarationAdjustmentStatus.COMPLETED || next == ManualDeclarationAdjustmentStatus.CANCELLED
            ManualDeclarationAdjustmentStatus.COMPLETED -> {
                when (next) {
                    ManualDeclarationAdjustmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDeclarationAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("manual declaration adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = score

    fun snapshot(): ManualDeclarationAdjustment {
        return ManualDeclarationAdjustment(
            code = code,
            label = label,
            driverId = driverId,
            locale = locale,
            lane = lane,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual declaration adjustment in status planned. */
fun newManualDeclarationAdjustment(code: String, label: String): ManualDeclarationAdjustment {
    return ManualDeclarationAdjustment(
        code = code,
        label = label,
        driverId = "",
        locale = "",
        lane = "",
        score = 0.0,
        status = ManualDeclarationAdjustmentStatus.PLANNED,
    )
}
