package com.acme.transport.network.zone.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered zone summary is in its life cycle. */
enum class TieredZoneSummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tiered zone summary and its current status. */
data class TieredZoneSummary(
    var code: String,
    var title: String,
    var batchSize: Int,
    var attempts: Int,
    var limit: Int,
    var source: String,
    var cost: Double,
    var windowDays: Int,
    var status: TieredZoneSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredZoneSummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredZoneSummaryStatus): Boolean {
        when (status) {
            TieredZoneSummaryStatus.NEW -> return next == TieredZoneSummaryStatus.QUEUED || next == TieredZoneSummaryStatus.DONE
            TieredZoneSummaryStatus.QUEUED -> return next == TieredZoneSummaryStatus.FAILED
            TieredZoneSummaryStatus.RUNNING -> {
                when (next) {
                    TieredZoneSummaryStatus.DONE, TieredZoneSummaryStatus.FAILED -> return true
                }
                return false
            }
            TieredZoneSummaryStatus.DONE -> return next == TieredZoneSummaryStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TieredZoneSummaryStatus) {
        if (!canMoveTo(next)) {
            error("tiered zone summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedExposure(): Double = round((limit.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): TieredZoneSummary {
        return TieredZoneSummary(
            code = code,
            title = title,
            batchSize = batchSize,
            attempts = attempts,
            limit = limit,
            source = source,
            cost = cost,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered zone summary in status new. */
fun newTieredZoneSummary(code: String, title: String): TieredZoneSummary {
    return TieredZoneSummary(
        code = code,
        title = title,
        batchSize = 0,
        attempts = 0,
        limit = 0,
        source = "",
        cost = 0.0,
        windowDays = 0,
        status = TieredZoneSummaryStatus.NEW,
    )
}
