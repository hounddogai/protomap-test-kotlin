package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge plan is in its life cycle. */
enum class FuelSurchargePlanStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a fuel surcharge plan and its current status. */
data class FuelSurchargePlan(
    var code: String,
    var name: String,
    var loadKg: Double,
    var createdAt: Int,
    var durationMinutes: Int,
    var externalRef: String,
    var status: FuelSurchargePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargePlanStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargePlanStatus): Boolean {
        when (status) {
            FuelSurchargePlanStatus.OPEN -> return next == FuelSurchargePlanStatus.REOPENED || next == FuelSurchargePlanStatus.ARCHIVED
            FuelSurchargePlanStatus.IN_REVIEW -> {
                when (next) {
                    FuelSurchargePlanStatus.RESOLVED -> return true
                }
                return false
            }
            FuelSurchargePlanStatus.RESOLVED -> return next == FuelSurchargePlanStatus.ARCHIVED
            FuelSurchargePlanStatus.REOPENED -> return next == FuelSurchargePlanStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FuelSurchargePlanStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge plan $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round((durationMinutes.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): FuelSurchargePlan {
        return FuelSurchargePlan(
            code = code,
            name = name,
            loadKg = loadKg,
            createdAt = createdAt,
            durationMinutes = durationMinutes,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fuel surcharge plan in status open. */
fun newFuelSurchargePlan(code: String, name: String): FuelSurchargePlan {
    return FuelSurchargePlan(
        code = code,
        name = name,
        loadKg = 0.0,
        createdAt = 0,
        durationMinutes = 0,
        externalRef = "",
        status = FuelSurchargePlanStatus.OPEN,
    )
}
