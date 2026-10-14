package com.acme.transport.fleetops.defect.bundle

import com.acme.transport.kit.text.normalize

/** Where a scheduled defect claim is in its life cycle. */
enum class ScheduledDefectClaimStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A scheduled defect claim. Its code is unique within a tenant. */
data class ScheduledDefectClaim(
    var code: String,
    var title: String,
    var routeCode: String,
    var etaMinutes: Int,
    var timezone: String,
    var notes: String,
    var status: ScheduledDefectClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDefectClaimStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDefectClaimStatus): Boolean {
        when (status) {
            ScheduledDefectClaimStatus.PROPOSED -> {
                when (next) {
                    ScheduledDefectClaimStatus.APPROVED, ScheduledDefectClaimStatus.REJECTED -> return true
                }
                return false
            }
            ScheduledDefectClaimStatus.APPROVED -> return next == ScheduledDefectClaimStatus.REJECTED || next == ScheduledDefectClaimStatus.WITHDRAWN
            ScheduledDefectClaimStatus.REJECTED -> {
                when (next) {
                    ScheduledDefectClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledDefectClaimStatus) {
        if (!canMoveTo(next)) {
            error("scheduled defect claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled defect claim"} $code"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): ScheduledDefectClaim {
        return ScheduledDefectClaim(
            code = code,
            title = title,
            routeCode = routeCode,
            etaMinutes = etaMinutes,
            timezone = timezone,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled defect claim in status proposed. */
fun newScheduledDefectClaim(code: String, title: String): ScheduledDefectClaim {
    return ScheduledDefectClaim(
        code = code,
        title = title,
        routeCode = "",
        etaMinutes = 0,
        timezone = "",
        notes = "",
        status = ScheduledDefectClaimStatus.PROPOSED,
    )
}
