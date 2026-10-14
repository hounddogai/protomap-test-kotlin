package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual carbon report cycle is in its life cycle. */
enum class ManualCarbonReportCycleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A manual carbon report cycle as the carbon report team keeps it. */
data class ManualCarbonReportCycle(
    var code: String,
    var title: String,
    var stopCount: Int,
    var startsAt: Int,
    var tags: MutableList<String>,
    var carrier: String,
    var version: Int,
    var loadKg: Double,
    var driverId: String,
    var endsAt: Int,
    var score: Double,
    var status: ManualCarbonReportCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCarbonReportCycleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCarbonReportCycleStatus): Boolean {
        when (status) {
            ManualCarbonReportCycleStatus.PLANNED -> {
                when (next) {
                    ManualCarbonReportCycleStatus.SCHEDULED -> return true
                }
                return false
            }
            ManualCarbonReportCycleStatus.SCHEDULED -> return next == ManualCarbonReportCycleStatus.IN_PROGRESS
            ManualCarbonReportCycleStatus.IN_PROGRESS -> return next == ManualCarbonReportCycleStatus.CANCELLED
            ManualCarbonReportCycleStatus.COMPLETED -> {
                when (next) {
                    ManualCarbonReportCycleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualCarbonReportCycleStatus) {
        if (!canMoveTo(next)) {
            error("manual carbon report cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round((score - loadKg) * 100) / 100

    fun snapshot(): ManualCarbonReportCycle {
        return ManualCarbonReportCycle(
            code = code,
            title = title,
            stopCount = stopCount,
            startsAt = startsAt,
            tags = tags,
            carrier = carrier,
            version = version,
            loadKg = loadKg,
            driverId = driverId,
            endsAt = endsAt,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual carbon report cycle in status planned. */
fun newManualCarbonReportCycle(code: String, title: String): ManualCarbonReportCycle {
    return ManualCarbonReportCycle(
        code = code,
        title = title,
        stopCount = 0,
        startsAt = 0,
        tags = mutableListOf<String>(),
        carrier = "",
        version = 0,
        loadKg = 0.0,
        driverId = "",
        endsAt = 0,
        score = 0.0,
        status = ManualCarbonReportCycleStatus.PLANNED,
    )
}
