package com.acme.transport.emissions.fuelburn.burnestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped fuel burn milestone is in its life cycle. */
enum class CappedFuelBurnMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One capped fuel burn milestone, with what the planners need to act on it. */
data class CappedFuelBurnMilestone(
    var code: String,
    var title: String,
    var discount: Double,
    var limit: Int,
    var batchSize: Int,
    var notes: String,
    var expiresAt: Int,
    var startsAt: Int,
    var status: CappedFuelBurnMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedFuelBurnMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedFuelBurnMilestoneStatus): Boolean {
        when (status) {
            CappedFuelBurnMilestoneStatus.OPEN -> {
                when (next) {
                    CappedFuelBurnMilestoneStatus.RESOLVED, CappedFuelBurnMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedFuelBurnMilestoneStatus.IN_REVIEW -> return next == CappedFuelBurnMilestoneStatus.RESOLVED || next == CappedFuelBurnMilestoneStatus.REOPENED
            CappedFuelBurnMilestoneStatus.RESOLVED -> {
                when (next) {
                    CappedFuelBurnMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedFuelBurnMilestoneStatus.REOPENED -> {
                when (next) {
                    CappedFuelBurnMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedFuelBurnMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("capped fuel burn milestone $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped fuel burn milestone"} $code"

    fun adjustedLoad(): Double = round((expiresAt.toDouble() - discount) * 100) / 100

    fun snapshot(): CappedFuelBurnMilestone {
        return CappedFuelBurnMilestone(
            code = code,
            title = title,
            discount = discount,
            limit = limit,
            batchSize = batchSize,
            notes = notes,
            expiresAt = expiresAt,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped fuel burn milestone in status open. */
fun newCappedFuelBurnMilestone(code: String, title: String): CappedFuelBurnMilestone {
    return CappedFuelBurnMilestone(
        code = code,
        title = title,
        discount = 0.0,
        limit = 0,
        batchSize = 0,
        notes = "",
        expiresAt = 0,
        startsAt = 0,
        status = CappedFuelBurnMilestoneStatus.OPEN,
    )
}
