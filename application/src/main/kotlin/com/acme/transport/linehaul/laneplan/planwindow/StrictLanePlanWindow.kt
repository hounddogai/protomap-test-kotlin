package com.acme.transport.linehaul.laneplan.planwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict lane plan window records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a strict lane plan window is in its life cycle. */
enum class StrictLanePlanWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict lane plan window. Its code is unique within a tenant. */
data class StrictLanePlanWindow(
    var code: String,
    var label: String,
    var limit: Int,
    var checksum: String,
    var name: String,
    var loadKg: Double,
    var version: Int,
    var reviewer: String,
    var etaMinutes: Int,
    var windowDays: Int,
    var category: String,
    var status: StrictLanePlanWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictLanePlanWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictLanePlanWindowStatus): Boolean {
        when (status) {
            StrictLanePlanWindowStatus.DRAFT -> return next == StrictLanePlanWindowStatus.SUSPENDED || next == StrictLanePlanWindowStatus.CLOSED
            StrictLanePlanWindowStatus.PENDING -> {
                when (next) {
                    StrictLanePlanWindowStatus.ACTIVE, StrictLanePlanWindowStatus.CLOSED -> return true
                }
                return false
            }
            StrictLanePlanWindowStatus.ACTIVE -> {
                when (next) {
                    StrictLanePlanWindowStatus.SUSPENDED, StrictLanePlanWindowStatus.CLOSED -> return true
                }
                return false
            }
            StrictLanePlanWindowStatus.SUSPENDED -> return next == StrictLanePlanWindowStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictLanePlanWindowStatus) {
        if (!canMoveTo(next)) {
            error("strict lane plan window $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"strict lane plan window"} $code"

    fun effectiveTotal(): Double = round((version.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): StrictLanePlanWindow {
        return StrictLanePlanWindow(
            code = code,
            label = label,
            limit = limit,
            checksum = checksum,
            name = name,
            loadKg = loadKg,
            version = version,
            reviewer = reviewer,
            etaMinutes = etaMinutes,
            windowDays = windowDays,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a strict lane plan window in status draft. */
fun newStrictLanePlanWindow(code: String, label: String): StrictLanePlanWindow {
    return StrictLanePlanWindow(
        code = code,
        label = label,
        limit = 0,
        checksum = "",
        name = "",
        loadKg = 0.0,
        version = 0,
        reviewer = "",
        etaMinutes = 0,
        windowDays = 0,
        category = "",
        status = StrictLanePlanWindowStatus.DRAFT,
    )
}
