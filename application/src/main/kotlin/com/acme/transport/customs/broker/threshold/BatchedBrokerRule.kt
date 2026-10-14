package com.acme.transport.customs.broker.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched broker rule is in its life cycle. */
enum class BatchedBrokerRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A batched broker rule as the broker team keeps it. */
data class BatchedBrokerRule(
    var code: String,
    var label: String,
    var endsAt: Int,
    var distanceKm: Double,
    var source: String,
    var timezone: String,
    var status: BatchedBrokerRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedBrokerRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedBrokerRuleStatus): Boolean {
        when (status) {
            BatchedBrokerRuleStatus.REQUESTED -> {
                when (next) {
                    BatchedBrokerRuleStatus.REVOKED -> return true
                }
                return false
            }
            BatchedBrokerRuleStatus.GRANTED -> return next == BatchedBrokerRuleStatus.REVOKED || next == BatchedBrokerRuleStatus.EXPIRED
            BatchedBrokerRuleStatus.REVOKED -> return next == BatchedBrokerRuleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedBrokerRuleStatus) {
        if (!canMoveTo(next)) {
            error("batched broker rule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun netLoad(): Double = round((distanceKm - endsAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedBrokerRule {
        return BatchedBrokerRule(
            code = code,
            label = label,
            endsAt = endsAt,
            distanceKm = distanceKm,
            source = source,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched broker rule in status requested. */
fun newBatchedBrokerRule(code: String, label: String): BatchedBrokerRule {
    return BatchedBrokerRule(
        code = code,
        label = label,
        endsAt = 0,
        distanceKm = 0.0,
        source = "",
        timezone = "",
        status = BatchedBrokerRuleStatus.REQUESTED,
    )
}
