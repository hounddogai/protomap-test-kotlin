package com.acme.transport.network.zone.zoneagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled zone notice is in its life cycle. */
enum class ScheduledZoneNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scheduled zone notice. Its code is unique within a tenant. */
data class ScheduledZoneNotice(
    var code: String,
    var name: String,
    var threshold: Double,
    var retries: Int,
    var approved: Boolean,
    var etaMinutes: Int,
    var startsAt: Int,
    var version: Int,
    var status: ScheduledZoneNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledZoneNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledZoneNoticeStatus): Boolean {
        when (status) {
            ScheduledZoneNoticeStatus.PLANNED -> return next == ScheduledZoneNoticeStatus.SCHEDULED || next == ScheduledZoneNoticeStatus.IN_PROGRESS
            ScheduledZoneNoticeStatus.SCHEDULED -> return next == ScheduledZoneNoticeStatus.CANCELLED
            ScheduledZoneNoticeStatus.IN_PROGRESS -> return next == ScheduledZoneNoticeStatus.COMPLETED
            ScheduledZoneNoticeStatus.COMPLETED -> return next == ScheduledZoneNoticeStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledZoneNoticeStatus) {
        if (!canMoveTo(next)) {
            error("scheduled zone notice $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"scheduled zone notice"} $code"

    fun projectedTotal(): Double = round((retries.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledZoneNotice {
        return ScheduledZoneNotice(
            code = code,
            name = name,
            threshold = threshold,
            retries = retries,
            approved = approved,
            etaMinutes = etaMinutes,
            startsAt = startsAt,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled zone notice in status planned. */
fun newScheduledZoneNotice(code: String, name: String): ScheduledZoneNotice {
    return ScheduledZoneNotice(
        code = code,
        name = name,
        threshold = 0.0,
        retries = 0,
        approved = false,
        etaMinutes = 0,
        startsAt = 0,
        version = 0,
        status = ScheduledZoneNoticeStatus.PLANNED,
    )
}
