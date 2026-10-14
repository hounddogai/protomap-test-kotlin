package com.acme.transport.terminal.traileryard.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched trailer yard draft is in its life cycle. */
enum class BatchedTrailerYardDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched trailer yard draft. Its code is unique within a tenant. */
data class BatchedTrailerYardDraft(
    var code: String,
    var title: String,
    var label: String,
    var cost: Double,
    var endsAt: Int,
    var loadKg: Double,
    var archived: Boolean,
    var status: BatchedTrailerYardDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTrailerYardDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTrailerYardDraftStatus): Boolean {
        when (status) {
            BatchedTrailerYardDraftStatus.PROPOSED -> return next == BatchedTrailerYardDraftStatus.WITHDRAWN
            BatchedTrailerYardDraftStatus.APPROVED -> return next == BatchedTrailerYardDraftStatus.REJECTED || next == BatchedTrailerYardDraftStatus.WITHDRAWN
            BatchedTrailerYardDraftStatus.REJECTED -> {
                when (next) {
                    BatchedTrailerYardDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTrailerYardDraftStatus) {
        if (!canMoveTo(next)) {
            error("batched trailer yard draft $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"batched trailer yard draft"} $code"

    fun netVolume(): Double = round((loadKg - cost) * 100) / 100

    fun snapshot(): BatchedTrailerYardDraft {
        return BatchedTrailerYardDraft(
            code = code,
            title = title,
            label = label,
            cost = cost,
            endsAt = endsAt,
            loadKg = loadKg,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched trailer yard draft in status proposed. */
fun newBatchedTrailerYardDraft(code: String, title: String): BatchedTrailerYardDraft {
    return BatchedTrailerYardDraft(
        code = code,
        title = title,
        label = "",
        cost = 0.0,
        endsAt = 0,
        loadKg = 0.0,
        archived = false,
        status = BatchedTrailerYardDraftStatus.PROPOSED,
    )
}
