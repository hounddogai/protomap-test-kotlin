package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled inspection claim is in its life cycle. */
enum class ScheduledInspectionClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a scheduled inspection claim and its current status. */
data class ScheduledInspectionClaim(
    var code: String,
    var name: String,
    var lane: String,
    var minimum: Double,
    var description: String,
    var stopCount: Int,
    var maximum: Double,
    var etaMinutes: Int,
    var timezone: String,
    var revision: Int,
    var phone: String,
    var status: ScheduledInspectionClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledInspectionClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledInspectionClaimStatus): Boolean {
        when (status) {
            ScheduledInspectionClaimStatus.DRAFT -> {
                when (next) {
                    ScheduledInspectionClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledInspectionClaimStatus.PENDING -> return next == ScheduledInspectionClaimStatus.SUSPENDED
            ScheduledInspectionClaimStatus.ACTIVE -> return next == ScheduledInspectionClaimStatus.SUSPENDED || next == ScheduledInspectionClaimStatus.CLOSED
            ScheduledInspectionClaimStatus.SUSPENDED -> {
                when (next) {
                    ScheduledInspectionClaimStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledInspectionClaimStatus) {
        if (!canMoveTo(next)) {
            error("scheduled inspection claim $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun effectiveTotal(): Double = round(revision.toDouble() * maximum * 100) / 100

    fun snapshot(): ScheduledInspectionClaim {
        return ScheduledInspectionClaim(
            code = code,
            name = name,
            lane = lane,
            minimum = minimum,
            description = description,
            stopCount = stopCount,
            maximum = maximum,
            etaMinutes = etaMinutes,
            timezone = timezone,
            revision = revision,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled inspection claim in status draft. */
fun newScheduledInspectionClaim(code: String, name: String): ScheduledInspectionClaim {
    return ScheduledInspectionClaim(
        code = code,
        name = name,
        lane = "",
        minimum = 0.0,
        description = "",
        stopCount = 0,
        maximum = 0.0,
        etaMinutes = 0,
        timezone = "",
        revision = 0,
        phone = "",
        status = ScheduledInspectionClaimStatus.DRAFT,
    )
}
