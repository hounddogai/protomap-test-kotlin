package com.acme.transport.fleetops.defect.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect milestone is in its life cycle. */
enum class DefectMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A defect milestone. Its code is unique within a tenant. */
data class DefectMilestone(
    var code: String,
    var label: String,
    var name: String,
    var discount: Double,
    var ceiling: Double,
    var limit: Int,
    var amount: Double,
    var fuelLiters: Double,
    var distanceKm: Double,
    var expiresAt: Int,
    var status: DefectMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectMilestoneStatus): Boolean {
        when (status) {
            DefectMilestoneStatus.REQUESTED -> {
                when (next) {
                    DefectMilestoneStatus.GRANTED, DefectMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
            DefectMilestoneStatus.GRANTED -> return next == DefectMilestoneStatus.REVOKED || next == DefectMilestoneStatus.EXPIRED
            DefectMilestoneStatus.REVOKED -> return next == DefectMilestoneStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefectMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("defect milestone $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedTotal(): Double = round((discount + amount) * 100) / 100

    fun snapshot(): DefectMilestone {
        return DefectMilestone(
            code = code,
            label = label,
            name = name,
            discount = discount,
            ceiling = ceiling,
            limit = limit,
            amount = amount,
            fuelLiters = fuelLiters,
            distanceKm = distanceKm,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a defect milestone in status requested. */
fun newDefectMilestone(code: String, label: String): DefectMilestone {
    return DefectMilestone(
        code = code,
        label = label,
        name = "",
        discount = 0.0,
        ceiling = 0.0,
        limit = 0,
        amount = 0.0,
        fuelLiters = 0.0,
        distanceKm = 0.0,
        expiresAt = 0,
        status = DefectMilestoneStatus.REQUESTED,
    )
}
