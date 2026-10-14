package com.acme.transport.safety.coaching.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global coaching template is in its life cycle. */
enum class GlobalCoachingTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a global coaching template and its current status. */
data class GlobalCoachingTemplate(
    var code: String,
    var label: String,
    var priority: Int,
    var description: String,
    var durationMinutes: Int,
    var status: GlobalCoachingTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCoachingTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCoachingTemplateStatus): Boolean {
        when (status) {
            GlobalCoachingTemplateStatus.DRAFT -> return next == GlobalCoachingTemplateStatus.CLOSED
            GlobalCoachingTemplateStatus.PENDING -> return next == GlobalCoachingTemplateStatus.ACTIVE || next == GlobalCoachingTemplateStatus.SUSPENDED
            GlobalCoachingTemplateStatus.ACTIVE -> {
                when (next) {
                    GlobalCoachingTemplateStatus.SUSPENDED, GlobalCoachingTemplateStatus.CLOSED -> return true
                }
                return false
            }
            GlobalCoachingTemplateStatus.SUSPENDED -> return next == GlobalCoachingTemplateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: GlobalCoachingTemplateStatus) {
        if (!canMoveTo(next)) {
            error("global coaching template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global coaching template"} $code"

    fun effectiveLoad(): Double = round((durationMinutes.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): GlobalCoachingTemplate {
        return GlobalCoachingTemplate(
            code = code,
            label = label,
            priority = priority,
            description = description,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global coaching template in status draft. */
fun newGlobalCoachingTemplate(code: String, label: String): GlobalCoachingTemplate {
    return GlobalCoachingTemplate(
        code = code,
        label = label,
        priority = 0,
        description = "",
        durationMinutes = 0,
        status = GlobalCoachingTemplateStatus.DRAFT,
    )
}
