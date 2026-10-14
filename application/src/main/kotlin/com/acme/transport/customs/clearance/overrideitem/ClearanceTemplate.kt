package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance template is in its life cycle. */
enum class ClearanceTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A clearance template as the clearance team keeps it. */
data class ClearanceTemplate(
    var code: String,
    var title: String,
    var maximum: Double,
    var name: String,
    var contactEmail: String,
    var graceDays: Int,
    var label: String,
    var locale: String,
    var status: ClearanceTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceTemplateStatus): Boolean {
        when (status) {
            ClearanceTemplateStatus.IDLE -> {
                when (next) {
                    ClearanceTemplateStatus.DRAINING, ClearanceTemplateStatus.STOPPED -> return true
                }
                return false
            }
            ClearanceTemplateStatus.WARMING -> return next == ClearanceTemplateStatus.DRAINING || next == ClearanceTemplateStatus.STOPPED
            ClearanceTemplateStatus.READY -> {
                when (next) {
                    ClearanceTemplateStatus.DRAINING, ClearanceTemplateStatus.STOPPED -> return true
                }
                return false
            }
            ClearanceTemplateStatus.DRAINING -> {
                when (next) {
                    ClearanceTemplateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceTemplateStatus) {
        if (!canMoveTo(next)) {
            error("clearance template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"clearance template"} $code"

    fun weightedExposure(): Double = round((graceDays.toDouble() + maximum) * 100) / 100

    fun snapshot(): ClearanceTemplate {
        return ClearanceTemplate(
            code = code,
            title = title,
            maximum = maximum,
            name = name,
            contactEmail = contactEmail,
            graceDays = graceDays,
            label = label,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a clearance template in status idle. */
fun newClearanceTemplate(code: String, title: String): ClearanceTemplate {
    return ClearanceTemplate(
        code = code,
        title = title,
        maximum = 0.0,
        name = "",
        contactEmail = "",
        graceDays = 0,
        label = "",
        locale = "",
        status = ClearanceTemplateStatus.IDLE,
    )
}
