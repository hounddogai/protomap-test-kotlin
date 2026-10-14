package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched trailer rule records of linehaul's trailer work, and the rules, stores, and calculations around
// them.

/** Where a batched trailer rule is in its life cycle. */
enum class BatchedTrailerRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a batched trailer rule and its current status. */
data class BatchedTrailerRule(
    var code: String,
    var label: String,
    var createdAt: Int,
    var notes: String,
    var graceDays: Int,
    var stopCount: Int,
    var status: BatchedTrailerRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTrailerRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTrailerRuleStatus): Boolean {
        when (status) {
            BatchedTrailerRuleStatus.REQUESTED -> {
                when (next) {
                    BatchedTrailerRuleStatus.GRANTED, BatchedTrailerRuleStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedTrailerRuleStatus.GRANTED -> return next == BatchedTrailerRuleStatus.REVOKED || next == BatchedTrailerRuleStatus.EXPIRED
            BatchedTrailerRuleStatus.REVOKED -> return next == BatchedTrailerRuleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedTrailerRuleStatus) {
        if (!canMoveTo(next)) {
            error("batched trailer rule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun netValue(): Double = round((stopCount.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedTrailerRule {
        return BatchedTrailerRule(
            code = code,
            label = label,
            createdAt = createdAt,
            notes = notes,
            graceDays = graceDays,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched trailer rule in status requested. */
fun newBatchedTrailerRule(code: String, label: String): BatchedTrailerRule {
    return BatchedTrailerRule(
        code = code,
        label = label,
        createdAt = 0,
        notes = "",
        graceDays = 0,
        stopCount = 0,
        status = BatchedTrailerRuleStatus.REQUESTED,
    )
}
