package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.text.normalize

/** Where a batched locker claim is in its life cycle. */
enum class BatchedLockerClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One batched locker claim, with what the planners need to act on it. */
data class BatchedLockerClaim(
    var code: String,
    var name: String,
    var driverId: String,
    var region: String,
    var segment: String,
    var country: String,
    var dock: String,
    var ceiling: Double,
    var status: BatchedLockerClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedLockerClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedLockerClaimStatus): Boolean {
        when (status) {
            BatchedLockerClaimStatus.CREATED -> {
                when (next) {
                    BatchedLockerClaimStatus.PUBLISHED, BatchedLockerClaimStatus.RETIRED -> return true
                }
                return false
            }
            BatchedLockerClaimStatus.VALIDATED -> {
                when (next) {
                    BatchedLockerClaimStatus.RETIRED -> return true
                }
                return false
            }
            BatchedLockerClaimStatus.PUBLISHED -> {
                when (next) {
                    BatchedLockerClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedLockerClaimStatus) {
        if (!canMoveTo(next)) {
            error("batched locker claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = ceiling

    fun snapshot(): BatchedLockerClaim {
        return BatchedLockerClaim(
            code = code,
            name = name,
            driverId = driverId,
            region = region,
            segment = segment,
            country = country,
            dock = dock,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a batched locker claim in status created. */
fun newBatchedLockerClaim(code: String, name: String): BatchedLockerClaim {
    return BatchedLockerClaim(
        code = code,
        name = name,
        driverId = "",
        region = "",
        segment = "",
        country = "",
        dock = "",
        ceiling = 0.0,
        status = BatchedLockerClaimStatus.CREATED,
    )
}
