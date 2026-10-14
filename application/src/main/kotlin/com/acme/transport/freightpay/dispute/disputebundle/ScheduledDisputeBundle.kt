package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled dispute bundle records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a scheduled dispute bundle is in its life cycle. */
enum class ScheduledDisputeBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One scheduled dispute bundle, with what the planners need to act on it. */
data class ScheduledDisputeBundle(
    var code: String,
    var label: String,
    var margin: Double,
    var updatedAt: Int,
    var timezone: String,
    var durationMinutes: Int,
    var visible: Boolean,
    var category: String,
    var status: ScheduledDisputeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDisputeBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDisputeBundleStatus): Boolean {
        when (status) {
            ScheduledDisputeBundleStatus.NEW -> {
                when (next) {
                    ScheduledDisputeBundleStatus.DONE, ScheduledDisputeBundleStatus.FAILED -> return true
                }
                return false
            }
            ScheduledDisputeBundleStatus.QUEUED -> return next == ScheduledDisputeBundleStatus.RUNNING || next == ScheduledDisputeBundleStatus.DONE
            ScheduledDisputeBundleStatus.RUNNING -> {
                when (next) {
                    ScheduledDisputeBundleStatus.DONE, ScheduledDisputeBundleStatus.FAILED -> return true
                }
                return false
            }
            ScheduledDisputeBundleStatus.DONE -> return next == ScheduledDisputeBundleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledDisputeBundleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dispute bundle $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedTotal(): Double = round((updatedAt.toDouble() + margin) * 100) / 100

    fun snapshot(): ScheduledDisputeBundle {
        return ScheduledDisputeBundle(
            code = code,
            label = label,
            margin = margin,
            updatedAt = updatedAt,
            timezone = timezone,
            durationMinutes = durationMinutes,
            visible = visible,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled dispute bundle in status new. */
fun newScheduledDisputeBundle(code: String, label: String): ScheduledDisputeBundle {
    return ScheduledDisputeBundle(
        code = code,
        label = label,
        margin = 0.0,
        updatedAt = 0,
        timezone = "",
        durationMinutes = 0,
        visible = false,
        category = "",
        status = ScheduledDisputeBundleStatus.NEW,
    )
}
