package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.text.normalize

/** Where a global backhaul metric is in its life cycle. */
enum class GlobalBackhaulMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One global backhaul metric, with what the planners need to act on it. */
data class GlobalBackhaulMetric(
    var code: String,
    var label: String,
    var capacity: Int,
    var name: String,
    var phone: String,
    var locale: String,
    var routeCode: String,
    var status: GlobalBackhaulMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalBackhaulMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalBackhaulMetricStatus): Boolean {
        when (status) {
            GlobalBackhaulMetricStatus.PLANNED -> {
                when (next) {
                    GlobalBackhaulMetricStatus.IN_PROGRESS -> return true
                }
                return false
            }
            GlobalBackhaulMetricStatus.SCHEDULED -> {
                when (next) {
                    GlobalBackhaulMetricStatus.IN_PROGRESS, GlobalBackhaulMetricStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalBackhaulMetricStatus.IN_PROGRESS -> {
                when (next) {
                    GlobalBackhaulMetricStatus.COMPLETED -> return true
                }
                return false
            }
            GlobalBackhaulMetricStatus.COMPLETED -> return next == GlobalBackhaulMetricStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: GlobalBackhaulMetricStatus) {
        if (!canMoveTo(next)) {
            error("global backhaul metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): GlobalBackhaulMetric {
        return GlobalBackhaulMetric(
            code = code,
            label = label,
            capacity = capacity,
            name = name,
            phone = phone,
            locale = locale,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a global backhaul metric in status planned. */
fun newGlobalBackhaulMetric(code: String, label: String): GlobalBackhaulMetric {
    return GlobalBackhaulMetric(
        code = code,
        label = label,
        capacity = 0,
        name = "",
        phone = "",
        locale = "",
        routeCode = "",
        status = GlobalBackhaulMetricStatus.PLANNED,
    )
}
