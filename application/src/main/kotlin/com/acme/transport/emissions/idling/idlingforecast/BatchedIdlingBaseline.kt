package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched idling baseline is in its life cycle. */
enum class BatchedIdlingBaselineStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a batched idling baseline and its current status. */
data class BatchedIdlingBaseline(
    var code: String,
    var title: String,
    var batchSize: Int,
    var windowDays: Int,
    var version: Int,
    var timezone: String,
    var lane: String,
    var maximum: Double,
    var rate: Double,
    var source: String,
    var attempts: Int,
    var status: BatchedIdlingBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedIdlingBaselineStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedIdlingBaselineStatus): Boolean {
        when (status) {
            BatchedIdlingBaselineStatus.NEW -> {
                when (next) {
                    BatchedIdlingBaselineStatus.QUEUED, BatchedIdlingBaselineStatus.RUNNING -> return true
                }
                return false
            }
            BatchedIdlingBaselineStatus.QUEUED -> {
                when (next) {
                    BatchedIdlingBaselineStatus.RUNNING, BatchedIdlingBaselineStatus.FAILED -> return true
                }
                return false
            }
            BatchedIdlingBaselineStatus.RUNNING -> {
                when (next) {
                    BatchedIdlingBaselineStatus.DONE, BatchedIdlingBaselineStatus.FAILED -> return true
                }
                return false
            }
            BatchedIdlingBaselineStatus.DONE -> {
                when (next) {
                    BatchedIdlingBaselineStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedIdlingBaselineStatus) {
        if (!canMoveTo(next)) {
            error("batched idling baseline $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun weightedExposure(): Double = round((windowDays.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): BatchedIdlingBaseline {
        return BatchedIdlingBaseline(
            code = code,
            title = title,
            batchSize = batchSize,
            windowDays = windowDays,
            version = version,
            timezone = timezone,
            lane = lane,
            maximum = maximum,
            rate = rate,
            source = source,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched idling baseline in status new. */
fun newBatchedIdlingBaseline(code: String, title: String): BatchedIdlingBaseline {
    return BatchedIdlingBaseline(
        code = code,
        title = title,
        batchSize = 0,
        windowDays = 0,
        version = 0,
        timezone = "",
        lane = "",
        maximum = 0.0,
        rate = 0.0,
        source = "",
        attempts = 0,
        status = BatchedIdlingBaselineStatus.NEW,
    )
}
