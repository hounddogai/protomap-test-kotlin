package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default parcel metric is in its life cycle. */
enum class DefaultParcelMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A default parcel metric as the parcel team keeps it. */
data class DefaultParcelMetric(
    var code: String,
    var title: String,
    var ratio: Double,
    var segment: String,
    var quantity: Int,
    var approved: Boolean,
    var status: DefaultParcelMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultParcelMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultParcelMetricStatus): Boolean {
        when (status) {
            DefaultParcelMetricStatus.OPEN -> return next == DefaultParcelMetricStatus.REOPENED || next == DefaultParcelMetricStatus.ARCHIVED
            DefaultParcelMetricStatus.IN_REVIEW -> {
                when (next) {
                    DefaultParcelMetricStatus.RESOLVED, DefaultParcelMetricStatus.REOPENED -> return true
                }
                return false
            }
            DefaultParcelMetricStatus.RESOLVED -> return next == DefaultParcelMetricStatus.ARCHIVED
            DefaultParcelMetricStatus.REOPENED -> return next == DefaultParcelMetricStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DefaultParcelMetricStatus) {
        if (!canMoveTo(next)) {
            error("default parcel metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedValue(): Double = round((quantity.toDouble() + ratio) * 100) / 100

    fun snapshot(): DefaultParcelMetric {
        return DefaultParcelMetric(
            code = code,
            title = title,
            ratio = ratio,
            segment = segment,
            quantity = quantity,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default parcel metric in status open. */
fun newDefaultParcelMetric(code: String, title: String): DefaultParcelMetric {
    return DefaultParcelMetric(
        code = code,
        title = title,
        ratio = 0.0,
        segment = "",
        quantity = 0,
        approved = false,
        status = DefaultParcelMetricStatus.OPEN,
    )
}
