package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback attempt request records of lastmile's attempt work, and the rules, stores, and calculations
// around them.

/** Where a fallback attempt request is in its life cycle. */
enum class FallbackAttemptRequestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A fallback attempt request as the attempt team keeps it. */
data class FallbackAttemptRequest(
    var code: String,
    var label: String,
    var description: String,
    var phone: String,
    var createdAt: Int,
    var segment: String,
    var batchSize: Int,
    var externalRef: String,
    var ownerId: String,
    var status: FallbackAttemptRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAttemptRequestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAttemptRequestStatus): Boolean {
        when (status) {
            FallbackAttemptRequestStatus.IDLE -> return next == FallbackAttemptRequestStatus.DRAINING || next == FallbackAttemptRequestStatus.STOPPED
            FallbackAttemptRequestStatus.WARMING -> {
                when (next) {
                    FallbackAttemptRequestStatus.READY, FallbackAttemptRequestStatus.DRAINING -> return true
                }
                return false
            }
            FallbackAttemptRequestStatus.READY -> return next == FallbackAttemptRequestStatus.DRAINING || next == FallbackAttemptRequestStatus.STOPPED
            FallbackAttemptRequestStatus.DRAINING -> return next == FallbackAttemptRequestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackAttemptRequestStatus) {
        if (!canMoveTo(next)) {
            error("fallback attempt request $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netVolume(): Double = round((batchSize.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackAttemptRequest {
        return FallbackAttemptRequest(
            code = code,
            label = label,
            description = description,
            phone = phone,
            createdAt = createdAt,
            segment = segment,
            batchSize = batchSize,
            externalRef = externalRef,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fallback attempt request in status idle. */
fun newFallbackAttemptRequest(code: String, label: String): FallbackAttemptRequest {
    return FallbackAttemptRequest(
        code = code,
        label = label,
        description = "",
        phone = "",
        createdAt = 0,
        segment = "",
        batchSize = 0,
        externalRef = "",
        ownerId = "",
        status = FallbackAttemptRequestStatus.IDLE,
    )
}
