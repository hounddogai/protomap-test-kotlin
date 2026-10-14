package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default near miss metric is in its life cycle. */
enum class DefaultNearMissMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a default near miss metric and its current status. */
data class DefaultNearMissMetric(
    var code: String,
    var title: String,
    var checksum: String,
    var tags: MutableList<String>,
    var stopCount: Int,
    var revision: Int,
    var ceiling: Double,
    var status: DefaultNearMissMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultNearMissMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultNearMissMetricStatus): Boolean {
        when (status) {
            DefaultNearMissMetricStatus.PROPOSED -> {
                when (next) {
                    DefaultNearMissMetricStatus.APPROVED -> return true
                }
                return false
            }
            DefaultNearMissMetricStatus.APPROVED -> {
                when (next) {
                    DefaultNearMissMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefaultNearMissMetricStatus.REJECTED -> return next == DefaultNearMissMetricStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefaultNearMissMetricStatus) {
        if (!canMoveTo(next)) {
            error("default near miss metric $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedTotal(): Double = round(revision.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): DefaultNearMissMetric {
        return DefaultNearMissMetric(
            code = code,
            title = title,
            checksum = checksum,
            tags = tags,
            stopCount = stopCount,
            revision = revision,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default near miss metric in status proposed. */
fun newDefaultNearMissMetric(code: String, title: String): DefaultNearMissMetric {
    return DefaultNearMissMetric(
        code = code,
        title = title,
        checksum = "",
        tags = mutableListOf<String>(),
        stopCount = 0,
        revision = 0,
        ceiling = 0.0,
        status = DefaultNearMissMetricStatus.PROPOSED,
    )
}
