package com.acme.transport.safety.coaching.coachingscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching audit is in its life cycle. */
enum class CoachingAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a coaching audit and its current status. */
data class CoachingAudit(
    var code: String,
    var title: String,
    var createdAt: Int,
    var archived: Boolean,
    var name: String,
    var weight: Double,
    var updatedAt: Int,
    var score: Double,
    var threshold: Double,
    var ownerId: String,
    var status: CoachingAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingAuditStatus): Boolean {
        when (status) {
            CoachingAuditStatus.PLANNED -> return next == CoachingAuditStatus.CANCELLED
            CoachingAuditStatus.SCHEDULED -> return next == CoachingAuditStatus.IN_PROGRESS || next == CoachingAuditStatus.COMPLETED
            CoachingAuditStatus.IN_PROGRESS -> return next == CoachingAuditStatus.COMPLETED || next == CoachingAuditStatus.CANCELLED
            CoachingAuditStatus.COMPLETED -> {
                when (next) {
                    CoachingAuditStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingAuditStatus) {
        if (!canMoveTo(next)) {
            error("coaching audit $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedLoad(): Double = round((createdAt.toDouble() - threshold) * 100) / 100

    fun snapshot(): CoachingAudit {
        return CoachingAudit(
            code = code,
            title = title,
            createdAt = createdAt,
            archived = archived,
            name = name,
            weight = weight,
            updatedAt = updatedAt,
            score = score,
            threshold = threshold,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a coaching audit in status planned. */
fun newCoachingAudit(code: String, title: String): CoachingAudit {
    return CoachingAudit(
        code = code,
        title = title,
        createdAt = 0,
        archived = false,
        name = "",
        weight = 0.0,
        updatedAt = 0,
        score = 0.0,
        threshold = 0.0,
        ownerId = "",
        status = CoachingAuditStatus.PLANNED,
    )
}
