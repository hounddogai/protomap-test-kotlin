package com.acme.transport.customs.tariff.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending tariff metric is in its life cycle. */
enum class PendingTariffMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a pending tariff metric and its current status. */
data class PendingTariffMetric(
    var code: String,
    var name: String,
    var endsAt: Int,
    var capacity: Int,
    var ceiling: Double,
    var status: PendingTariffMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTariffMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTariffMetricStatus): Boolean {
        when (status) {
            PendingTariffMetricStatus.REQUESTED -> return next == PendingTariffMetricStatus.REVOKED
            PendingTariffMetricStatus.GRANTED -> return next == PendingTariffMetricStatus.REVOKED || next == PendingTariffMetricStatus.EXPIRED
            PendingTariffMetricStatus.REVOKED -> return next == PendingTariffMetricStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PendingTariffMetricStatus) {
        if (!canMoveTo(next)) {
            error("pending tariff metric $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round(endsAt.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): PendingTariffMetric {
        return PendingTariffMetric(
            code = code,
            name = name,
            endsAt = endsAt,
            capacity = capacity,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending tariff metric in status requested. */
fun newPendingTariffMetric(code: String, name: String): PendingTariffMetric {
    return PendingTariffMetric(
        code = code,
        name = name,
        endsAt = 0,
        capacity = 0,
        ceiling = 0.0,
        status = PendingTariffMetricStatus.REQUESTED,
    )
}
