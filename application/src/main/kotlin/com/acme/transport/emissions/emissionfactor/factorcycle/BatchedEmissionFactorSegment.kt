package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched emission factor segment is in its life cycle. */
enum class BatchedEmissionFactorSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched emission factor segment as the emission factor team keeps it. */
data class BatchedEmissionFactorSegment(
    var code: String,
    var title: String,
    var windowDays: Int,
    var amount: Double,
    var archived: Boolean,
    var active: Boolean,
    var tags: MutableList<String>,
    var threshold: Double,
    var unit: String,
    var status: BatchedEmissionFactorSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedEmissionFactorSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedEmissionFactorSegmentStatus): Boolean {
        when (status) {
            BatchedEmissionFactorSegmentStatus.OPEN -> {
                when (next) {
                    BatchedEmissionFactorSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedEmissionFactorSegmentStatus.IN_REVIEW -> {
                when (next) {
                    BatchedEmissionFactorSegmentStatus.RESOLVED, BatchedEmissionFactorSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedEmissionFactorSegmentStatus.RESOLVED -> return next == BatchedEmissionFactorSegmentStatus.REOPENED
            BatchedEmissionFactorSegmentStatus.REOPENED -> {
                when (next) {
                    BatchedEmissionFactorSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedEmissionFactorSegmentStatus) {
        if (!canMoveTo(next)) {
            error("batched emission factor segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched emission factor segment"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedTotal(): Double = round((threshold - amount) * 100) / 100

    fun snapshot(): BatchedEmissionFactorSegment {
        return BatchedEmissionFactorSegment(
            code = code,
            title = title,
            windowDays = windowDays,
            amount = amount,
            archived = archived,
            active = active,
            tags = tags,
            threshold = threshold,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a batched emission factor segment in status open. */
fun newBatchedEmissionFactorSegment(code: String, title: String): BatchedEmissionFactorSegment {
    return BatchedEmissionFactorSegment(
        code = code,
        title = title,
        windowDays = 0,
        amount = 0.0,
        archived = false,
        active = false,
        tags = mutableListOf<String>(),
        threshold = 0.0,
        unit = "",
        status = BatchedEmissionFactorSegmentStatus.OPEN,
    )
}
