package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched service plan record records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a batched service plan record is in its life cycle. */
enum class BatchedServicePlanRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a batched service plan record and its current status. */
data class BatchedServicePlanRecord(
    var code: String,
    var label: String,
    var contactEmail: String,
    var locale: String,
    var sequence: Int,
    var expiresAt: Int,
    var ceiling: Double,
    var description: String,
    var ownerId: String,
    var score: Double,
    var status: BatchedServicePlanRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedServicePlanRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedServicePlanRecordStatus): Boolean {
        when (status) {
            BatchedServicePlanRecordStatus.PROPOSED -> {
                when (next) {
                    BatchedServicePlanRecordStatus.REJECTED, BatchedServicePlanRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedServicePlanRecordStatus.APPROVED -> return next == BatchedServicePlanRecordStatus.REJECTED || next == BatchedServicePlanRecordStatus.WITHDRAWN
            BatchedServicePlanRecordStatus.REJECTED -> return next == BatchedServicePlanRecordStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedServicePlanRecordStatus) {
        if (!canMoveTo(next)) {
            error("batched service plan record $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"batched service plan record"} $code"

    fun weightedExposure(): Double = round((ceiling - sequence.toDouble()) * 100) / 100

    fun snapshot(): BatchedServicePlanRecord {
        return BatchedServicePlanRecord(
            code = code,
            label = label,
            contactEmail = contactEmail,
            locale = locale,
            sequence = sequence,
            expiresAt = expiresAt,
            ceiling = ceiling,
            description = description,
            ownerId = ownerId,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a batched service plan record in status proposed. */
fun newBatchedServicePlanRecord(code: String, label: String): BatchedServicePlanRecord {
    return BatchedServicePlanRecord(
        code = code,
        label = label,
        contactEmail = "",
        locale = "",
        sequence = 0,
        expiresAt = 0,
        ceiling = 0.0,
        description = "",
        ownerId = "",
        score = 0.0,
        status = BatchedServicePlanRecordStatus.PROPOSED,
    )
}
