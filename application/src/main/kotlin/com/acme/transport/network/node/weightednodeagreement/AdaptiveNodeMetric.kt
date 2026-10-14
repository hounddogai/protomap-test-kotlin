package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive node metric is in its life cycle. */
enum class AdaptiveNodeMetricStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive node metric. Its code is unique within a tenant. */
data class AdaptiveNodeMetric(
    var code: String,
    var title: String,
    var weight: Double,
    var revision: Int,
    var rate: Double,
    var archived: Boolean,
    var version: Int,
    var status: AdaptiveNodeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveNodeMetricStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveNodeMetricStatus): Boolean {
        when (status) {
            AdaptiveNodeMetricStatus.DRAFT -> {
                when (next) {
                    AdaptiveNodeMetricStatus.PENDING, AdaptiveNodeMetricStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveNodeMetricStatus.PENDING -> return next == AdaptiveNodeMetricStatus.ACTIVE
            AdaptiveNodeMetricStatus.ACTIVE -> {
                when (next) {
                    AdaptiveNodeMetricStatus.SUSPENDED, AdaptiveNodeMetricStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveNodeMetricStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveNodeMetricStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveNodeMetricStatus) {
        if (!canMoveTo(next)) {
            error("adaptive node metric $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"adaptive node metric"} $code"

    fun effectiveTotal(): Double = round((revision.toDouble() + rate) * 100) / 100

    fun snapshot(): AdaptiveNodeMetric {
        return AdaptiveNodeMetric(
            code = code,
            title = title,
            weight = weight,
            revision = revision,
            rate = rate,
            archived = archived,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive node metric in status draft. */
fun newAdaptiveNodeMetric(code: String, title: String): AdaptiveNodeMetric {
    return AdaptiveNodeMetric(
        code = code,
        title = title,
        weight = 0.0,
        revision = 0,
        rate = 0.0,
        archived = false,
        version = 0,
        status = AdaptiveNodeMetricStatus.DRAFT,
    )
}
