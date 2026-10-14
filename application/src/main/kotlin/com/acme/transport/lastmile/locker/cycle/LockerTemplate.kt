package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker template is in its life cycle. */
enum class LockerTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a locker template and its current status. */
data class LockerTemplate(
    var code: String,
    var name: String,
    var shiftCode: String,
    var tags: MutableList<String>,
    var checksum: String,
    var quantity: Int,
    var title: String,
    var stopCount: Int,
    var lane: String,
    var margin: Double,
    var status: LockerTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerTemplateStatus): Boolean {
        when (status) {
            LockerTemplateStatus.PLANNED -> return next == LockerTemplateStatus.CANCELLED
            LockerTemplateStatus.SCHEDULED -> return next == LockerTemplateStatus.IN_PROGRESS
            LockerTemplateStatus.IN_PROGRESS -> return next == LockerTemplateStatus.CANCELLED
            LockerTemplateStatus.COMPLETED -> return next == LockerTemplateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LockerTemplateStatus) {
        if (!canMoveTo(next)) {
            error("locker template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"locker template"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round(margin * stopCount.toDouble() * 100) / 100

    fun snapshot(): LockerTemplate {
        return LockerTemplate(
            code = code,
            name = name,
            shiftCode = shiftCode,
            tags = tags,
            checksum = checksum,
            quantity = quantity,
            title = title,
            stopCount = stopCount,
            lane = lane,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a locker template in status planned. */
fun newLockerTemplate(code: String, name: String): LockerTemplate {
    return LockerTemplate(
        code = code,
        name = name,
        shiftCode = "",
        tags = mutableListOf<String>(),
        checksum = "",
        quantity = 0,
        title = "",
        stopCount = 0,
        lane = "",
        margin = 0.0,
        status = LockerTemplateStatus.PLANNED,
    )
}
