package com.acme.transport.customs.hold.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal hold audit is in its life cycle. */
enum class SeasonalHoldAuditStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a seasonal hold audit and its current status. */
data class SeasonalHoldAudit(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var loadKg: Double,
    var routeCode: String,
    var active: Boolean,
    var ownerId: String,
    var windowDays: Int,
    var priority: Int,
    var vehicleId: String,
    var ceiling: Double,
    var status: SeasonalHoldAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalHoldAuditStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalHoldAuditStatus): Boolean {
        when (status) {
            SeasonalHoldAuditStatus.REQUESTED -> return next == SeasonalHoldAuditStatus.GRANTED || next == SeasonalHoldAuditStatus.REVOKED
            SeasonalHoldAuditStatus.GRANTED -> return next == SeasonalHoldAuditStatus.EXPIRED
            SeasonalHoldAuditStatus.REVOKED -> {
                when (next) {
                    SeasonalHoldAuditStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalHoldAuditStatus) {
        if (!canMoveTo(next)) {
            error("seasonal hold audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveVolume(): Double = round((distanceKm - priority.toDouble()) * 100) / 100

    fun snapshot(): SeasonalHoldAudit {
        return SeasonalHoldAudit(
            code = code,
            title = title,
            distanceKm = distanceKm,
            loadKg = loadKg,
            routeCode = routeCode,
            active = active,
            ownerId = ownerId,
            windowDays = windowDays,
            priority = priority,
            vehicleId = vehicleId,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal hold audit in status requested. */
fun newSeasonalHoldAudit(code: String, title: String): SeasonalHoldAudit {
    return SeasonalHoldAudit(
        code = code,
        title = title,
        distanceKm = 0.0,
        loadKg = 0.0,
        routeCode = "",
        active = false,
        ownerId = "",
        windowDays = 0,
        priority = 0,
        vehicleId = "",
        ceiling = 0.0,
        status = SeasonalHoldAuditStatus.REQUESTED,
    )
}
