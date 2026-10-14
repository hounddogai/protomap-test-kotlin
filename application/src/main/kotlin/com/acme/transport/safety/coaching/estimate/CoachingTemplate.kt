package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching template is in its life cycle. */
enum class CoachingTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One coaching template, with what the planners need to act on it. */
data class CoachingTemplate(
    var code: String,
    var label: String,
    var capacity: Int,
    var version: Int,
    var ownerId: String,
    var currency: String,
    var status: CoachingTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingTemplateStatus): Boolean {
        when (status) {
            CoachingTemplateStatus.IDLE -> return next == CoachingTemplateStatus.WARMING || next == CoachingTemplateStatus.STOPPED
            CoachingTemplateStatus.WARMING -> return next == CoachingTemplateStatus.STOPPED
            CoachingTemplateStatus.READY -> {
                when (next) {
                    CoachingTemplateStatus.DRAINING -> return true
                }
                return false
            }
            CoachingTemplateStatus.DRAINING -> return next == CoachingTemplateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CoachingTemplateStatus) {
        if (!canMoveTo(next)) {
            error("coaching template $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedTotal(): Double = round(version.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): CoachingTemplate {
        return CoachingTemplate(
            code = code,
            label = label,
            capacity = capacity,
            version = version,
            ownerId = ownerId,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a coaching template in status idle. */
fun newCoachingTemplate(code: String, label: String): CoachingTemplate {
    return CoachingTemplate(
        code = code,
        label = label,
        capacity = 0,
        version = 0,
        ownerId = "",
        currency = "",
        status = CoachingTemplateStatus.IDLE,
    )
}
