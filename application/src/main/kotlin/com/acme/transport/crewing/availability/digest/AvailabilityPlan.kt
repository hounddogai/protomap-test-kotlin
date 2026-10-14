package com.acme.transport.crewing.availability.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability plan is in its life cycle. */
enum class AvailabilityPlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A availability plan. Its code is unique within a tenant. */
data class AvailabilityPlan(
    var code: String,
    var label: String,
    var routeCode: String,
    var fuelLiters: Double,
    var title: String,
    var visible: Boolean,
    var contactEmail: String,
    var pallets: Int,
    var shiftCode: String,
    var summary: String,
    var limit: Int,
    var status: AvailabilityPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityPlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityPlanStatus): Boolean {
        when (status) {
            AvailabilityPlanStatus.NEW -> return next == AvailabilityPlanStatus.RUNNING || next == AvailabilityPlanStatus.FAILED
            AvailabilityPlanStatus.QUEUED -> return next == AvailabilityPlanStatus.DONE || next == AvailabilityPlanStatus.FAILED
            AvailabilityPlanStatus.RUNNING -> return next == AvailabilityPlanStatus.FAILED
            AvailabilityPlanStatus.DONE -> return next == AvailabilityPlanStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AvailabilityPlanStatus) {
        if (!canMoveTo(next)) {
            error("availability plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedLoad(): Double = round((fuelLiters + pallets.toDouble()) * 100) / 100

    fun snapshot(): AvailabilityPlan {
        return AvailabilityPlan(
            code = code,
            label = label,
            routeCode = routeCode,
            fuelLiters = fuelLiters,
            title = title,
            visible = visible,
            contactEmail = contactEmail,
            pallets = pallets,
            shiftCode = shiftCode,
            summary = summary,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a availability plan in status new. */
fun newAvailabilityPlan(code: String, label: String): AvailabilityPlan {
    return AvailabilityPlan(
        code = code,
        label = label,
        routeCode = "",
        fuelLiters = 0.0,
        title = "",
        visible = false,
        contactEmail = "",
        pallets = 0,
        shiftCode = "",
        summary = "",
        limit = 0,
        status = AvailabilityPlanStatus.NEW,
    )
}
