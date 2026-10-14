package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop metric is in its life cycle. */
enum class DropMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a drop metric and its current status. */
data class DropMetric(
    var code: String,
    var title: String,
    var country: String,
    var notes: String,
    var amount: Double,
    var trailerId: String,
    var routeCode: String,
    var vehicleId: String,
    var maximum: Double,
    var lane: String,
    var status: DropMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropMetricStatus): Boolean {
        when (status) {
            DropMetricStatus.DRAFT -> return next == DropMetricStatus.CLOSED
            DropMetricStatus.PENDING -> return next == DropMetricStatus.SUSPENDED || next == DropMetricStatus.CLOSED
            DropMetricStatus.ACTIVE -> return next == DropMetricStatus.SUSPENDED || next == DropMetricStatus.CLOSED
            DropMetricStatus.SUSPENDED -> return next == DropMetricStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DropMetricStatus) {
        if (!canMoveTo(next)) {
            error("drop metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"drop metric"} $code"

    fun projectedVolume(): Double = round((amount + maximum) * 100) / 100

    fun snapshot(): DropMetric {
        return DropMetric(
            code = code,
            title = title,
            country = country,
            notes = notes,
            amount = amount,
            trailerId = trailerId,
            routeCode = routeCode,
            vehicleId = vehicleId,
            maximum = maximum,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a drop metric in status draft. */
fun newDropMetric(code: String, title: String): DropMetric {
    return DropMetric(
        code = code,
        title = title,
        country = "",
        notes = "",
        amount = 0.0,
        trailerId = "",
        routeCode = "",
        vehicleId = "",
        maximum = 0.0,
        lane = "",
        status = DropMetricStatus.DRAFT,
    )
}
