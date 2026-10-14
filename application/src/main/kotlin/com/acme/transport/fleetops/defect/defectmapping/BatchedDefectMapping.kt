package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched defect mapping records of fleetops's defect work, and the rules, stores, and calculations
// around them.

/** Where a batched defect mapping is in its life cycle. */
enum class BatchedDefectMappingStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a batched defect mapping and its current status. */
data class BatchedDefectMapping(
    var code: String,
    var title: String,
    var archived: Boolean,
    var reviewer: String,
    var lane: String,
    var rate: Double,
    var active: Boolean,
    var phone: String,
    var trailerId: String,
    var expiresAt: Int,
    var windowDays: Int,
    var status: BatchedDefectMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDefectMappingStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDefectMappingStatus): Boolean {
        when (status) {
            BatchedDefectMappingStatus.DRAFT -> {
                when (next) {
                    BatchedDefectMappingStatus.ACTIVE, BatchedDefectMappingStatus.SUSPENDED -> return true
                }
                return false
            }
            BatchedDefectMappingStatus.PENDING -> return next == BatchedDefectMappingStatus.ACTIVE || next == BatchedDefectMappingStatus.SUSPENDED
            BatchedDefectMappingStatus.ACTIVE -> {
                when (next) {
                    BatchedDefectMappingStatus.CLOSED -> return true
                }
                return false
            }
            BatchedDefectMappingStatus.SUSPENDED -> return next == BatchedDefectMappingStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BatchedDefectMappingStatus) {
        if (!canMoveTo(next)) {
            error("batched defect mapping $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"batched defect mapping"} $code"

    fun projectedTotal(): Double = round((rate + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedDefectMapping {
        return BatchedDefectMapping(
            code = code,
            title = title,
            archived = archived,
            reviewer = reviewer,
            lane = lane,
            rate = rate,
            active = active,
            phone = phone,
            trailerId = trailerId,
            expiresAt = expiresAt,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched defect mapping in status draft. */
fun newBatchedDefectMapping(code: String, title: String): BatchedDefectMapping {
    return BatchedDefectMapping(
        code = code,
        title = title,
        archived = false,
        reviewer = "",
        lane = "",
        rate = 0.0,
        active = false,
        phone = "",
        trailerId = "",
        expiresAt = 0,
        windowDays = 0,
        status = BatchedDefectMappingStatus.DRAFT,
    )
}
