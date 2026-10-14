package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched tender estimate is in its life cycle. */
enum class BatchedTenderEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One batched tender estimate, with what the planners need to act on it. */
data class BatchedTenderEstimate(
    var code: String,
    var title: String,
    var checksum: String,
    var archived: Boolean,
    var visible: Boolean,
    var threshold: Double,
    var label: String,
    var durationMinutes: Int,
    var contactEmail: String,
    var trailerId: String,
    var margin: Double,
    var status: BatchedTenderEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTenderEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTenderEstimateStatus): Boolean {
        when (status) {
            BatchedTenderEstimateStatus.REQUESTED -> return next == BatchedTenderEstimateStatus.GRANTED || next == BatchedTenderEstimateStatus.EXPIRED
            BatchedTenderEstimateStatus.GRANTED -> {
                when (next) {
                    BatchedTenderEstimateStatus.REVOKED, BatchedTenderEstimateStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedTenderEstimateStatus.REVOKED -> {
                when (next) {
                    BatchedTenderEstimateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTenderEstimateStatus) {
        if (!canMoveTo(next)) {
            error("batched tender estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun netLoad(): Double = round((threshold + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): BatchedTenderEstimate {
        return BatchedTenderEstimate(
            code = code,
            title = title,
            checksum = checksum,
            archived = archived,
            visible = visible,
            threshold = threshold,
            label = label,
            durationMinutes = durationMinutes,
            contactEmail = contactEmail,
            trailerId = trailerId,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched tender estimate in status requested. */
fun newBatchedTenderEstimate(code: String, title: String): BatchedTenderEstimate {
    return BatchedTenderEstimate(
        code = code,
        title = title,
        checksum = "",
        archived = false,
        visible = false,
        threshold = 0.0,
        label = "",
        durationMinutes = 0,
        contactEmail = "",
        trailerId = "",
        margin = 0.0,
        status = BatchedTenderEstimateStatus.REQUESTED,
    )
}
