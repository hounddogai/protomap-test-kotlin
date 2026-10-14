package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive dispute rollup is in its life cycle. */
enum class AdaptiveDisputeRollupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive dispute rollup as the dispute team keeps it. */
data class AdaptiveDisputeRollup(
    var code: String,
    var title: String,
    var threshold: Double,
    var carrier: String,
    var region: String,
    var locale: String,
    var depot: String,
    var endsAt: Int,
    var status: AdaptiveDisputeRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDisputeRollupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDisputeRollupStatus): Boolean {
        when (status) {
            AdaptiveDisputeRollupStatus.DRAFT -> {
                when (next) {
                    AdaptiveDisputeRollupStatus.PENDING -> return true
                }
                return false
            }
            AdaptiveDisputeRollupStatus.PENDING -> return next == AdaptiveDisputeRollupStatus.ACTIVE
            AdaptiveDisputeRollupStatus.ACTIVE -> return next == AdaptiveDisputeRollupStatus.SUSPENDED || next == AdaptiveDisputeRollupStatus.CLOSED
            AdaptiveDisputeRollupStatus.SUSPENDED -> return next == AdaptiveDisputeRollupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveDisputeRollupStatus) {
        if (!canMoveTo(next)) {
            error("adaptive dispute rollup $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveVolume(): Double = round(threshold * endsAt.toDouble() * 100) / 100

    fun snapshot(): AdaptiveDisputeRollup {
        return AdaptiveDisputeRollup(
            code = code,
            title = title,
            threshold = threshold,
            carrier = carrier,
            region = region,
            locale = locale,
            depot = depot,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a adaptive dispute rollup in status draft. */
fun newAdaptiveDisputeRollup(code: String, title: String): AdaptiveDisputeRollup {
    return AdaptiveDisputeRollup(
        code = code,
        title = title,
        threshold = 0.0,
        carrier = "",
        region = "",
        locale = "",
        depot = "",
        endsAt = 0,
        status = AdaptiveDisputeRollupStatus.DRAFT,
    )
}
