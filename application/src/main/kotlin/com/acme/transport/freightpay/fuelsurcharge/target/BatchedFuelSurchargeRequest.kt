package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.kit.text.normalize

/** Where a batched fuel surcharge request is in its life cycle. */
enum class BatchedFuelSurchargeRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One batched fuel surcharge request, with what the planners need to act on it. */
data class BatchedFuelSurchargeRequest(
    var code: String,
    var title: String,
    var checksum: String,
    var channel: String,
    var durationMinutes: Int,
    var status: BatchedFuelSurchargeRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedFuelSurchargeRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedFuelSurchargeRequestStatus): Boolean {
        when (status) {
            BatchedFuelSurchargeRequestStatus.REQUESTED -> {
                when (next) {
                    BatchedFuelSurchargeRequestStatus.GRANTED, BatchedFuelSurchargeRequestStatus.REVOKED -> return true
                }
                return false
            }
            BatchedFuelSurchargeRequestStatus.GRANTED -> {
                when (next) {
                    BatchedFuelSurchargeRequestStatus.REVOKED, BatchedFuelSurchargeRequestStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedFuelSurchargeRequestStatus.REVOKED -> return next == BatchedFuelSurchargeRequestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedFuelSurchargeRequestStatus) {
        if (!canMoveTo(next)) {
            error("batched fuel surcharge request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = durationMinutes.toDouble()

    fun snapshot(): BatchedFuelSurchargeRequest {
        return BatchedFuelSurchargeRequest(
            code = code,
            title = title,
            checksum = checksum,
            channel = channel,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a batched fuel surcharge request in status requested. */
fun newBatchedFuelSurchargeRequest(code: String, title: String): BatchedFuelSurchargeRequest {
    return BatchedFuelSurchargeRequest(
        code = code,
        title = title,
        checksum = "",
        channel = "",
        durationMinutes = 0,
        status = BatchedFuelSurchargeRequestStatus.REQUESTED,
    )
}
