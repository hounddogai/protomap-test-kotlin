package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification milestone records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a qualification milestone is in its life cycle. */
enum class QualificationMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A qualification milestone. Its code is unique within a tenant. */
data class QualificationMilestone(
    var code: String,
    var name: String,
    var weight: Double,
    var lane: String,
    var endsAt: Int,
    var capacity: Int,
    var title: String,
    var floorValue: Double,
    var status: QualificationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationMilestoneStatus): Boolean {
        when (status) {
            QualificationMilestoneStatus.OPEN -> return next == QualificationMilestoneStatus.REOPENED
            QualificationMilestoneStatus.IN_REVIEW -> return next == QualificationMilestoneStatus.ARCHIVED
            QualificationMilestoneStatus.RESOLVED -> return next == QualificationMilestoneStatus.REOPENED
            QualificationMilestoneStatus.REOPENED -> return next == QualificationMilestoneStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: QualificationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("qualification milestone $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round((endsAt.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): QualificationMilestone {
        return QualificationMilestone(
            code = code,
            name = name,
            weight = weight,
            lane = lane,
            endsAt = endsAt,
            capacity = capacity,
            title = title,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a qualification milestone in status open. */
fun newQualificationMilestone(code: String, name: String): QualificationMilestone {
    return QualificationMilestone(
        code = code,
        name = name,
        weight = 0.0,
        lane = "",
        endsAt = 0,
        capacity = 0,
        title = "",
        floorValue = 0.0,
        status = QualificationMilestoneStatus.OPEN,
    )
}
