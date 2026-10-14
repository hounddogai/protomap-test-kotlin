package com.acme.transport.fleetops.defect.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect estimate is in its life cycle. */
enum class DefectEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A defect estimate. Its code is unique within a tenant. */
data class DefectEstimate(
    var code: String,
    var label: String,
    var discount: Double,
    var ownerId: String,
    var pallets: Int,
    var routeCode: String,
    var country: String,
    var fuelLiters: Double,
    var archived: Boolean,
    var graceDays: Int,
    var amount: Double,
    var status: DefectEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectEstimateStatus): Boolean {
        when (status) {
            DefectEstimateStatus.IDLE -> return next == DefectEstimateStatus.READY || next == DefectEstimateStatus.STOPPED
            DefectEstimateStatus.WARMING -> return next == DefectEstimateStatus.DRAINING || next == DefectEstimateStatus.STOPPED
            DefectEstimateStatus.READY -> {
                when (next) {
                    DefectEstimateStatus.DRAINING, DefectEstimateStatus.STOPPED -> return true
                }
                return false
            }
            DefectEstimateStatus.DRAINING -> {
                when (next) {
                    DefectEstimateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectEstimateStatus) {
        if (!canMoveTo(next)) {
            error("defect estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedTotal(): Double = round((graceDays.toDouble() + discount) * 100) / 100

    fun snapshot(): DefectEstimate {
        return DefectEstimate(
            code = code,
            label = label,
            discount = discount,
            ownerId = ownerId,
            pallets = pallets,
            routeCode = routeCode,
            country = country,
            fuelLiters = fuelLiters,
            archived = archived,
            graceDays = graceDays,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a defect estimate in status idle. */
fun newDefectEstimate(code: String, label: String): DefectEstimate {
    return DefectEstimate(
        code = code,
        label = label,
        discount = 0.0,
        ownerId = "",
        pallets = 0,
        routeCode = "",
        country = "",
        fuelLiters = 0.0,
        archived = false,
        graceDays = 0,
        amount = 0.0,
        status = DefectEstimateStatus.IDLE,
    )
}
