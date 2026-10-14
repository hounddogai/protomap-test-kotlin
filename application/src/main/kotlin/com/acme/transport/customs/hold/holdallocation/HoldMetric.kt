package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold metric is in its life cycle. */
enum class HoldMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A hold metric. Its code is unique within a tenant. */
data class HoldMetric(
    var code: String,
    var name: String,
    var cost: Double,
    var notes: String,
    var visible: Boolean,
    var category: String,
    var retries: Int,
    var label: String,
    var archived: Boolean,
    var status: HoldMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldMetricStatus): Boolean {
        when (status) {
            HoldMetricStatus.IDLE -> return next == HoldMetricStatus.DRAINING || next == HoldMetricStatus.STOPPED
            HoldMetricStatus.WARMING -> return next == HoldMetricStatus.DRAINING || next == HoldMetricStatus.STOPPED
            HoldMetricStatus.READY -> {
                when (next) {
                    HoldMetricStatus.STOPPED -> return true
                }
                return false
            }
            HoldMetricStatus.DRAINING -> return next == HoldMetricStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: HoldMetricStatus) {
        if (!canMoveTo(next)) {
            error("hold metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun effectiveValue(): Double = round(retries.toDouble() * cost * 100) / 100

    fun snapshot(): HoldMetric {
        return HoldMetric(
            code = code,
            name = name,
            cost = cost,
            notes = notes,
            visible = visible,
            category = category,
            retries = retries,
            label = label,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a hold metric in status idle. */
fun newHoldMetric(code: String, name: String): HoldMetric {
    return HoldMetric(
        code = code,
        name = name,
        cost = 0.0,
        notes = "",
        visible = false,
        category = "",
        retries = 0,
        label = "",
        archived = false,
        status = HoldMetricStatus.IDLE,
    )
}
