package com.acme.transport.lastmile.locker.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker milestone is in its life cycle. */
enum class LockerMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One locker milestone, with what the planners need to act on it. */
data class LockerMilestone(
    var code: String,
    var title: String,
    var revision: Int,
    var archived: Boolean,
    var reviewer: String,
    var batchSize: Int,
    var trailerId: String,
    var ratio: Double,
    var graceDays: Int,
    var score: Double,
    var createdAt: Int,
    var status: LockerMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerMilestoneStatus): Boolean {
        when (status) {
            LockerMilestoneStatus.CREATED -> {
                when (next) {
                    LockerMilestoneStatus.VALIDATED -> return true
                }
                return false
            }
            LockerMilestoneStatus.VALIDATED -> {
                when (next) {
                    LockerMilestoneStatus.PUBLISHED, LockerMilestoneStatus.RETIRED -> return true
                }
                return false
            }
            LockerMilestoneStatus.PUBLISHED -> return next == LockerMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LockerMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("locker milestone $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun effectiveExposure(): Double = round(batchSize.toDouble() * score * 100) / 100

    fun snapshot(): LockerMilestone {
        return LockerMilestone(
            code = code,
            title = title,
            revision = revision,
            archived = archived,
            reviewer = reviewer,
            batchSize = batchSize,
            trailerId = trailerId,
            ratio = ratio,
            graceDays = graceDays,
            score = score,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a locker milestone in status created. */
fun newLockerMilestone(code: String, title: String): LockerMilestone {
    return LockerMilestone(
        code = code,
        title = title,
        revision = 0,
        archived = false,
        reviewer = "",
        batchSize = 0,
        trailerId = "",
        ratio = 0.0,
        graceDays = 0,
        score = 0.0,
        createdAt = 0,
        status = LockerMilestoneStatus.CREATED,
    )
}
