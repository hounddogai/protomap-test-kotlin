package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched accessorial reservation is in its life cycle. */
enum class BatchedAccessorialReservationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched accessorial reservation as the accessorial team keeps it. */
data class BatchedAccessorialReservation(
    var code: String,
    var title: String,
    var region: String,
    var checksum: String,
    var unit: String,
    var ratio: Double,
    var approved: Boolean,
    var durationMinutes: Int,
    var priority: Int,
    var rate: Double,
    var status: BatchedAccessorialReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAccessorialReservationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAccessorialReservationStatus): Boolean {
        when (status) {
            BatchedAccessorialReservationStatus.PROPOSED -> {
                when (next) {
                    BatchedAccessorialReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedAccessorialReservationStatus.APPROVED -> {
                when (next) {
                    BatchedAccessorialReservationStatus.REJECTED, BatchedAccessorialReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedAccessorialReservationStatus.REJECTED -> return next == BatchedAccessorialReservationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedAccessorialReservationStatus) {
        if (!canMoveTo(next)) {
            error("batched accessorial reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched accessorial reservation"} $code"

    fun adjustedVolume(): Double = round((durationMinutes.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): BatchedAccessorialReservation {
        return BatchedAccessorialReservation(
            code = code,
            title = title,
            region = region,
            checksum = checksum,
            unit = unit,
            ratio = ratio,
            approved = approved,
            durationMinutes = durationMinutes,
            priority = priority,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a batched accessorial reservation in status proposed. */
fun newBatchedAccessorialReservation(code: String, title: String): BatchedAccessorialReservation {
    return BatchedAccessorialReservation(
        code = code,
        title = title,
        region = "",
        checksum = "",
        unit = "",
        ratio = 0.0,
        approved = false,
        durationMinutes = 0,
        priority = 0,
        rate = 0.0,
        status = BatchedAccessorialReservationStatus.PROPOSED,
    )
}
