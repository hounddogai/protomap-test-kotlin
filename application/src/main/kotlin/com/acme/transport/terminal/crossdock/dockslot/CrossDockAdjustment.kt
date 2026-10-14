package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock adjustment is in its life cycle. */
enum class CrossDockAdjustmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A cross dock adjustment. Its code is unique within a tenant. */
data class CrossDockAdjustment(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var etaMinutes: Int,
    var country: String,
    var windowDays: Int,
    var stopCount: Int,
    var driverId: String,
    var label: String,
    var endsAt: Int,
    var cost: Double,
    var status: CrossDockAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockAdjustmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockAdjustmentStatus): Boolean {
        when (status) {
            CrossDockAdjustmentStatus.OPEN -> {
                when (next) {
                    CrossDockAdjustmentStatus.RESOLVED, CrossDockAdjustmentStatus.ARCHIVED -> return true
                }
                return false
            }
            CrossDockAdjustmentStatus.IN_REVIEW -> {
                when (next) {
                    CrossDockAdjustmentStatus.RESOLVED, CrossDockAdjustmentStatus.REOPENED -> return true
                }
                return false
            }
            CrossDockAdjustmentStatus.RESOLVED -> {
                when (next) {
                    CrossDockAdjustmentStatus.REOPENED -> return true
                }
                return false
            }
            CrossDockAdjustmentStatus.REOPENED -> return next == CrossDockAdjustmentStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CrossDockAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("cross dock adjustment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($name)"

    fun adjustedExposure(): Double = round(etaMinutes.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): CrossDockAdjustment {
        return CrossDockAdjustment(
            code = code,
            name = name,
            expiresAt = expiresAt,
            etaMinutes = etaMinutes,
            country = country,
            windowDays = windowDays,
            stopCount = stopCount,
            driverId = driverId,
            label = label,
            endsAt = endsAt,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a cross dock adjustment in status open. */
fun newCrossDockAdjustment(code: String, name: String): CrossDockAdjustment {
    return CrossDockAdjustment(
        code = code,
        name = name,
        expiresAt = 0,
        etaMinutes = 0,
        country = "",
        windowDays = 0,
        stopCount = 0,
        driverId = "",
        label = "",
        endsAt = 0,
        cost = 0.0,
        status = CrossDockAdjustmentStatus.OPEN,
    )
}
