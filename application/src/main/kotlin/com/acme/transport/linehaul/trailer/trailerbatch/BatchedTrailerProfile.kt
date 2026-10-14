package com.acme.transport.linehaul.trailer.trailerbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched trailer profile is in its life cycle. */
enum class BatchedTrailerProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One batched trailer profile, with what the planners need to act on it. */
data class BatchedTrailerProfile(
    var code: String,
    var label: String,
    var driverId: String,
    var threshold: Double,
    var title: String,
    var dock: String,
    var limit: Int,
    var visible: Boolean,
    var status: BatchedTrailerProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTrailerProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTrailerProfileStatus): Boolean {
        when (status) {
            BatchedTrailerProfileStatus.REQUESTED -> {
                when (next) {
                    BatchedTrailerProfileStatus.GRANTED, BatchedTrailerProfileStatus.REVOKED -> return true
                }
                return false
            }
            BatchedTrailerProfileStatus.GRANTED -> return next == BatchedTrailerProfileStatus.EXPIRED
            BatchedTrailerProfileStatus.REVOKED -> {
                when (next) {
                    BatchedTrailerProfileStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTrailerProfileStatus) {
        if (!canMoveTo(next)) {
            error("batched trailer profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedLoad(): Double = round((limit.toDouble() + threshold) * 100) / 100

    fun snapshot(): BatchedTrailerProfile {
        return BatchedTrailerProfile(
            code = code,
            label = label,
            driverId = driverId,
            threshold = threshold,
            title = title,
            dock = dock,
            limit = limit,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched trailer profile in status requested. */
fun newBatchedTrailerProfile(code: String, label: String): BatchedTrailerProfile {
    return BatchedTrailerProfile(
        code = code,
        label = label,
        driverId = "",
        threshold = 0.0,
        title = "",
        dock = "",
        limit = 0,
        visible = false,
        status = BatchedTrailerProfileStatus.REQUESTED,
    )
}
