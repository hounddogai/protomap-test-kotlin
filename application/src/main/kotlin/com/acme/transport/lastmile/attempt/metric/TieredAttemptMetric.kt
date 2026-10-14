package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered attempt metric records of lastmile's attempt work, and the rules, stores, and calculations
// around them.

/** Where a tiered attempt metric is in its life cycle. */
enum class TieredAttemptMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tiered attempt metric and its current status. */
data class TieredAttemptMetric(
    var code: String,
    var label: String,
    var startsAt: Int,
    var limit: Int,
    var amount: Double,
    var country: String,
    var weight: Double,
    var dock: String,
    var retries: Int,
    var status: TieredAttemptMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAttemptMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAttemptMetricStatus): Boolean {
        when (status) {
            TieredAttemptMetricStatus.NEW -> return next == TieredAttemptMetricStatus.QUEUED
            TieredAttemptMetricStatus.QUEUED -> {
                when (next) {
                    TieredAttemptMetricStatus.RUNNING, TieredAttemptMetricStatus.FAILED -> return true
                }
                return false
            }
            TieredAttemptMetricStatus.RUNNING -> return next == TieredAttemptMetricStatus.DONE || next == TieredAttemptMetricStatus.FAILED
            TieredAttemptMetricStatus.DONE -> return next == TieredAttemptMetricStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TieredAttemptMetricStatus) {
        if (!canMoveTo(next)) {
            error("tiered attempt metric $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedExposure(): Double = round(weight * amount * 100) / 100

    fun snapshot(): TieredAttemptMetric {
        return TieredAttemptMetric(
            code = code,
            label = label,
            startsAt = startsAt,
            limit = limit,
            amount = amount,
            country = country,
            weight = weight,
            dock = dock,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered attempt metric in status new. */
fun newTieredAttemptMetric(code: String, label: String): TieredAttemptMetric {
    return TieredAttemptMetric(
        code = code,
        label = label,
        startsAt = 0,
        limit = 0,
        amount = 0.0,
        country = "",
        weight = 0.0,
        dock = "",
        retries = 0,
        status = TieredAttemptMetricStatus.NEW,
    )
}
