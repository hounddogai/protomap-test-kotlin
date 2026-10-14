package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional parcel schedule is in its life cycle. */
enum class RegionalParcelScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional parcel schedule and its current status. */
data class RegionalParcelSchedule(
    var code: String,
    var title: String,
    var description: String,
    var externalRef: String,
    var graceDays: Int,
    var rate: Double,
    var ceiling: Double,
    var visible: Boolean,
    var stopCount: Int,
    var version: Int,
    var endsAt: Int,
    var status: RegionalParcelScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalParcelScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalParcelScheduleStatus): Boolean {
        when (status) {
            RegionalParcelScheduleStatus.CREATED -> return next == RegionalParcelScheduleStatus.VALIDATED || next == RegionalParcelScheduleStatus.RETIRED
            RegionalParcelScheduleStatus.VALIDATED -> return next == RegionalParcelScheduleStatus.PUBLISHED || next == RegionalParcelScheduleStatus.RETIRED
            RegionalParcelScheduleStatus.PUBLISHED -> {
                when (next) {
                    RegionalParcelScheduleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalParcelScheduleStatus) {
        if (!canMoveTo(next)) {
            error("regional parcel schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"regional parcel schedule"} $code"

    fun effectiveValue(): Double = round(ceiling * rate * 100) / 100

    fun snapshot(): RegionalParcelSchedule {
        return RegionalParcelSchedule(
            code = code,
            title = title,
            description = description,
            externalRef = externalRef,
            graceDays = graceDays,
            rate = rate,
            ceiling = ceiling,
            visible = visible,
            stopCount = stopCount,
            version = version,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional parcel schedule in status created. */
fun newRegionalParcelSchedule(code: String, title: String): RegionalParcelSchedule {
    return RegionalParcelSchedule(
        code = code,
        title = title,
        description = "",
        externalRef = "",
        graceDays = 0,
        rate = 0.0,
        ceiling = 0.0,
        visible = false,
        stopCount = 0,
        version = 0,
        endsAt = 0,
        status = RegionalParcelScheduleStatus.CREATED,
    )
}
