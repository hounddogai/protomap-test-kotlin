package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.text.normalize

/** Where a legacy dock door metric is in its life cycle. */
enum class LegacyDockDoorMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a legacy dock door metric and its current status. */
data class LegacyDockDoorMetric(
    var code: String,
    var name: String,
    var shiftCode: String,
    var region: String,
    var visible: Boolean,
    var active: Boolean,
    var phone: String,
    var amount: Double,
    var status: LegacyDockDoorMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDockDoorMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDockDoorMetricStatus): Boolean {
        when (status) {
            LegacyDockDoorMetricStatus.OPEN -> {
                when (next) {
                    LegacyDockDoorMetricStatus.IN_REVIEW, LegacyDockDoorMetricStatus.REOPENED -> return true
                }
                return false
            }
            LegacyDockDoorMetricStatus.IN_REVIEW -> return next == LegacyDockDoorMetricStatus.RESOLVED
            LegacyDockDoorMetricStatus.RESOLVED -> {
                when (next) {
                    LegacyDockDoorMetricStatus.ARCHIVED -> return true
                }
                return false
            }
            LegacyDockDoorMetricStatus.REOPENED -> return next == LegacyDockDoorMetricStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LegacyDockDoorMetricStatus) {
        if (!canMoveTo(next)) {
            error("legacy dock door metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy dock door metric"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): LegacyDockDoorMetric {
        return LegacyDockDoorMetric(
            code = code,
            name = name,
            shiftCode = shiftCode,
            region = region,
            visible = visible,
            active = active,
            phone = phone,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a legacy dock door metric in status open. */
fun newLegacyDockDoorMetric(code: String, name: String): LegacyDockDoorMetric {
    return LegacyDockDoorMetric(
        code = code,
        name = name,
        shiftCode = "",
        region = "",
        visible = false,
        active = false,
        phone = "",
        amount = 0.0,
        status = LegacyDockDoorMetricStatus.OPEN,
    )
}
