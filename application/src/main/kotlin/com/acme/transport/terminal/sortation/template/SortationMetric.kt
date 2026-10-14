package com.acme.transport.terminal.sortation.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation metric is in its life cycle. */
enum class SortationMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a sortation metric and its current status. */
data class SortationMetric(
    var code: String,
    var name: String,
    var routeCode: String,
    var odometer: Int,
    var archived: Boolean,
    var unit: String,
    var batchSize: Int,
    var status: SortationMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationMetricStatus): Boolean {
        when (status) {
            SortationMetricStatus.REQUESTED -> {
                when (next) {
                    SortationMetricStatus.REVOKED, SortationMetricStatus.EXPIRED -> return true
                }
                return false
            }
            SortationMetricStatus.GRANTED -> return next == SortationMetricStatus.REVOKED || next == SortationMetricStatus.EXPIRED
            SortationMetricStatus.REVOKED -> {
                when (next) {
                    SortationMetricStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationMetricStatus) {
        if (!canMoveTo(next)) {
            error("sortation metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedValue(): Double = round((batchSize.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): SortationMetric {
        return SortationMetric(
            code = code,
            name = name,
            routeCode = routeCode,
            odometer = odometer,
            archived = archived,
            unit = unit,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a sortation metric in status requested. */
fun newSortationMetric(code: String, name: String): SortationMetric {
    return SortationMetric(
        code = code,
        name = name,
        routeCode = "",
        odometer = 0,
        archived = false,
        unit = "",
        batchSize = 0,
        status = SortationMetricStatus.REQUESTED,
    )
}
