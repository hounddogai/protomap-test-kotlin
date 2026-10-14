package com.acme.transport.terminal.sortation.sortationvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal sortation variance records of terminal's sortation work, and the rules, stores, and
// calculations around them.

/** Where a seasonal sortation variance is in its life cycle. */
enum class SeasonalSortationVarianceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A seasonal sortation variance. Its code is unique within a tenant. */
data class SeasonalSortationVariance(
    var code: String,
    var label: String,
    var ratio: Double,
    var visible: Boolean,
    var retries: Int,
    var windowDays: Int,
    var ceiling: Double,
    var quantity: Int,
    var status: SeasonalSortationVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalSortationVarianceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalSortationVarianceStatus): Boolean {
        when (status) {
            SeasonalSortationVarianceStatus.REQUESTED -> {
                when (next) {
                    SeasonalSortationVarianceStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalSortationVarianceStatus.GRANTED -> return next == SeasonalSortationVarianceStatus.REVOKED || next == SeasonalSortationVarianceStatus.EXPIRED
            SeasonalSortationVarianceStatus.REVOKED -> {
                when (next) {
                    SeasonalSortationVarianceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalSortationVarianceStatus) {
        if (!canMoveTo(next)) {
            error("seasonal sortation variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netLoad(): Double = round((retries.toDouble() - ceiling) * 100) / 100

    fun snapshot(): SeasonalSortationVariance {
        return SeasonalSortationVariance(
            code = code,
            label = label,
            ratio = ratio,
            visible = visible,
            retries = retries,
            windowDays = windowDays,
            ceiling = ceiling,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal sortation variance in status requested. */
fun newSeasonalSortationVariance(code: String, label: String): SeasonalSortationVariance {
    return SeasonalSortationVariance(
        code = code,
        label = label,
        ratio = 0.0,
        visible = false,
        retries = 0,
        windowDays = 0,
        ceiling = 0.0,
        quantity = 0,
        status = SeasonalSortationVarianceStatus.REQUESTED,
    )
}
