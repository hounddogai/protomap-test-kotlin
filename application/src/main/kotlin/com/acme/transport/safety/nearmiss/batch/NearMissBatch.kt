package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The near miss batch records of safety's near miss work, and the rules, stores, and calculations around
// them.

/** Where a near miss batch is in its life cycle. */
enum class NearMissBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A near miss batch. Its code is unique within a tenant. */
data class NearMissBatch(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var amount: Double,
    var loadKg: Double,
    var score: Double,
    var pinned: Boolean,
    var lane: String,
    var createdAt: Int,
    var threshold: Double,
    var status: NearMissBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissBatchStatus): Boolean {
        when (status) {
            NearMissBatchStatus.OPEN -> return next == NearMissBatchStatus.REOPENED || next == NearMissBatchStatus.ARCHIVED
            NearMissBatchStatus.IN_REVIEW -> {
                when (next) {
                    NearMissBatchStatus.RESOLVED, NearMissBatchStatus.REOPENED -> return true
                }
                return false
            }
            NearMissBatchStatus.RESOLVED -> {
                when (next) {
                    NearMissBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            NearMissBatchStatus.REOPENED -> return next == NearMissBatchStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: NearMissBatchStatus) {
        if (!canMoveTo(next)) {
            error("near miss batch $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedLoad(): Double = round((score - amount) * 100) / 100

    fun snapshot(): NearMissBatch {
        return NearMissBatch(
            code = code,
            title = title,
            expiresAt = expiresAt,
            amount = amount,
            loadKg = loadKg,
            score = score,
            pinned = pinned,
            lane = lane,
            createdAt = createdAt,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a near miss batch in status open. */
fun newNearMissBatch(code: String, title: String): NearMissBatch {
    return NearMissBatch(
        code = code,
        title = title,
        expiresAt = 0,
        amount = 0.0,
        loadKg = 0.0,
        score = 0.0,
        pinned = false,
        lane = "",
        createdAt = 0,
        threshold = 0.0,
        status = NearMissBatchStatus.OPEN,
    )
}
