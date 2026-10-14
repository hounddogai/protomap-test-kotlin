package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy locker template is in its life cycle. */
enum class LegacyLockerTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One legacy locker template, with what the planners need to act on it. */
data class LegacyLockerTemplate(
    var code: String,
    var title: String,
    var archived: Boolean,
    var windowDays: Int,
    var ceiling: Double,
    var odometer: Int,
    var status: LegacyLockerTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLockerTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLockerTemplateStatus): Boolean {
        when (status) {
            LegacyLockerTemplateStatus.PLANNED -> {
                when (next) {
                    LegacyLockerTemplateStatus.COMPLETED -> return true
                }
                return false
            }
            LegacyLockerTemplateStatus.SCHEDULED -> return next == LegacyLockerTemplateStatus.IN_PROGRESS
            LegacyLockerTemplateStatus.IN_PROGRESS -> return next == LegacyLockerTemplateStatus.COMPLETED || next == LegacyLockerTemplateStatus.CANCELLED
            LegacyLockerTemplateStatus.COMPLETED -> return next == LegacyLockerTemplateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyLockerTemplateStatus) {
        if (!canMoveTo(next)) {
            error("legacy locker template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netValue(): Double = round((odometer.toDouble() + ceiling) * 100) / 100

    fun snapshot(): LegacyLockerTemplate {
        return LegacyLockerTemplate(
            code = code,
            title = title,
            archived = archived,
            windowDays = windowDays,
            ceiling = ceiling,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy locker template in status planned. */
fun newLegacyLockerTemplate(code: String, title: String): LegacyLockerTemplate {
    return LegacyLockerTemplate(
        code = code,
        title = title,
        archived = false,
        windowDays = 0,
        ceiling = 0.0,
        odometer = 0,
        status = LegacyLockerTemplateStatus.PLANNED,
    )
}
