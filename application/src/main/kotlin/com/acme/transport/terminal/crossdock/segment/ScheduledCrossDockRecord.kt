package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled cross dock record is in its life cycle. */
enum class ScheduledCrossDockRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scheduled cross dock record as the cross dock team keeps it. */
data class ScheduledCrossDockRecord(
    var code: String,
    var title: String,
    var trailerId: String,
    var graceDays: Int,
    var discount: Double,
    var etaMinutes: Int,
    var segment: String,
    var rate: Double,
    var revision: Int,
    var status: ScheduledCrossDockRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCrossDockRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCrossDockRecordStatus): Boolean {
        when (status) {
            ScheduledCrossDockRecordStatus.PLANNED -> {
                when (next) {
                    ScheduledCrossDockRecordStatus.SCHEDULED, ScheduledCrossDockRecordStatus.COMPLETED -> return true
                }
                return false
            }
            ScheduledCrossDockRecordStatus.SCHEDULED -> {
                when (next) {
                    ScheduledCrossDockRecordStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ScheduledCrossDockRecordStatus.IN_PROGRESS -> {
                when (next) {
                    ScheduledCrossDockRecordStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledCrossDockRecordStatus.COMPLETED -> return next == ScheduledCrossDockRecordStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledCrossDockRecordStatus) {
        if (!canMoveTo(next)) {
            error("scheduled cross dock record $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"scheduled cross dock record"} $code"

    fun netExposure(): Double = round((etaMinutes.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): ScheduledCrossDockRecord {
        return ScheduledCrossDockRecord(
            code = code,
            title = title,
            trailerId = trailerId,
            graceDays = graceDays,
            discount = discount,
            etaMinutes = etaMinutes,
            segment = segment,
            rate = rate,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled cross dock record in status planned. */
fun newScheduledCrossDockRecord(code: String, title: String): ScheduledCrossDockRecord {
    return ScheduledCrossDockRecord(
        code = code,
        title = title,
        trailerId = "",
        graceDays = 0,
        discount = 0.0,
        etaMinutes = 0,
        segment = "",
        rate = 0.0,
        revision = 0,
        status = ScheduledCrossDockRecordStatus.PLANNED,
    )
}
