package com.acme.transport.fleetops.inspection.inspectionpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection baseline is in its life cycle. */
enum class InspectionBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One inspection baseline, with what the planners need to act on it. */
data class InspectionBaseline(
    var code: String,
    var label: String,
    var cost: Double,
    var contactEmail: String,
    var version: Int,
    var endsAt: Int,
    var score: Double,
    var title: String,
    var routeCode: String,
    var status: InspectionBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionBaselineStatus): Boolean {
        when (status) {
            InspectionBaselineStatus.REQUESTED -> return next == InspectionBaselineStatus.GRANTED || next == InspectionBaselineStatus.REVOKED
            InspectionBaselineStatus.GRANTED -> return next == InspectionBaselineStatus.EXPIRED
            InspectionBaselineStatus.REVOKED -> return next == InspectionBaselineStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: InspectionBaselineStatus) {
        if (!canMoveTo(next)) {
            error("inspection baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedValue(): Double = round((cost + endsAt.toDouble()) * 100) / 100

    fun snapshot(): InspectionBaseline {
        return InspectionBaseline(
            code = code,
            label = label,
            cost = cost,
            contactEmail = contactEmail,
            version = version,
            endsAt = endsAt,
            score = score,
            title = title,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection baseline in status requested. */
fun newInspectionBaseline(code: String, label: String): InspectionBaseline {
    return InspectionBaseline(
        code = code,
        label = label,
        cost = 0.0,
        contactEmail = "",
        version = 0,
        endsAt = 0,
        score = 0.0,
        title = "",
        routeCode = "",
        status = InspectionBaselineStatus.REQUESTED,
    )
}
