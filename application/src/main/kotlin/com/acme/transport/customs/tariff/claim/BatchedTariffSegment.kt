package com.acme.transport.customs.tariff.claim

import com.acme.transport.kit.text.normalize

/** Where a batched tariff segment is in its life cycle. */
enum class BatchedTariffSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched tariff segment as the tariff team keeps it. */
data class BatchedTariffSegment(
    var code: String,
    var name: String,
    var country: String,
    var channel: String,
    var archived: Boolean,
    var amount: Double,
    var status: BatchedTariffSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTariffSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTariffSegmentStatus): Boolean {
        when (status) {
            BatchedTariffSegmentStatus.OPEN -> {
                when (next) {
                    BatchedTariffSegmentStatus.IN_REVIEW, BatchedTariffSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedTariffSegmentStatus.IN_REVIEW -> return next == BatchedTariffSegmentStatus.RESOLVED || next == BatchedTariffSegmentStatus.REOPENED
            BatchedTariffSegmentStatus.RESOLVED -> {
                when (next) {
                    BatchedTariffSegmentStatus.REOPENED -> return true
                }
                return false
            }
            BatchedTariffSegmentStatus.REOPENED -> {
                when (next) {
                    BatchedTariffSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTariffSegmentStatus) {
        if (!canMoveTo(next)) {
            error("batched tariff segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched tariff segment"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): BatchedTariffSegment {
        return BatchedTariffSegment(
            code = code,
            name = name,
            country = country,
            channel = channel,
            archived = archived,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched tariff segment in status open. */
fun newBatchedTariffSegment(code: String, name: String): BatchedTariffSegment {
    return BatchedTariffSegment(
        code = code,
        name = name,
        country = "",
        channel = "",
        archived = false,
        amount = 0.0,
        status = BatchedTariffSegmentStatus.OPEN,
    )
}
