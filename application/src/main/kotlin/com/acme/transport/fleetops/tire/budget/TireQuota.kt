package com.acme.transport.fleetops.tire.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire quota is in its life cycle. */
enum class TireQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tire quota, with what the planners need to act on it. */
data class TireQuota(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var endsAt: Int,
    var phone: String,
    var priority: Int,
    var approved: Boolean,
    var floorValue: Double,
    var segment: String,
    var reviewer: String,
    var status: TireQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireQuotaStatus): Boolean {
        when (status) {
            TireQuotaStatus.IDLE -> {
                when (next) {
                    TireQuotaStatus.WARMING -> return true
                }
                return false
            }
            TireQuotaStatus.WARMING -> {
                when (next) {
                    TireQuotaStatus.READY -> return true
                }
                return false
            }
            TireQuotaStatus.READY -> return next == TireQuotaStatus.DRAINING
            TireQuotaStatus.DRAINING -> return next == TireQuotaStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TireQuotaStatus) {
        if (!canMoveTo(next)) {
            error("tire quota $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveVolume(): Double = round((priority.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): TireQuota {
        return TireQuota(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            endsAt = endsAt,
            phone = phone,
            priority = priority,
            approved = approved,
            floorValue = floorValue,
            segment = segment,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tire quota in status idle. */
fun newTireQuota(code: String, label: String): TireQuota {
    return TireQuota(
        code = code,
        label = label,
        durationMinutes = 0,
        endsAt = 0,
        phone = "",
        priority = 0,
        approved = false,
        floorValue = 0.0,
        segment = "",
        reviewer = "",
        status = TireQuotaStatus.IDLE,
    )
}
