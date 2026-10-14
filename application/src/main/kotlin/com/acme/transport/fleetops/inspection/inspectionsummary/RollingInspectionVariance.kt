package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling inspection variance is in its life cycle. */
enum class RollingInspectionVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling inspection variance as the inspection team keeps it. */
data class RollingInspectionVariance(
    var code: String,
    var name: String,
    var approved: Boolean,
    var pinned: Boolean,
    var label: String,
    var windowDays: Int,
    var odometer: Int,
    var country: String,
    var margin: Double,
    var status: RollingInspectionVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingInspectionVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingInspectionVarianceStatus): Boolean {
        when (status) {
            RollingInspectionVarianceStatus.NEW -> return next == RollingInspectionVarianceStatus.RUNNING || next == RollingInspectionVarianceStatus.FAILED
            RollingInspectionVarianceStatus.QUEUED -> return next == RollingInspectionVarianceStatus.DONE || next == RollingInspectionVarianceStatus.FAILED
            RollingInspectionVarianceStatus.RUNNING -> {
                when (next) {
                    RollingInspectionVarianceStatus.DONE, RollingInspectionVarianceStatus.FAILED -> return true
                }
                return false
            }
            RollingInspectionVarianceStatus.DONE -> return next == RollingInspectionVarianceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingInspectionVarianceStatus) {
        if (!canMoveTo(next)) {
            error("rolling inspection variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun weightedLoad(): Double = round(odometer.toDouble() * margin * 100) / 100

    fun snapshot(): RollingInspectionVariance {
        return RollingInspectionVariance(
            code = code,
            name = name,
            approved = approved,
            pinned = pinned,
            label = label,
            windowDays = windowDays,
            odometer = odometer,
            country = country,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rolling inspection variance in status new. */
fun newRollingInspectionVariance(code: String, name: String): RollingInspectionVariance {
    return RollingInspectionVariance(
        code = code,
        name = name,
        approved = false,
        pinned = false,
        label = "",
        windowDays = 0,
        odometer = 0,
        country = "",
        margin = 0.0,
        status = RollingInspectionVarianceStatus.NEW,
    )
}
