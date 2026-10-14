package com.acme.transport.crewing.qualification.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched qualification cycle is in its life cycle. */
enum class BatchedQualificationCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a batched qualification cycle and its current status. */
data class BatchedQualificationCycle(
    var code: String,
    var title: String,
    var graceDays: Int,
    var startsAt: Int,
    var createdAt: Int,
    var trailerId: String,
    var phone: String,
    var weight: Double,
    var name: String,
    var source: String,
    var status: BatchedQualificationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedQualificationCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedQualificationCycleStatus): Boolean {
        when (status) {
            BatchedQualificationCycleStatus.PROPOSED -> return next == BatchedQualificationCycleStatus.APPROVED || next == BatchedQualificationCycleStatus.REJECTED
            BatchedQualificationCycleStatus.APPROVED -> {
                when (next) {
                    BatchedQualificationCycleStatus.REJECTED, BatchedQualificationCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedQualificationCycleStatus.REJECTED -> return next == BatchedQualificationCycleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedQualificationCycleStatus) {
        if (!canMoveTo(next)) {
            error("batched qualification cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedLoad(): Double = round((createdAt.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): BatchedQualificationCycle {
        return BatchedQualificationCycle(
            code = code,
            title = title,
            graceDays = graceDays,
            startsAt = startsAt,
            createdAt = createdAt,
            trailerId = trailerId,
            phone = phone,
            weight = weight,
            name = name,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched qualification cycle in status proposed. */
fun newBatchedQualificationCycle(code: String, title: String): BatchedQualificationCycle {
    return BatchedQualificationCycle(
        code = code,
        title = title,
        graceDays = 0,
        startsAt = 0,
        createdAt = 0,
        trailerId = "",
        phone = "",
        weight = 0.0,
        name = "",
        source = "",
        status = BatchedQualificationCycleStatus.PROPOSED,
    )
}
