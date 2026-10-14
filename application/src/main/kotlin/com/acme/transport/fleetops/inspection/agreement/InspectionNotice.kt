package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection notice is in its life cycle. */
enum class InspectionNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One inspection notice, with what the planners need to act on it. */
data class InspectionNotice(
    var code: String,
    var name: String,
    var unit: String,
    var vehicleId: String,
    var depot: String,
    var score: Double,
    var budget: Double,
    var routeCode: String,
    var status: InspectionNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionNoticeStatus): Boolean {
        when (status) {
            InspectionNoticeStatus.NEW -> return next == InspectionNoticeStatus.QUEUED
            InspectionNoticeStatus.QUEUED -> {
                when (next) {
                    InspectionNoticeStatus.DONE -> return true
                }
                return false
            }
            InspectionNoticeStatus.RUNNING -> return next == InspectionNoticeStatus.FAILED
            InspectionNoticeStatus.DONE -> return next == InspectionNoticeStatus.FAILED
        }
        return false
    }

    fun moveTo(next: InspectionNoticeStatus) {
        if (!canMoveTo(next)) {
            error("inspection notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveLoad(): Double = round((score - budget) * 100) / 100

    fun snapshot(): InspectionNotice {
        return InspectionNotice(
            code = code,
            name = name,
            unit = unit,
            vehicleId = vehicleId,
            depot = depot,
            score = score,
            budget = budget,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a inspection notice in status new. */
fun newInspectionNotice(code: String, name: String): InspectionNotice {
    return InspectionNotice(
        code = code,
        name = name,
        unit = "",
        vehicleId = "",
        depot = "",
        score = 0.0,
        budget = 0.0,
        routeCode = "",
        status = InspectionNoticeStatus.NEW,
    )
}
