package com.acme.transport.customs.inspection.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection window is in its life cycle. */
enum class InspectionWindowStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A inspection window as the inspection team keeps it. */
data class InspectionWindow(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var rate: Double,
    var currency: String,
    var windowDays: Int,
    var approved: Boolean,
    var expiresAt: Int,
    var trailerId: String,
    var status: InspectionWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionWindowStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionWindowStatus): Boolean {
        when (status) {
            InspectionWindowStatus.PLANNED -> return next == InspectionWindowStatus.IN_PROGRESS
            InspectionWindowStatus.SCHEDULED -> return next == InspectionWindowStatus.IN_PROGRESS || next == InspectionWindowStatus.CANCELLED
            InspectionWindowStatus.IN_PROGRESS -> {
                when (next) {
                    InspectionWindowStatus.COMPLETED, InspectionWindowStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionWindowStatus.COMPLETED -> return next == InspectionWindowStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: InspectionWindowStatus) {
        if (!canMoveTo(next)) {
            error("inspection window $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"inspection window"} $code"

    fun netLoad(): Double = round((windowDays.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): InspectionWindow {
        return InspectionWindow(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            rate = rate,
            currency = currency,
            windowDays = windowDays,
            approved = approved,
            expiresAt = expiresAt,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection window in status planned. */
fun newInspectionWindow(code: String, label: String): InspectionWindow {
    return InspectionWindow(
        code = code,
        label = label,
        etaMinutes = 0,
        rate = 0.0,
        currency = "",
        windowDays = 0,
        approved = false,
        expiresAt = 0,
        trailerId = "",
        status = InspectionWindowStatus.PLANNED,
    )
}
