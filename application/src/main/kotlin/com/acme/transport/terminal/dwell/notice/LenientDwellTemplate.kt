package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.text.normalize

/** Where a lenient dwell template is in its life cycle. */
enum class LenientDwellTemplateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One lenient dwell template, with what the planners need to act on it. */
data class LenientDwellTemplate(
    var code: String,
    var title: String,
    var threshold: Double,
    var summary: String,
    var dock: String,
    var unit: String,
    var locale: String,
    var notes: String,
    var status: LenientDwellTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDwellTemplateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDwellTemplateStatus): Boolean {
        when (status) {
            LenientDwellTemplateStatus.REQUESTED -> return next == LenientDwellTemplateStatus.GRANTED
            LenientDwellTemplateStatus.GRANTED -> return next == LenientDwellTemplateStatus.EXPIRED
            LenientDwellTemplateStatus.REVOKED -> {
                when (next) {
                    LenientDwellTemplateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDwellTemplateStatus) {
        if (!canMoveTo(next)) {
            error("lenient dwell template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = threshold

    fun snapshot(): LenientDwellTemplate {
        return LenientDwellTemplate(
            code = code,
            title = title,
            threshold = threshold,
            summary = summary,
            dock = dock,
            unit = unit,
            locale = locale,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient dwell template in status requested. */
fun newLenientDwellTemplate(code: String, title: String): LenientDwellTemplate {
    return LenientDwellTemplate(
        code = code,
        title = title,
        threshold = 0.0,
        summary = "",
        dock = "",
        unit = "",
        locale = "",
        notes = "",
        status = LenientDwellTemplateStatus.REQUESTED,
    )
}
