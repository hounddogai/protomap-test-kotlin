package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped route offset metric records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a capped route offset metric is in its life cycle. */
enum class CappedRouteOffsetMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped route offset metric as the route offset team keeps it. */
data class CappedRouteOffsetMetric(
    var code: String,
    var name: String,
    var revision: Int,
    var title: String,
    var loadKg: Double,
    var unit: String,
    var approved: Boolean,
    var batchSize: Int,
    var segment: String,
    var durationMinutes: Int,
    var amount: Double,
    var status: CappedRouteOffsetMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRouteOffsetMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRouteOffsetMetricStatus): Boolean {
        when (status) {
            CappedRouteOffsetMetricStatus.NEW -> {
                when (next) {
                    CappedRouteOffsetMetricStatus.RUNNING, CappedRouteOffsetMetricStatus.DONE -> return true
                }
                return false
            }
            CappedRouteOffsetMetricStatus.QUEUED -> {
                when (next) {
                    CappedRouteOffsetMetricStatus.FAILED -> return true
                }
                return false
            }
            CappedRouteOffsetMetricStatus.RUNNING -> {
                when (next) {
                    CappedRouteOffsetMetricStatus.DONE, CappedRouteOffsetMetricStatus.FAILED -> return true
                }
                return false
            }
            CappedRouteOffsetMetricStatus.DONE -> return next == CappedRouteOffsetMetricStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedRouteOffsetMetricStatus) {
        if (!canMoveTo(next)) {
            error("capped route offset metric $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun netVolume(): Double = round(revision.toDouble() * amount * 100) / 100

    fun snapshot(): CappedRouteOffsetMetric {
        return CappedRouteOffsetMetric(
            code = code,
            name = name,
            revision = revision,
            title = title,
            loadKg = loadKg,
            unit = unit,
            approved = approved,
            batchSize = batchSize,
            segment = segment,
            durationMinutes = durationMinutes,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a capped route offset metric in status new. */
fun newCappedRouteOffsetMetric(code: String, name: String): CappedRouteOffsetMetric {
    return CappedRouteOffsetMetric(
        code = code,
        name = name,
        revision = 0,
        title = "",
        loadKg = 0.0,
        unit = "",
        approved = false,
        batchSize = 0,
        segment = "",
        durationMinutes = 0,
        amount = 0.0,
        status = CappedRouteOffsetMetricStatus.NEW,
    )
}
