package com.acme.transport.network.transittime.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched transit time rule is in its life cycle. */
enum class BatchedTransitTimeRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A batched transit time rule as the transit time team keeps it. */
data class BatchedTransitTimeRule(
    var code: String,
    var label: String,
    var ceiling: Double,
    var externalRef: String,
    var startsAt: Int,
    var graceDays: Int,
    var tags: MutableList<String>,
    var updatedAt: Int,
    var source: String,
    var loadKg: Double,
    var currency: String,
    var status: BatchedTransitTimeRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTransitTimeRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTransitTimeRuleStatus): Boolean {
        when (status) {
            BatchedTransitTimeRuleStatus.DRAFT -> return next == BatchedTransitTimeRuleStatus.PENDING
            BatchedTransitTimeRuleStatus.PENDING -> return next == BatchedTransitTimeRuleStatus.ACTIVE || next == BatchedTransitTimeRuleStatus.CLOSED
            BatchedTransitTimeRuleStatus.ACTIVE -> return next == BatchedTransitTimeRuleStatus.SUSPENDED || next == BatchedTransitTimeRuleStatus.CLOSED
            BatchedTransitTimeRuleStatus.SUSPENDED -> return next == BatchedTransitTimeRuleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BatchedTransitTimeRuleStatus) {
        if (!canMoveTo(next)) {
            error("batched transit time rule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round(loadKg * ceiling * 100) / 100

    fun snapshot(): BatchedTransitTimeRule {
        return BatchedTransitTimeRule(
            code = code,
            label = label,
            ceiling = ceiling,
            externalRef = externalRef,
            startsAt = startsAt,
            graceDays = graceDays,
            tags = tags,
            updatedAt = updatedAt,
            source = source,
            loadKg = loadKg,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched transit time rule in status draft. */
fun newBatchedTransitTimeRule(code: String, label: String): BatchedTransitTimeRule {
    return BatchedTransitTimeRule(
        code = code,
        label = label,
        ceiling = 0.0,
        externalRef = "",
        startsAt = 0,
        graceDays = 0,
        tags = mutableListOf<String>(),
        updatedAt = 0,
        source = "",
        loadKg = 0.0,
        currency = "",
        status = BatchedTransitTimeRuleStatus.DRAFT,
    )
}
