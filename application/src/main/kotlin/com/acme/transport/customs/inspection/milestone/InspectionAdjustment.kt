package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection adjustment is in its life cycle. */
enum class InspectionAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a inspection adjustment and its current status. */
data class InspectionAdjustment(
    var code: String,
    var label: String,
    var category: String,
    var fuelLiters: Double,
    var endsAt: Int,
    var odometer: Int,
    var graceDays: Int,
    var cost: Double,
    var limit: Int,
    var status: InspectionAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionAdjustmentStatus): Boolean {
        when (status) {
            InspectionAdjustmentStatus.NEW -> return next == InspectionAdjustmentStatus.QUEUED || next == InspectionAdjustmentStatus.RUNNING
            InspectionAdjustmentStatus.QUEUED -> {
                when (next) {
                    InspectionAdjustmentStatus.RUNNING, InspectionAdjustmentStatus.DONE -> return true
                }
                return false
            }
            InspectionAdjustmentStatus.RUNNING -> return next == InspectionAdjustmentStatus.FAILED
            InspectionAdjustmentStatus.DONE -> return next == InspectionAdjustmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: InspectionAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("inspection adjustment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveTotal(): Double = round((endsAt.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): InspectionAdjustment {
        return InspectionAdjustment(
            code = code,
            label = label,
            category = category,
            fuelLiters = fuelLiters,
            endsAt = endsAt,
            odometer = odometer,
            graceDays = graceDays,
            cost = cost,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a inspection adjustment in status new. */
fun newInspectionAdjustment(code: String, label: String): InspectionAdjustment {
    return InspectionAdjustment(
        code = code,
        label = label,
        category = "",
        fuelLiters = 0.0,
        endsAt = 0,
        odometer = 0,
        graceDays = 0,
        cost = 0.0,
        limit = 0,
        status = InspectionAdjustmentStatus.NEW,
    )
}
