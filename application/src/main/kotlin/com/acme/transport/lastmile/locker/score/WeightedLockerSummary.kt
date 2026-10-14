package com.acme.transport.lastmile.locker.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted locker summary is in its life cycle. */
enum class WeightedLockerSummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted locker summary. Its code is unique within a tenant. */
data class WeightedLockerSummary(
    var code: String,
    var title: String,
    var ratio: Double,
    var ceiling: Double,
    var createdAt: Int,
    var expiresAt: Int,
    var pinned: Boolean,
    var segment: String,
    var checksum: String,
    var status: WeightedLockerSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedLockerSummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedLockerSummaryStatus): Boolean {
        when (status) {
            WeightedLockerSummaryStatus.NEW -> {
                when (next) {
                    WeightedLockerSummaryStatus.RUNNING, WeightedLockerSummaryStatus.FAILED -> return true
                }
                return false
            }
            WeightedLockerSummaryStatus.QUEUED -> {
                when (next) {
                    WeightedLockerSummaryStatus.DONE -> return true
                }
                return false
            }
            WeightedLockerSummaryStatus.RUNNING -> {
                when (next) {
                    WeightedLockerSummaryStatus.DONE -> return true
                }
                return false
            }
            WeightedLockerSummaryStatus.DONE -> return next == WeightedLockerSummaryStatus.FAILED
        }
        return false
    }

    fun moveTo(next: WeightedLockerSummaryStatus) {
        if (!canMoveTo(next)) {
            error("weighted locker summary $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun netLoad(): Double = round(ratio * createdAt.toDouble() * 100) / 100

    fun snapshot(): WeightedLockerSummary {
        return WeightedLockerSummary(
            code = code,
            title = title,
            ratio = ratio,
            ceiling = ceiling,
            createdAt = createdAt,
            expiresAt = expiresAt,
            pinned = pinned,
            segment = segment,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted locker summary in status new. */
fun newWeightedLockerSummary(code: String, title: String): WeightedLockerSummary {
    return WeightedLockerSummary(
        code = code,
        title = title,
        ratio = 0.0,
        ceiling = 0.0,
        createdAt = 0,
        expiresAt = 0,
        pinned = false,
        segment = "",
        checksum = "",
        status = WeightedLockerSummaryStatus.NEW,
    )
}
