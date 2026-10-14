package com.acme.transport.safety.nearmiss.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The near miss milestone records of safety's near miss work, and the rules, stores, and calculations around
// them.

/** Where a near miss milestone is in its life cycle. */
enum class NearMissMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a near miss milestone and its current status. */
data class NearMissMilestone(
    var code: String,
    var label: String,
    var depot: String,
    var floorValue: Double,
    var batchSize: Int,
    var archived: Boolean,
    var createdAt: Int,
    var amount: Double,
    var unit: String,
    var updatedAt: Int,
    var weight: Double,
    var status: NearMissMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissMilestoneStatus): Boolean {
        when (status) {
            NearMissMilestoneStatus.DRAFT -> return next == NearMissMilestoneStatus.CLOSED
            NearMissMilestoneStatus.PENDING -> {
                when (next) {
                    NearMissMilestoneStatus.ACTIVE -> return true
                }
                return false
            }
            NearMissMilestoneStatus.ACTIVE -> return next == NearMissMilestoneStatus.SUSPENDED || next == NearMissMilestoneStatus.CLOSED
            NearMissMilestoneStatus.SUSPENDED -> return next == NearMissMilestoneStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: NearMissMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("near miss milestone $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedExposure(): Double = round((weight - floorValue) * 100) / 100

    fun snapshot(): NearMissMilestone {
        return NearMissMilestone(
            code = code,
            label = label,
            depot = depot,
            floorValue = floorValue,
            batchSize = batchSize,
            archived = archived,
            createdAt = createdAt,
            amount = amount,
            unit = unit,
            updatedAt = updatedAt,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a near miss milestone in status draft. */
fun newNearMissMilestone(code: String, label: String): NearMissMilestone {
    return NearMissMilestone(
        code = code,
        label = label,
        depot = "",
        floorValue = 0.0,
        batchSize = 0,
        archived = false,
        createdAt = 0,
        amount = 0.0,
        unit = "",
        updatedAt = 0,
        weight = 0.0,
        status = NearMissMilestoneStatus.DRAFT,
    )
}
