package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal near miss assignment is in its life cycle. */
enum class SeasonalNearMissAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal near miss assignment. Its code is unique within a tenant. */
data class SeasonalNearMissAssignment(
    var code: String,
    var name: String,
    var cost: Double,
    var floorValue: Double,
    var maximum: Double,
    var active: Boolean,
    var batchSize: Int,
    var ownerId: String,
    var notes: String,
    var budget: Double,
    var contactEmail: String,
    var status: SeasonalNearMissAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalNearMissAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalNearMissAssignmentStatus): Boolean {
        when (status) {
            SeasonalNearMissAssignmentStatus.NEW -> return next == SeasonalNearMissAssignmentStatus.QUEUED || next == SeasonalNearMissAssignmentStatus.RUNNING
            SeasonalNearMissAssignmentStatus.QUEUED -> return next == SeasonalNearMissAssignmentStatus.DONE || next == SeasonalNearMissAssignmentStatus.FAILED
            SeasonalNearMissAssignmentStatus.RUNNING -> {
                when (next) {
                    SeasonalNearMissAssignmentStatus.DONE, SeasonalNearMissAssignmentStatus.FAILED -> return true
                }
                return false
            }
            SeasonalNearMissAssignmentStatus.DONE -> {
                when (next) {
                    SeasonalNearMissAssignmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalNearMissAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal near miss assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal near miss assignment"} $code"

    fun netLoad(): Double = round((floorValue - cost) * 100) / 100

    fun snapshot(): SeasonalNearMissAssignment {
        return SeasonalNearMissAssignment(
            code = code,
            name = name,
            cost = cost,
            floorValue = floorValue,
            maximum = maximum,
            active = active,
            batchSize = batchSize,
            ownerId = ownerId,
            notes = notes,
            budget = budget,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal near miss assignment in status new. */
fun newSeasonalNearMissAssignment(code: String, name: String): SeasonalNearMissAssignment {
    return SeasonalNearMissAssignment(
        code = code,
        name = name,
        cost = 0.0,
        floorValue = 0.0,
        maximum = 0.0,
        active = false,
        batchSize = 0,
        ownerId = "",
        notes = "",
        budget = 0.0,
        contactEmail = "",
        status = SeasonalNearMissAssignmentStatus.NEW,
    )
}
