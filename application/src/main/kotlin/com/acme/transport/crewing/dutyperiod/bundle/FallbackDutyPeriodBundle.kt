package com.acme.transport.crewing.dutyperiod.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback duty period bundle records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a fallback duty period bundle is in its life cycle. */
enum class FallbackDutyPeriodBundleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One fallback duty period bundle, with what the planners need to act on it. */
data class FallbackDutyPeriodBundle(
    var code: String,
    var label: String,
    var floorValue: Double,
    var carrier: String,
    var phone: String,
    var shiftCode: String,
    var windowDays: Int,
    var status: FallbackDutyPeriodBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDutyPeriodBundleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDutyPeriodBundleStatus): Boolean {
        when (status) {
            FallbackDutyPeriodBundleStatus.PLANNED -> return next == FallbackDutyPeriodBundleStatus.COMPLETED
            FallbackDutyPeriodBundleStatus.SCHEDULED -> {
                when (next) {
                    FallbackDutyPeriodBundleStatus.COMPLETED, FallbackDutyPeriodBundleStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackDutyPeriodBundleStatus.IN_PROGRESS -> {
                when (next) {
                    FallbackDutyPeriodBundleStatus.COMPLETED, FallbackDutyPeriodBundleStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackDutyPeriodBundleStatus.COMPLETED -> return next == FallbackDutyPeriodBundleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FallbackDutyPeriodBundleStatus) {
        if (!canMoveTo(next)) {
            error("fallback duty period bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedVolume(): Double = round((floorValue + windowDays.toDouble()) * 100) / 100

    fun snapshot(): FallbackDutyPeriodBundle {
        return FallbackDutyPeriodBundle(
            code = code,
            label = label,
            floorValue = floorValue,
            carrier = carrier,
            phone = phone,
            shiftCode = shiftCode,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback duty period bundle in status planned. */
fun newFallbackDutyPeriodBundle(code: String, label: String): FallbackDutyPeriodBundle {
    return FallbackDutyPeriodBundle(
        code = code,
        label = label,
        floorValue = 0.0,
        carrier = "",
        phone = "",
        shiftCode = "",
        windowDays = 0,
        status = FallbackDutyPeriodBundleStatus.PLANNED,
    )
}
