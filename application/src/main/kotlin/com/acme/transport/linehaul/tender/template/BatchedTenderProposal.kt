package com.acme.transport.linehaul.tender.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched tender proposal is in its life cycle. */
enum class BatchedTenderProposalStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a batched tender proposal and its current status. */
data class BatchedTenderProposal(
    var code: String,
    var title: String,
    var name: String,
    var phone: String,
    var distanceKm: Double,
    var windowDays: Int,
    var version: Int,
    var loadKg: Double,
    var floorValue: Double,
    var pinned: Boolean,
    var status: BatchedTenderProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTenderProposalStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTenderProposalStatus): Boolean {
        when (status) {
            BatchedTenderProposalStatus.PLANNED -> {
                when (next) {
                    BatchedTenderProposalStatus.IN_PROGRESS -> return true
                }
                return false
            }
            BatchedTenderProposalStatus.SCHEDULED -> {
                when (next) {
                    BatchedTenderProposalStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedTenderProposalStatus.IN_PROGRESS -> return next == BatchedTenderProposalStatus.CANCELLED
            BatchedTenderProposalStatus.COMPLETED -> return next == BatchedTenderProposalStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BatchedTenderProposalStatus) {
        if (!canMoveTo(next)) {
            error("batched tender proposal $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"batched tender proposal"} $code"

    fun weightedLoad(): Double = round((version.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): BatchedTenderProposal {
        return BatchedTenderProposal(
            code = code,
            title = title,
            name = name,
            phone = phone,
            distanceKm = distanceKm,
            windowDays = windowDays,
            version = version,
            loadKg = loadKg,
            floorValue = floorValue,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched tender proposal in status planned. */
fun newBatchedTenderProposal(code: String, title: String): BatchedTenderProposal {
    return BatchedTenderProposal(
        code = code,
        title = title,
        name = "",
        phone = "",
        distanceKm = 0.0,
        windowDays = 0,
        version = 0,
        loadKg = 0.0,
        floorValue = 0.0,
        pinned = false,
        status = BatchedTenderProposalStatus.PLANNED,
    )
}
