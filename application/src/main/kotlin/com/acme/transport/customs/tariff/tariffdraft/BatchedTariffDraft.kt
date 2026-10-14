package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched tariff draft records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a batched tariff draft is in its life cycle. */
enum class BatchedTariffDraftStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One batched tariff draft, with what the planners need to act on it. */
data class BatchedTariffDraft(
    var code: String,
    var label: String,
    var region: String,
    var sequence: Int,
    var updatedAt: Int,
    var budget: Double,
    var source: String,
    var createdAt: Int,
    var driverId: String,
    var score: Double,
    var status: BatchedTariffDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTariffDraftStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTariffDraftStatus): Boolean {
        when (status) {
            BatchedTariffDraftStatus.OPEN -> {
                when (next) {
                    BatchedTariffDraftStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedTariffDraftStatus.IN_REVIEW -> {
                when (next) {
                    BatchedTariffDraftStatus.RESOLVED -> return true
                }
                return false
            }
            BatchedTariffDraftStatus.RESOLVED -> return next == BatchedTariffDraftStatus.REOPENED || next == BatchedTariffDraftStatus.ARCHIVED
            BatchedTariffDraftStatus.REOPENED -> {
                when (next) {
                    BatchedTariffDraftStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTariffDraftStatus) {
        if (!canMoveTo(next)) {
            error("batched tariff draft $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedExposure(): Double = round((budget - score) * 100) / 100

    fun snapshot(): BatchedTariffDraft {
        return BatchedTariffDraft(
            code = code,
            label = label,
            region = region,
            sequence = sequence,
            updatedAt = updatedAt,
            budget = budget,
            source = source,
            createdAt = createdAt,
            driverId = driverId,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a batched tariff draft in status open. */
fun newBatchedTariffDraft(code: String, label: String): BatchedTariffDraft {
    return BatchedTariffDraft(
        code = code,
        label = label,
        region = "",
        sequence = 0,
        updatedAt = 0,
        budget = 0.0,
        source = "",
        createdAt = 0,
        driverId = "",
        score = 0.0,
        status = BatchedTariffDraftStatus.OPEN,
    )
}
