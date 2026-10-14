package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient dock door policy is in its life cycle. */
enum class LenientDockDoorPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lenient dock door policy. Its code is unique within a tenant. */
data class LenientDockDoorPolicy(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var summary: String,
    var carrier: String,
    var priority: Int,
    var phone: String,
    var limit: Int,
    var windowDays: Int,
    var timezone: String,
    var threshold: Double,
    var status: LenientDockDoorPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDockDoorPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDockDoorPolicyStatus): Boolean {
        when (status) {
            LenientDockDoorPolicyStatus.PLANNED -> {
                when (next) {
                    LenientDockDoorPolicyStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LenientDockDoorPolicyStatus.SCHEDULED -> return next == LenientDockDoorPolicyStatus.COMPLETED || next == LenientDockDoorPolicyStatus.CANCELLED
            LenientDockDoorPolicyStatus.IN_PROGRESS -> return next == LenientDockDoorPolicyStatus.COMPLETED || next == LenientDockDoorPolicyStatus.CANCELLED
            LenientDockDoorPolicyStatus.COMPLETED -> return next == LenientDockDoorPolicyStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LenientDockDoorPolicyStatus) {
        if (!canMoveTo(next)) {
            error("lenient dock door policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((priority.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): LenientDockDoorPolicy {
        return LenientDockDoorPolicy(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            summary = summary,
            carrier = carrier,
            priority = priority,
            phone = phone,
            limit = limit,
            windowDays = windowDays,
            timezone = timezone,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient dock door policy in status planned. */
fun newLenientDockDoorPolicy(code: String, title: String): LenientDockDoorPolicy {
    return LenientDockDoorPolicy(
        code = code,
        title = title,
        etaMinutes = 0,
        summary = "",
        carrier = "",
        priority = 0,
        phone = "",
        limit = 0,
        windowDays = 0,
        timezone = "",
        threshold = 0.0,
        status = LenientDockDoorPolicyStatus.PLANNED,
    )
}
