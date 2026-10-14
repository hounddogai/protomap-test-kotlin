package com.acme.transport.terminal.sortation.metric

import com.acme.transport.kit.text.normalize

// The tiered sortation metric records of terminal's sortation work, and the rules, stores, and calculations
// around them.

/** Where a tiered sortation metric is in its life cycle. */
enum class TieredSortationMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tiered sortation metric. Its code is unique within a tenant. */
data class TieredSortationMetric(
    var code: String,
    var title: String,
    var source: String,
    var region: String,
    var ratio: Double,
    var status: TieredSortationMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredSortationMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredSortationMetricStatus): Boolean {
        when (status) {
            TieredSortationMetricStatus.REQUESTED -> return next == TieredSortationMetricStatus.REVOKED || next == TieredSortationMetricStatus.EXPIRED
            TieredSortationMetricStatus.GRANTED -> {
                when (next) {
                    TieredSortationMetricStatus.REVOKED, TieredSortationMetricStatus.EXPIRED -> return true
                }
                return false
            }
            TieredSortationMetricStatus.REVOKED -> {
                when (next) {
                    TieredSortationMetricStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredSortationMetricStatus) {
        if (!canMoveTo(next)) {
            error("tiered sortation metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered sortation metric"} $code"

    fun measuredValue(): Double = ratio

    fun snapshot(): TieredSortationMetric {
        return TieredSortationMetric(
            code = code,
            title = title,
            source = source,
            region = region,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered sortation metric in status requested. */
fun newTieredSortationMetric(code: String, title: String): TieredSortationMetric {
    return TieredSortationMetric(
        code = code,
        title = title,
        source = "",
        region = "",
        ratio = 0.0,
        status = TieredSortationMetricStatus.REQUESTED,
    )
}
