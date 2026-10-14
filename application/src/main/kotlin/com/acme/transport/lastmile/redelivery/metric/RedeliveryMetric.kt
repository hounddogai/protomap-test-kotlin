package com.acme.transport.lastmile.redelivery.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery metric records of lastmile's redelivery work, and the rules, stores, and calculations around
// them.

/** Where a redelivery metric is in its life cycle. */
enum class RedeliveryMetricStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A redelivery metric as the redelivery team keeps it. */
data class RedeliveryMetric(
    var code: String,
    var title: String,
    var score: Double,
    var retries: Int,
    var revision: Int,
    var status: RedeliveryMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryMetricStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryMetricStatus): Boolean {
        when (status) {
            RedeliveryMetricStatus.NEW -> {
                when (next) {
                    RedeliveryMetricStatus.FAILED -> return true
                }
                return false
            }
            RedeliveryMetricStatus.QUEUED -> {
                when (next) {
                    RedeliveryMetricStatus.DONE -> return true
                }
                return false
            }
            RedeliveryMetricStatus.RUNNING -> return next == RedeliveryMetricStatus.DONE
            RedeliveryMetricStatus.DONE -> {
                when (next) {
                    RedeliveryMetricStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryMetricStatus) {
        if (!canMoveTo(next)) {
            error("redelivery metric $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun netVolume(): Double = round(revision.toDouble() * score * 100) / 100

    fun snapshot(): RedeliveryMetric {
        return RedeliveryMetric(
            code = code,
            title = title,
            score = score,
            retries = retries,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a redelivery metric in status new. */
fun newRedeliveryMetric(code: String, title: String): RedeliveryMetric {
    return RedeliveryMetric(
        code = code,
        title = title,
        score = 0.0,
        retries = 0,
        revision = 0,
        status = RedeliveryMetricStatus.NEW,
    )
}
