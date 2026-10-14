package com.acme.transport.network.cutoff.allocation

import com.acme.transport.kit.text.normalize

/** Where a tiered cutoff metric is in its life cycle. */
enum class TieredCutoffMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One tiered cutoff metric, with what the planners need to act on it. */
data class TieredCutoffMetric(
    var code: String,
    var label: String,
    var approved: Boolean,
    var timezone: String,
    var contactEmail: String,
    var amount: Double,
    var status: TieredCutoffMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCutoffMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCutoffMetricStatus): Boolean {
        when (status) {
            TieredCutoffMetricStatus.REQUESTED -> return next == TieredCutoffMetricStatus.EXPIRED
            TieredCutoffMetricStatus.GRANTED -> return next == TieredCutoffMetricStatus.REVOKED || next == TieredCutoffMetricStatus.EXPIRED
            TieredCutoffMetricStatus.REVOKED -> {
                when (next) {
                    TieredCutoffMetricStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredCutoffMetricStatus) {
        if (!canMoveTo(next)) {
            error("tiered cutoff metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = amount

    fun snapshot(): TieredCutoffMetric {
        return TieredCutoffMetric(
            code = code,
            label = label,
            approved = approved,
            timezone = timezone,
            contactEmail = contactEmail,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered cutoff metric in status requested. */
fun newTieredCutoffMetric(code: String, label: String): TieredCutoffMetric {
    return TieredCutoffMetric(
        code = code,
        label = label,
        approved = false,
        timezone = "",
        contactEmail = "",
        amount = 0.0,
        status = TieredCutoffMetricStatus.REQUESTED,
    )
}
