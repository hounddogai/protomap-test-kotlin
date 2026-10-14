package com.acme.transport.fleetops.defect.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive defect audit is in its life cycle. */
enum class AdaptiveDefectAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive defect audit as the defect team keeps it. */
data class AdaptiveDefectAudit(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var active: Boolean,
    var loadKg: Double,
    var routeCode: String,
    var fuelLiters: Double,
    var status: AdaptiveDefectAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDefectAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDefectAuditStatus): Boolean {
        when (status) {
            AdaptiveDefectAuditStatus.IDLE -> return next == AdaptiveDefectAuditStatus.WARMING || next == AdaptiveDefectAuditStatus.STOPPED
            AdaptiveDefectAuditStatus.WARMING -> {
                when (next) {
                    AdaptiveDefectAuditStatus.READY, AdaptiveDefectAuditStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveDefectAuditStatus.READY -> return next == AdaptiveDefectAuditStatus.DRAINING
            AdaptiveDefectAuditStatus.DRAINING -> {
                when (next) {
                    AdaptiveDefectAuditStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDefectAuditStatus) {
        if (!canMoveTo(next)) {
            error("adaptive defect audit $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun netValue(): Double = round(loadKg * fuelLiters * 100) / 100

    fun snapshot(): AdaptiveDefectAudit {
        return AdaptiveDefectAudit(
            code = code,
            label = label,
            updatedAt = updatedAt,
            active = active,
            loadKg = loadKg,
            routeCode = routeCode,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive defect audit in status idle. */
fun newAdaptiveDefectAudit(code: String, label: String): AdaptiveDefectAudit {
    return AdaptiveDefectAudit(
        code = code,
        label = label,
        updatedAt = 0,
        active = false,
        loadKg = 0.0,
        routeCode = "",
        fuelLiters = 0.0,
        status = AdaptiveDefectAuditStatus.IDLE,
    )
}
