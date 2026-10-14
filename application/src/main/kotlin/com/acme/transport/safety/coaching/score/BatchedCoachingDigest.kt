package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched coaching digest is in its life cycle. */
enum class BatchedCoachingDigestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One batched coaching digest, with what the planners need to act on it. */
data class BatchedCoachingDigest(
    var code: String,
    var name: String,
    var floorValue: Double,
    var startsAt: Int,
    var loadKg: Double,
    var visible: Boolean,
    var routeCode: String,
    var notes: String,
    var status: BatchedCoachingDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCoachingDigestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCoachingDigestStatus): Boolean {
        when (status) {
            BatchedCoachingDigestStatus.PROPOSED -> {
                when (next) {
                    BatchedCoachingDigestStatus.APPROVED, BatchedCoachingDigestStatus.REJECTED -> return true
                }
                return false
            }
            BatchedCoachingDigestStatus.APPROVED -> {
                when (next) {
                    BatchedCoachingDigestStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedCoachingDigestStatus.REJECTED -> return next == BatchedCoachingDigestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedCoachingDigestStatus) {
        if (!canMoveTo(next)) {
            error("batched coaching digest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"batched coaching digest"} $code"

    fun adjustedLoad(): Double = round((floorValue - startsAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedCoachingDigest {
        return BatchedCoachingDigest(
            code = code,
            name = name,
            floorValue = floorValue,
            startsAt = startsAt,
            loadKg = loadKg,
            visible = visible,
            routeCode = routeCode,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched coaching digest in status proposed. */
fun newBatchedCoachingDigest(code: String, name: String): BatchedCoachingDigest {
    return BatchedCoachingDigest(
        code = code,
        name = name,
        floorValue = 0.0,
        startsAt = 0,
        loadKg = 0.0,
        visible = false,
        routeCode = "",
        notes = "",
        status = BatchedCoachingDigestStatus.PROPOSED,
    )
}
