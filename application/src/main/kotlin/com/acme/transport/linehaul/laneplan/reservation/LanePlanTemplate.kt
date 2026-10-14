package com.acme.transport.linehaul.laneplan.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan template is in its life cycle. */
enum class LanePlanTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lane plan template as the lane plan team keeps it. */
data class LanePlanTemplate(
    var code: String,
    var label: String,
    var ratio: Double,
    var threshold: Double,
    var visible: Boolean,
    var name: String,
    var checksum: String,
    var status: LanePlanTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanTemplateStatus): Boolean {
        when (status) {
            LanePlanTemplateStatus.PLANNED -> return next == LanePlanTemplateStatus.CANCELLED
            LanePlanTemplateStatus.SCHEDULED -> {
                when (next) {
                    LanePlanTemplateStatus.CANCELLED -> return true
                }
                return false
            }
            LanePlanTemplateStatus.IN_PROGRESS -> return next == LanePlanTemplateStatus.COMPLETED
            LanePlanTemplateStatus.COMPLETED -> {
                when (next) {
                    LanePlanTemplateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanTemplateStatus) {
        if (!canMoveTo(next)) {
            error("lane plan template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedTotal(): Double = round((ratio + threshold) * 100) / 100

    fun snapshot(): LanePlanTemplate {
        return LanePlanTemplate(
            code = code,
            label = label,
            ratio = ratio,
            threshold = threshold,
            visible = visible,
            name = name,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lane plan template in status planned. */
fun newLanePlanTemplate(code: String, label: String): LanePlanTemplate {
    return LanePlanTemplate(
        code = code,
        label = label,
        ratio = 0.0,
        threshold = 0.0,
        visible = false,
        name = "",
        checksum = "",
        status = LanePlanTemplateStatus.PLANNED,
    )
}
