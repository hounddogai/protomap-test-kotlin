package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.text.normalize

/** Where a tiered trailer template is in its life cycle. */
enum class TieredTrailerTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One tiered trailer template, with what the planners need to act on it. */
data class TieredTrailerTemplate(
    var code: String,
    var label: String,
    var driverId: String,
    var margin: Double,
    var title: String,
    var status: TieredTrailerTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTrailerTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTrailerTemplateStatus): Boolean {
        when (status) {
            TieredTrailerTemplateStatus.PLANNED -> return next == TieredTrailerTemplateStatus.IN_PROGRESS
            TieredTrailerTemplateStatus.SCHEDULED -> return next == TieredTrailerTemplateStatus.COMPLETED || next == TieredTrailerTemplateStatus.CANCELLED
            TieredTrailerTemplateStatus.IN_PROGRESS -> {
                when (next) {
                    TieredTrailerTemplateStatus.CANCELLED -> return true
                }
                return false
            }
            TieredTrailerTemplateStatus.COMPLETED -> return next == TieredTrailerTemplateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredTrailerTemplateStatus) {
        if (!canMoveTo(next)) {
            error("tiered trailer template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = margin

    fun snapshot(): TieredTrailerTemplate {
        return TieredTrailerTemplate(
            code = code,
            label = label,
            driverId = driverId,
            margin = margin,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered trailer template in status planned. */
fun newTieredTrailerTemplate(code: String, label: String): TieredTrailerTemplate {
    return TieredTrailerTemplate(
        code = code,
        label = label,
        driverId = "",
        margin = 0.0,
        title = "",
        status = TieredTrailerTemplateStatus.PLANNED,
    )
}
