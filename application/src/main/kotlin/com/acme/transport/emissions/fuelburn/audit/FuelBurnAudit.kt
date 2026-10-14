package com.acme.transport.emissions.fuelburn.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel burn audit records of emissions's fuel burn work, and the rules, stores, and calculations around
// them.

/** Where a fuel burn audit is in its life cycle. */
enum class FuelBurnAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a fuel burn audit and its current status. */
data class FuelBurnAudit(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var stopCount: Int,
    var pallets: Int,
    var weight: Double,
    var status: FuelBurnAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnAuditStatus): Boolean {
        when (status) {
            FuelBurnAuditStatus.DRAFT -> return next == FuelBurnAuditStatus.ACTIVE
            FuelBurnAuditStatus.PENDING -> {
                when (next) {
                    FuelBurnAuditStatus.ACTIVE -> return true
                }
                return false
            }
            FuelBurnAuditStatus.ACTIVE -> return next == FuelBurnAuditStatus.SUSPENDED
            FuelBurnAuditStatus.SUSPENDED -> {
                when (next) {
                    FuelBurnAuditStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnAuditStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fuel burn audit"} $code"

    fun netLoad(): Double = round(weight * stopCount.toDouble() * 100) / 100

    fun snapshot(): FuelBurnAudit {
        return FuelBurnAudit(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            stopCount = stopCount,
            pallets = pallets,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fuel burn audit in status draft. */
fun newFuelBurnAudit(code: String, label: String): FuelBurnAudit {
    return FuelBurnAudit(
        code = code,
        label = label,
        fuelLiters = 0.0,
        stopCount = 0,
        pallets = 0,
        weight = 0.0,
        status = FuelBurnAuditStatus.DRAFT,
    )
}
