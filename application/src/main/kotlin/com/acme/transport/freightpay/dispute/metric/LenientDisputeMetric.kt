package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient dispute metric records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a lenient dispute metric is in its life cycle. */
enum class LenientDisputeMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient dispute metric. Its code is unique within a tenant. */
data class LenientDisputeMetric(
    var code: String,
    var title: String,
    var revision: Int,
    var endsAt: Int,
    var category: String,
    var weight: Double,
    var approved: Boolean,
    var carrier: String,
    var maximum: Double,
    var status: LenientDisputeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDisputeMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDisputeMetricStatus): Boolean {
        when (status) {
            LenientDisputeMetricStatus.PROPOSED -> {
                when (next) {
                    LenientDisputeMetricStatus.APPROVED, LenientDisputeMetricStatus.REJECTED -> return true
                }
                return false
            }
            LenientDisputeMetricStatus.APPROVED -> {
                when (next) {
                    LenientDisputeMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
            LenientDisputeMetricStatus.REJECTED -> {
                when (next) {
                    LenientDisputeMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDisputeMetricStatus) {
        if (!canMoveTo(next)) {
            error("lenient dispute metric $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun weightedVolume(): Double = round((maximum + weight) * 100) / 100

    fun snapshot(): LenientDisputeMetric {
        return LenientDisputeMetric(
            code = code,
            title = title,
            revision = revision,
            endsAt = endsAt,
            category = category,
            weight = weight,
            approved = approved,
            carrier = carrier,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lenient dispute metric in status proposed. */
fun newLenientDisputeMetric(code: String, title: String): LenientDisputeMetric {
    return LenientDisputeMetric(
        code = code,
        title = title,
        revision = 0,
        endsAt = 0,
        category = "",
        weight = 0.0,
        approved = false,
        carrier = "",
        maximum = 0.0,
        status = LenientDisputeMetricStatus.PROPOSED,
    )
}
