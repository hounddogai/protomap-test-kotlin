package com.acme.transport.safety.coaching.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending coaching template records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a pending coaching template is in its life cycle. */
enum class PendingCoachingTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending coaching template as the coaching team keeps it. */
data class PendingCoachingTemplate(
    var code: String,
    var label: String,
    var floorValue: Double,
    var durationMinutes: Int,
    var pinned: Boolean,
    var visible: Boolean,
    var region: String,
    var title: String,
    var status: PendingCoachingTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCoachingTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCoachingTemplateStatus): Boolean {
        when (status) {
            PendingCoachingTemplateStatus.PLANNED -> return next == PendingCoachingTemplateStatus.SCHEDULED || next == PendingCoachingTemplateStatus.CANCELLED
            PendingCoachingTemplateStatus.SCHEDULED -> return next == PendingCoachingTemplateStatus.COMPLETED || next == PendingCoachingTemplateStatus.CANCELLED
            PendingCoachingTemplateStatus.IN_PROGRESS -> {
                when (next) {
                    PendingCoachingTemplateStatus.COMPLETED -> return true
                }
                return false
            }
            PendingCoachingTemplateStatus.COMPLETED -> {
                when (next) {
                    PendingCoachingTemplateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingCoachingTemplateStatus) {
        if (!canMoveTo(next)) {
            error("pending coaching template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedLoad(): Double = round((durationMinutes.toDouble() - floorValue) * 100) / 100

    fun snapshot(): PendingCoachingTemplate {
        return PendingCoachingTemplate(
            code = code,
            label = label,
            floorValue = floorValue,
            durationMinutes = durationMinutes,
            pinned = pinned,
            visible = visible,
            region = region,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending coaching template in status planned. */
fun newPendingCoachingTemplate(code: String, label: String): PendingCoachingTemplate {
    return PendingCoachingTemplate(
        code = code,
        label = label,
        floorValue = 0.0,
        durationMinutes = 0,
        pinned = false,
        visible = false,
        region = "",
        title = "",
        status = PendingCoachingTemplateStatus.PLANNED,
    )
}
