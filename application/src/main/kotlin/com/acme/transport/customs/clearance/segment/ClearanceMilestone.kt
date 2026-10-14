package com.acme.transport.customs.clearance.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance milestone is in its life cycle. */
enum class ClearanceMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A clearance milestone. Its code is unique within a tenant. */
data class ClearanceMilestone(
    var code: String,
    var label: String,
    var visible: Boolean,
    var active: Boolean,
    var routeCode: String,
    var margin: Double,
    var ratio: Double,
    var retries: Int,
    var budget: Double,
    var revision: Int,
    var status: ClearanceMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceMilestoneStatus): Boolean {
        when (status) {
            ClearanceMilestoneStatus.REQUESTED -> {
                when (next) {
                    ClearanceMilestoneStatus.GRANTED, ClearanceMilestoneStatus.REVOKED -> return true
                }
                return false
            }
            ClearanceMilestoneStatus.GRANTED -> return next == ClearanceMilestoneStatus.REVOKED || next == ClearanceMilestoneStatus.EXPIRED
            ClearanceMilestoneStatus.REVOKED -> return next == ClearanceMilestoneStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ClearanceMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("clearance milestone $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedLoad(): Double = round(revision.toDouble() * margin * 100) / 100

    fun snapshot(): ClearanceMilestone {
        return ClearanceMilestone(
            code = code,
            label = label,
            visible = visible,
            active = active,
            routeCode = routeCode,
            margin = margin,
            ratio = ratio,
            retries = retries,
            budget = budget,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a clearance milestone in status requested. */
fun newClearanceMilestone(code: String, label: String): ClearanceMilestone {
    return ClearanceMilestone(
        code = code,
        label = label,
        visible = false,
        active = false,
        routeCode = "",
        margin = 0.0,
        ratio = 0.0,
        retries = 0,
        budget = 0.0,
        revision = 0,
        status = ClearanceMilestoneStatus.REQUESTED,
    )
}
