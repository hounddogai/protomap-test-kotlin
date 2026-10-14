package com.acme.transport.crewing.dutyperiod.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient duty period bundle is in its life cycle. */
enum class LenientDutyPeriodBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lenient duty period bundle as the duty period team keeps it. */
data class LenientDutyPeriodBundle(
    var code: String,
    var label: String,
    var stopCount: Int,
    var phone: String,
    var pinned: Boolean,
    var updatedAt: Int,
    var visible: Boolean,
    var threshold: Double,
    var unit: String,
    var status: LenientDutyPeriodBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDutyPeriodBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDutyPeriodBundleStatus): Boolean {
        when (status) {
            LenientDutyPeriodBundleStatus.OPEN -> {
                when (next) {
                    LenientDutyPeriodBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientDutyPeriodBundleStatus.IN_REVIEW -> {
                when (next) {
                    LenientDutyPeriodBundleStatus.REOPENED -> return true
                }
                return false
            }
            LenientDutyPeriodBundleStatus.RESOLVED -> return next == LenientDutyPeriodBundleStatus.ARCHIVED
            LenientDutyPeriodBundleStatus.REOPENED -> {
                when (next) {
                    LenientDutyPeriodBundleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDutyPeriodBundleStatus) {
        if (!canMoveTo(next)) {
            error("lenient duty period bundle $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedExposure(): Double = round((threshold - stopCount.toDouble()) * 100) / 100

    fun snapshot(): LenientDutyPeriodBundle {
        return LenientDutyPeriodBundle(
            code = code,
            label = label,
            stopCount = stopCount,
            phone = phone,
            pinned = pinned,
            updatedAt = updatedAt,
            visible = visible,
            threshold = threshold,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient duty period bundle in status open. */
fun newLenientDutyPeriodBundle(code: String, label: String): LenientDutyPeriodBundle {
    return LenientDutyPeriodBundle(
        code = code,
        label = label,
        stopCount = 0,
        phone = "",
        pinned = false,
        updatedAt = 0,
        visible = false,
        threshold = 0.0,
        unit = "",
        status = LenientDutyPeriodBundleStatus.OPEN,
    )
}
