package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.kit.text.normalize

/** Where a default inspection notice is in its life cycle. */
enum class DefaultInspectionNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default inspection notice as the inspection team keeps it. */
data class DefaultInspectionNotice(
    var code: String,
    var label: String,
    var segment: String,
    var notes: String,
    var approved: Boolean,
    var description: String,
    var durationMinutes: Int,
    var status: DefaultInspectionNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultInspectionNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultInspectionNoticeStatus): Boolean {
        when (status) {
            DefaultInspectionNoticeStatus.CREATED -> {
                when (next) {
                    DefaultInspectionNoticeStatus.VALIDATED, DefaultInspectionNoticeStatus.PUBLISHED -> return true
                }
                return false
            }
            DefaultInspectionNoticeStatus.VALIDATED -> return next == DefaultInspectionNoticeStatus.PUBLISHED || next == DefaultInspectionNoticeStatus.RETIRED
            DefaultInspectionNoticeStatus.PUBLISHED -> {
                when (next) {
                    DefaultInspectionNoticeStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultInspectionNoticeStatus) {
        if (!canMoveTo(next)) {
            error("default inspection notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default inspection notice"} $code"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): DefaultInspectionNotice {
        return DefaultInspectionNotice(
            code = code,
            label = label,
            segment = segment,
            notes = notes,
            approved = approved,
            description = description,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default inspection notice in status created. */
fun newDefaultInspectionNotice(code: String, label: String): DefaultInspectionNotice {
    return DefaultInspectionNotice(
        code = code,
        label = label,
        segment = "",
        notes = "",
        approved = false,
        description = "",
        durationMinutes = 0,
        status = DefaultInspectionNoticeStatus.CREATED,
    )
}
