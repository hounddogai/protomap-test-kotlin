package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional service plan baseline is in its life cycle. */
enum class RegionalServicePlanBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a regional service plan baseline and its current status. */
data class RegionalServicePlanBaseline(
    var code: String,
    var label: String,
    var loadKg: Double,
    var dock: String,
    var capacity: Int,
    var maximum: Double,
    var unit: String,
    var graceDays: Int,
    var active: Boolean,
    var status: RegionalServicePlanBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalServicePlanBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalServicePlanBaselineStatus): Boolean {
        when (status) {
            RegionalServicePlanBaselineStatus.OPEN -> {
                when (next) {
                    RegionalServicePlanBaselineStatus.IN_REVIEW -> return true
                }
                return false
            }
            RegionalServicePlanBaselineStatus.IN_REVIEW -> return next == RegionalServicePlanBaselineStatus.RESOLVED || next == RegionalServicePlanBaselineStatus.REOPENED
            RegionalServicePlanBaselineStatus.RESOLVED -> return next == RegionalServicePlanBaselineStatus.REOPENED || next == RegionalServicePlanBaselineStatus.ARCHIVED
            RegionalServicePlanBaselineStatus.REOPENED -> {
                when (next) {
                    RegionalServicePlanBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalServicePlanBaselineStatus) {
        if (!canMoveTo(next)) {
            error("regional service plan baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netVolume(): Double = round((graceDays.toDouble() - loadKg) * 100) / 100

    fun snapshot(): RegionalServicePlanBaseline {
        return RegionalServicePlanBaseline(
            code = code,
            label = label,
            loadKg = loadKg,
            dock = dock,
            capacity = capacity,
            maximum = maximum,
            unit = unit,
            graceDays = graceDays,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional service plan baseline in status open. */
fun newRegionalServicePlanBaseline(code: String, label: String): RegionalServicePlanBaseline {
    return RegionalServicePlanBaseline(
        code = code,
        label = label,
        loadKg = 0.0,
        dock = "",
        capacity = 0,
        maximum = 0.0,
        unit = "",
        graceDays = 0,
        active = false,
        status = RegionalServicePlanBaselineStatus.OPEN,
    )
}
