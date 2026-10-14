package com.acme.transport.network.zone.plan

import com.acme.transport.kit.text.normalize

/** Where a zone metric is in its life cycle. */
enum class ZoneMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A zone metric as the zone team keeps it. */
data class ZoneMetric(
    var code: String,
    var name: String,
    var trailerId: String,
    var label: String,
    var shiftCode: String,
    var notes: String,
    var vehicleId: String,
    var segment: String,
    var amount: Double,
    var status: ZoneMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneMetricStatus): Boolean {
        when (status) {
            ZoneMetricStatus.DRAFT -> {
                when (next) {
                    ZoneMetricStatus.PENDING, ZoneMetricStatus.SUSPENDED -> return true
                }
                return false
            }
            ZoneMetricStatus.PENDING -> {
                when (next) {
                    ZoneMetricStatus.SUSPENDED, ZoneMetricStatus.CLOSED -> return true
                }
                return false
            }
            ZoneMetricStatus.ACTIVE -> {
                when (next) {
                    ZoneMetricStatus.SUSPENDED -> return true
                }
                return false
            }
            ZoneMetricStatus.SUSPENDED -> {
                when (next) {
                    ZoneMetricStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneMetricStatus) {
        if (!canMoveTo(next)) {
            error("zone metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun measuredValue(): Double = amount

    fun snapshot(): ZoneMetric {
        return ZoneMetric(
            code = code,
            name = name,
            trailerId = trailerId,
            label = label,
            shiftCode = shiftCode,
            notes = notes,
            vehicleId = vehicleId,
            segment = segment,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a zone metric in status draft. */
fun newZoneMetric(code: String, name: String): ZoneMetric {
    return ZoneMetric(
        code = code,
        name = name,
        trailerId = "",
        label = "",
        shiftCode = "",
        notes = "",
        vehicleId = "",
        segment = "",
        amount = 0.0,
        status = ZoneMetricStatus.DRAFT,
    )
}
