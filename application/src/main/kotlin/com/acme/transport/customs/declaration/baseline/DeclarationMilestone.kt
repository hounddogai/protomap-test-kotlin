package com.acme.transport.customs.declaration.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration milestone is in its life cycle. */
enum class DeclarationMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One declaration milestone, with what the planners need to act on it. */
data class DeclarationMilestone(
    var code: String,
    var label: String,
    var capacity: Int,
    var vehicleId: String,
    var trailerId: String,
    var startsAt: Int,
    var title: String,
    var approved: Boolean,
    var name: String,
    var limit: Int,
    var minimum: Double,
    var status: DeclarationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationMilestoneStatus): Boolean {
        when (status) {
            DeclarationMilestoneStatus.DRAFT -> return next == DeclarationMilestoneStatus.CLOSED
            DeclarationMilestoneStatus.PENDING -> return next == DeclarationMilestoneStatus.ACTIVE || next == DeclarationMilestoneStatus.SUSPENDED
            DeclarationMilestoneStatus.ACTIVE -> return next == DeclarationMilestoneStatus.SUSPENDED || next == DeclarationMilestoneStatus.CLOSED
            DeclarationMilestoneStatus.SUSPENDED -> return next == DeclarationMilestoneStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DeclarationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("declaration milestone $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedVolume(): Double = round((capacity.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): DeclarationMilestone {
        return DeclarationMilestone(
            code = code,
            label = label,
            capacity = capacity,
            vehicleId = vehicleId,
            trailerId = trailerId,
            startsAt = startsAt,
            title = title,
            approved = approved,
            name = name,
            limit = limit,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a declaration milestone in status draft. */
fun newDeclarationMilestone(code: String, label: String): DeclarationMilestone {
    return DeclarationMilestone(
        code = code,
        label = label,
        capacity = 0,
        vehicleId = "",
        trailerId = "",
        startsAt = 0,
        title = "",
        approved = false,
        name = "",
        limit = 0,
        minimum = 0.0,
        status = DeclarationMilestoneStatus.DRAFT,
    )
}
