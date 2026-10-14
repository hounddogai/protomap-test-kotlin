package com.acme.transport.crewing.availability.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability notice is in its life cycle. */
enum class AvailabilityNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A availability notice as the availability team keeps it. */
data class AvailabilityNotice(
    var code: String,
    var name: String,
    var summary: String,
    var windowDays: Int,
    var maximum: Double,
    var graceDays: Int,
    var vehicleId: String,
    var durationMinutes: Int,
    var carrier: String,
    var description: String,
    var discount: Double,
    var status: AvailabilityNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityNoticeStatus): Boolean {
        when (status) {
            AvailabilityNoticeStatus.PROPOSED -> return next == AvailabilityNoticeStatus.APPROVED
            AvailabilityNoticeStatus.APPROVED -> {
                when (next) {
                    AvailabilityNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
            AvailabilityNoticeStatus.REJECTED -> return next == AvailabilityNoticeStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AvailabilityNoticeStatus) {
        if (!canMoveTo(next)) {
            error("availability notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedValue(): Double = round(windowDays.toDouble() * maximum * 100) / 100

    fun snapshot(): AvailabilityNotice {
        return AvailabilityNotice(
            code = code,
            name = name,
            summary = summary,
            windowDays = windowDays,
            maximum = maximum,
            graceDays = graceDays,
            vehicleId = vehicleId,
            durationMinutes = durationMinutes,
            carrier = carrier,
            description = description,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a availability notice in status proposed. */
fun newAvailabilityNotice(code: String, name: String): AvailabilityNotice {
    return AvailabilityNotice(
        code = code,
        name = name,
        summary = "",
        windowDays = 0,
        maximum = 0.0,
        graceDays = 0,
        vehicleId = "",
        durationMinutes = 0,
        carrier = "",
        description = "",
        discount = 0.0,
        status = AvailabilityNoticeStatus.PROPOSED,
    )
}
