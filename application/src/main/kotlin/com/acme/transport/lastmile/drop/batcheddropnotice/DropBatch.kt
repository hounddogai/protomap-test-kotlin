package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop batch is in its life cycle. */
enum class DropBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A drop batch. Its code is unique within a tenant. */
data class DropBatch(
    var code: String,
    var title: String,
    var ratio: Double,
    var checksum: String,
    var durationMinutes: Int,
    var odometer: Int,
    var cost: Double,
    var status: DropBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropBatchStatus): Boolean {
        when (status) {
            DropBatchStatus.OPEN -> {
                when (next) {
                    DropBatchStatus.IN_REVIEW -> return true
                }
                return false
            }
            DropBatchStatus.IN_REVIEW -> {
                when (next) {
                    DropBatchStatus.REOPENED -> return true
                }
                return false
            }
            DropBatchStatus.RESOLVED -> {
                when (next) {
                    DropBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            DropBatchStatus.REOPENED -> {
                when (next) {
                    DropBatchStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropBatchStatus) {
        if (!canMoveTo(next)) {
            error("drop batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((ratio + cost) * 100) / 100

    fun snapshot(): DropBatch {
        return DropBatch(
            code = code,
            title = title,
            ratio = ratio,
            checksum = checksum,
            durationMinutes = durationMinutes,
            odometer = odometer,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a drop batch in status open. */
fun newDropBatch(code: String, title: String): DropBatch {
    return DropBatch(
        code = code,
        title = title,
        ratio = 0.0,
        checksum = "",
        durationMinutes = 0,
        odometer = 0,
        cost = 0.0,
        status = DropBatchStatus.OPEN,
    )
}
