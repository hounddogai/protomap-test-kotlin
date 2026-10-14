package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched near miss segment is in its life cycle. */
enum class BatchedNearMissSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A batched near miss segment. Its code is unique within a tenant. */
data class BatchedNearMissSegment(
    var code: String,
    var name: String,
    var externalRef: String,
    var locale: String,
    var attributes: MutableMap<String, String>,
    var startsAt: Int,
    var limit: Int,
    var version: Int,
    var status: BatchedNearMissSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedNearMissSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedNearMissSegmentStatus): Boolean {
        when (status) {
            BatchedNearMissSegmentStatus.NEW -> {
                when (next) {
                    BatchedNearMissSegmentStatus.QUEUED -> return true
                }
                return false
            }
            BatchedNearMissSegmentStatus.QUEUED -> return next == BatchedNearMissSegmentStatus.FAILED
            BatchedNearMissSegmentStatus.RUNNING -> {
                when (next) {
                    BatchedNearMissSegmentStatus.DONE -> return true
                }
                return false
            }
            BatchedNearMissSegmentStatus.DONE -> {
                when (next) {
                    BatchedNearMissSegmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedNearMissSegmentStatus) {
        if (!canMoveTo(next)) {
            error("batched near miss segment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round(startsAt.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): BatchedNearMissSegment {
        return BatchedNearMissSegment(
            code = code,
            name = name,
            externalRef = externalRef,
            locale = locale,
            attributes = attributes,
            startsAt = startsAt,
            limit = limit,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a batched near miss segment in status new. */
fun newBatchedNearMissSegment(code: String, name: String): BatchedNearMissSegment {
    return BatchedNearMissSegment(
        code = code,
        name = name,
        externalRef = "",
        locale = "",
        attributes = mutableMapOf<String, String>(),
        startsAt = 0,
        limit = 0,
        version = 0,
        status = BatchedNearMissSegmentStatus.NEW,
    )
}
