package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched emission factor request is in its life cycle. */
enum class BatchedEmissionFactorRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a batched emission factor request and its current status. */
data class BatchedEmissionFactorRequest(
    var code: String,
    var title: String,
    var checksum: String,
    var summary: String,
    var odometer: Int,
    var graceDays: Int,
    var floorValue: Double,
    var status: BatchedEmissionFactorRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedEmissionFactorRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedEmissionFactorRequestStatus): Boolean {
        when (status) {
            BatchedEmissionFactorRequestStatus.PROPOSED -> {
                when (next) {
                    BatchedEmissionFactorRequestStatus.APPROVED -> return true
                }
                return false
            }
            BatchedEmissionFactorRequestStatus.APPROVED -> return next == BatchedEmissionFactorRequestStatus.REJECTED || next == BatchedEmissionFactorRequestStatus.WITHDRAWN
            BatchedEmissionFactorRequestStatus.REJECTED -> return next == BatchedEmissionFactorRequestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedEmissionFactorRequestStatus) {
        if (!canMoveTo(next)) {
            error("batched emission factor request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched emission factor request"} $code"

    fun adjustedVolume(): Double = round(floorValue * graceDays.toDouble() * 100) / 100

    fun snapshot(): BatchedEmissionFactorRequest {
        return BatchedEmissionFactorRequest(
            code = code,
            title = title,
            checksum = checksum,
            summary = summary,
            odometer = odometer,
            graceDays = graceDays,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched emission factor request in status proposed. */
fun newBatchedEmissionFactorRequest(code: String, title: String): BatchedEmissionFactorRequest {
    return BatchedEmissionFactorRequest(
        code = code,
        title = title,
        checksum = "",
        summary = "",
        odometer = 0,
        graceDays = 0,
        floorValue = 0.0,
        status = BatchedEmissionFactorRequestStatus.PROPOSED,
    )
}
