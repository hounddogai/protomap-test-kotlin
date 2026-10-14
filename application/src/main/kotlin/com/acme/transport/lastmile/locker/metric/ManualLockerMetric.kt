package com.acme.transport.lastmile.locker.metric

import com.acme.transport.kit.text.normalize

// The manual locker metric records of lastmile's locker work, and the rules, stores, and calculations around
// them.

/** Where a manual locker metric is in its life cycle. */
enum class ManualLockerMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual locker metric as the locker team keeps it. */
data class ManualLockerMetric(
    var code: String,
    var title: String,
    var visible: Boolean,
    var etaMinutes: Int,
    var shiftCode: String,
    var source: String,
    var unit: String,
    var dock: String,
    var status: ManualLockerMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualLockerMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualLockerMetricStatus): Boolean {
        when (status) {
            ManualLockerMetricStatus.IDLE -> return next == ManualLockerMetricStatus.DRAINING
            ManualLockerMetricStatus.WARMING -> {
                when (next) {
                    ManualLockerMetricStatus.READY, ManualLockerMetricStatus.DRAINING -> return true
                }
                return false
            }
            ManualLockerMetricStatus.READY -> return next == ManualLockerMetricStatus.DRAINING
            ManualLockerMetricStatus.DRAINING -> {
                when (next) {
                    ManualLockerMetricStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualLockerMetricStatus) {
        if (!canMoveTo(next)) {
            error("manual locker metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): ManualLockerMetric {
        return ManualLockerMetric(
            code = code,
            title = title,
            visible = visible,
            etaMinutes = etaMinutes,
            shiftCode = shiftCode,
            source = source,
            unit = unit,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a manual locker metric in status idle. */
fun newManualLockerMetric(code: String, title: String): ManualLockerMetric {
    return ManualLockerMetric(
        code = code,
        title = title,
        visible = false,
        etaMinutes = 0,
        shiftCode = "",
        source = "",
        unit = "",
        dock = "",
        status = ManualLockerMetricStatus.IDLE,
    )
}
