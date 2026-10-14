package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered trailer yard metric is in its life cycle. */
enum class TieredTrailerYardMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tiered trailer yard metric as the trailer yard team keeps it. */
data class TieredTrailerYardMetric(
    var code: String,
    var label: String,
    var carrier: String,
    var retries: Int,
    var routeCode: String,
    var capacity: Int,
    var archived: Boolean,
    var reviewer: String,
    var status: TieredTrailerYardMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTrailerYardMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTrailerYardMetricStatus): Boolean {
        when (status) {
            TieredTrailerYardMetricStatus.REQUESTED -> return next == TieredTrailerYardMetricStatus.REVOKED || next == TieredTrailerYardMetricStatus.EXPIRED
            TieredTrailerYardMetricStatus.GRANTED -> return next == TieredTrailerYardMetricStatus.EXPIRED
            TieredTrailerYardMetricStatus.REVOKED -> return next == TieredTrailerYardMetricStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TieredTrailerYardMetricStatus) {
        if (!canMoveTo(next)) {
            error("tiered trailer yard metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tiered trailer yard metric"} $code"

    fun adjustedVolume(): Double = round(capacity.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): TieredTrailerYardMetric {
        return TieredTrailerYardMetric(
            code = code,
            label = label,
            carrier = carrier,
            retries = retries,
            routeCode = routeCode,
            capacity = capacity,
            archived = archived,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered trailer yard metric in status requested. */
fun newTieredTrailerYardMetric(code: String, label: String): TieredTrailerYardMetric {
    return TieredTrailerYardMetric(
        code = code,
        label = label,
        carrier = "",
        retries = 0,
        routeCode = "",
        capacity = 0,
        archived = false,
        reviewer = "",
        status = TieredTrailerYardMetricStatus.REQUESTED,
    )
}
