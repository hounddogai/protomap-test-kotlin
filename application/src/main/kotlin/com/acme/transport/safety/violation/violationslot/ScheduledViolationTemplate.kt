package com.acme.transport.safety.violation.violationslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled violation template is in its life cycle. */
enum class ScheduledViolationTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One scheduled violation template, with what the planners need to act on it. */
data class ScheduledViolationTemplate(
    var code: String,
    var name: String,
    var batchSize: Int,
    var label: String,
    var summary: String,
    var floorValue: Double,
    var contactEmail: String,
    var archived: Boolean,
    var status: ScheduledViolationTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledViolationTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledViolationTemplateStatus): Boolean {
        when (status) {
            ScheduledViolationTemplateStatus.IDLE -> {
                when (next) {
                    ScheduledViolationTemplateStatus.READY, ScheduledViolationTemplateStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledViolationTemplateStatus.WARMING -> {
                when (next) {
                    ScheduledViolationTemplateStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledViolationTemplateStatus.READY -> {
                when (next) {
                    ScheduledViolationTemplateStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledViolationTemplateStatus.DRAINING -> return next == ScheduledViolationTemplateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledViolationTemplateStatus) {
        if (!canMoveTo(next)) {
            error("scheduled violation template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled violation template"} $code"

    fun weightedVolume(): Double = round(batchSize.toDouble() * floorValue * 100) / 100

    fun snapshot(): ScheduledViolationTemplate {
        return ScheduledViolationTemplate(
            code = code,
            name = name,
            batchSize = batchSize,
            label = label,
            summary = summary,
            floorValue = floorValue,
            contactEmail = contactEmail,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled violation template in status idle. */
fun newScheduledViolationTemplate(code: String, name: String): ScheduledViolationTemplate {
    return ScheduledViolationTemplate(
        code = code,
        name = name,
        batchSize = 0,
        label = "",
        summary = "",
        floorValue = 0.0,
        contactEmail = "",
        archived = false,
        status = ScheduledViolationTemplateStatus.IDLE,
    )
}
