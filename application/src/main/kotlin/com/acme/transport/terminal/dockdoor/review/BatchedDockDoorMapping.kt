package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched dock door mapping is in its life cycle. */
enum class BatchedDockDoorMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched dock door mapping. Its code is unique within a tenant. */
data class BatchedDockDoorMapping(
    var code: String,
    var title: String,
    var minimum: Double,
    var source: String,
    var rate: Double,
    var description: String,
    var currency: String,
    var odometer: Int,
    var startsAt: Int,
    var durationMinutes: Int,
    var status: BatchedDockDoorMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDockDoorMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDockDoorMappingStatus): Boolean {
        when (status) {
            BatchedDockDoorMappingStatus.OPEN -> {
                when (next) {
                    BatchedDockDoorMappingStatus.REOPENED, BatchedDockDoorMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedDockDoorMappingStatus.IN_REVIEW -> {
                when (next) {
                    BatchedDockDoorMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedDockDoorMappingStatus.RESOLVED -> {
                when (next) {
                    BatchedDockDoorMappingStatus.REOPENED -> return true
                }
                return false
            }
            BatchedDockDoorMappingStatus.REOPENED -> {
                when (next) {
                    BatchedDockDoorMappingStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedDockDoorMappingStatus) {
        if (!canMoveTo(next)) {
            error("batched dock door mapping $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netExposure(): Double = round(odometer.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): BatchedDockDoorMapping {
        return BatchedDockDoorMapping(
            code = code,
            title = title,
            minimum = minimum,
            source = source,
            rate = rate,
            description = description,
            currency = currency,
            odometer = odometer,
            startsAt = startsAt,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched dock door mapping in status open. */
fun newBatchedDockDoorMapping(code: String, title: String): BatchedDockDoorMapping {
    return BatchedDockDoorMapping(
        code = code,
        title = title,
        minimum = 0.0,
        source = "",
        rate = 0.0,
        description = "",
        currency = "",
        odometer = 0,
        startsAt = 0,
        durationMinutes = 0,
        status = BatchedDockDoorMappingStatus.OPEN,
    )
}
