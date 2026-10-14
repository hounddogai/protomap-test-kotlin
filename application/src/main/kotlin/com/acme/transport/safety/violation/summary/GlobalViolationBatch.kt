package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global violation batch is in its life cycle. */
enum class GlobalViolationBatchStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global violation batch. Its code is unique within a tenant. */
data class GlobalViolationBatch(
    var code: String,
    var name: String,
    var trailerId: String,
    var minimum: Double,
    var country: String,
    var expiresAt: Int,
    var segment: String,
    var etaMinutes: Int,
    var archived: Boolean,
    var contactEmail: String,
    var stopCount: Int,
    var status: GlobalViolationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalViolationBatchStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalViolationBatchStatus): Boolean {
        when (status) {
            GlobalViolationBatchStatus.IDLE -> return next == GlobalViolationBatchStatus.DRAINING || next == GlobalViolationBatchStatus.STOPPED
            GlobalViolationBatchStatus.WARMING -> return next == GlobalViolationBatchStatus.READY
            GlobalViolationBatchStatus.READY -> return next == GlobalViolationBatchStatus.DRAINING || next == GlobalViolationBatchStatus.STOPPED
            GlobalViolationBatchStatus.DRAINING -> return next == GlobalViolationBatchStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: GlobalViolationBatchStatus) {
        if (!canMoveTo(next)) {
            error("global violation batch $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun weightedExposure(): Double = round((minimum - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalViolationBatch {
        return GlobalViolationBatch(
            code = code,
            name = name,
            trailerId = trailerId,
            minimum = minimum,
            country = country,
            expiresAt = expiresAt,
            segment = segment,
            etaMinutes = etaMinutes,
            archived = archived,
            contactEmail = contactEmail,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global violation batch in status idle. */
fun newGlobalViolationBatch(code: String, name: String): GlobalViolationBatch {
    return GlobalViolationBatch(
        code = code,
        name = name,
        trailerId = "",
        minimum = 0.0,
        country = "",
        expiresAt = 0,
        segment = "",
        etaMinutes = 0,
        archived = false,
        contactEmail = "",
        stopCount = 0,
        status = GlobalViolationBatchStatus.IDLE,
    )
}
