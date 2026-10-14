package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched transit time entry is in its life cycle. */
enum class BatchedTransitTimeEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched transit time entry. Its code is unique within a tenant. */
data class BatchedTransitTimeEntry(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var cost: Double,
    var threshold: Double,
    var revision: Int,
    var dock: String,
    var rate: Double,
    var startsAt: Int,
    var ownerId: String,
    var status: BatchedTransitTimeEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTransitTimeEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTransitTimeEntryStatus): Boolean {
        when (status) {
            BatchedTransitTimeEntryStatus.OPEN -> {
                when (next) {
                    BatchedTransitTimeEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedTransitTimeEntryStatus.IN_REVIEW -> {
                when (next) {
                    BatchedTransitTimeEntryStatus.RESOLVED, BatchedTransitTimeEntryStatus.REOPENED -> return true
                }
                return false
            }
            BatchedTransitTimeEntryStatus.RESOLVED -> return next == BatchedTransitTimeEntryStatus.ARCHIVED
            BatchedTransitTimeEntryStatus.REOPENED -> return next == BatchedTransitTimeEntryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BatchedTransitTimeEntryStatus) {
        if (!canMoveTo(next)) {
            error("batched transit time entry $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"batched transit time entry"} $code"

    fun adjustedLoad(): Double = round((revision.toDouble() + cost) * 100) / 100

    fun snapshot(): BatchedTransitTimeEntry {
        return BatchedTransitTimeEntry(
            code = code,
            title = title,
            distanceKm = distanceKm,
            cost = cost,
            threshold = threshold,
            revision = revision,
            dock = dock,
            rate = rate,
            startsAt = startsAt,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched transit time entry in status open. */
fun newBatchedTransitTimeEntry(code: String, title: String): BatchedTransitTimeEntry {
    return BatchedTransitTimeEntry(
        code = code,
        title = title,
        distanceKm = 0.0,
        cost = 0.0,
        threshold = 0.0,
        revision = 0,
        dock = "",
        rate = 0.0,
        startsAt = 0,
        ownerId = "",
        status = BatchedTransitTimeEntryStatus.OPEN,
    )
}
