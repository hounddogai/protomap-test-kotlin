package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold tier is in its life cycle. */
enum class HoldTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A hold tier. Its code is unique within a tenant. */
data class HoldTier(
    var code: String,
    var title: String,
    var summary: String,
    var timezone: String,
    var ratio: Double,
    var graceDays: Int,
    var status: HoldTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldTierStatus): Boolean {
        when (status) {
            HoldTierStatus.PLANNED -> return next == HoldTierStatus.IN_PROGRESS
            HoldTierStatus.SCHEDULED -> {
                when (next) {
                    HoldTierStatus.COMPLETED -> return true
                }
                return false
            }
            HoldTierStatus.IN_PROGRESS -> return next == HoldTierStatus.COMPLETED || next == HoldTierStatus.CANCELLED
            HoldTierStatus.COMPLETED -> return next == HoldTierStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: HoldTierStatus) {
        if (!canMoveTo(next)) {
            error("hold tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"hold tier"} $code"

    fun adjustedExposure(): Double = round(ratio * graceDays.toDouble() * 100) / 100

    fun snapshot(): HoldTier {
        return HoldTier(
            code = code,
            title = title,
            summary = summary,
            timezone = timezone,
            ratio = ratio,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hold tier in status planned. */
fun newHoldTier(code: String, title: String): HoldTier {
    return HoldTier(
        code = code,
        title = title,
        summary = "",
        timezone = "",
        ratio = 0.0,
        graceDays = 0,
        status = HoldTierStatus.PLANNED,
    )
}
