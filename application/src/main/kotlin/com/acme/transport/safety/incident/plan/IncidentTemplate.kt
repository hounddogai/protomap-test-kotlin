package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident template is in its life cycle. */
enum class IncidentTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One incident template, with what the planners need to act on it. */
data class IncidentTemplate(
    var code: String,
    var title: String,
    var active: Boolean,
    var updatedAt: Int,
    var ceiling: Double,
    var weight: Double,
    var status: IncidentTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentTemplateStatus): Boolean {
        when (status) {
            IncidentTemplateStatus.IDLE -> {
                when (next) {
                    IncidentTemplateStatus.DRAINING -> return true
                }
                return false
            }
            IncidentTemplateStatus.WARMING -> {
                when (next) {
                    IncidentTemplateStatus.STOPPED -> return true
                }
                return false
            }
            IncidentTemplateStatus.READY -> return next == IncidentTemplateStatus.DRAINING
            IncidentTemplateStatus.DRAINING -> return next == IncidentTemplateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: IncidentTemplateStatus) {
        if (!canMoveTo(next)) {
            error("incident template $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netVolume(): Double = round((ceiling + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): IncidentTemplate {
        return IncidentTemplate(
            code = code,
            title = title,
            active = active,
            updatedAt = updatedAt,
            ceiling = ceiling,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a incident template in status idle. */
fun newIncidentTemplate(code: String, title: String): IncidentTemplate {
    return IncidentTemplate(
        code = code,
        title = title,
        active = false,
        updatedAt = 0,
        ceiling = 0.0,
        weight = 0.0,
        status = IncidentTemplateStatus.IDLE,
    )
}
