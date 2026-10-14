package com.acme.transport.network.servicearea.areacycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched service area cycle records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a batched service area cycle is in its life cycle. */
enum class BatchedServiceAreaCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A batched service area cycle. Its code is unique within a tenant. */
data class BatchedServiceAreaCycle(
    var code: String,
    var name: String,
    var shiftCode: String,
    var graceDays: Int,
    var quantity: Int,
    var checksum: String,
    var title: String,
    var status: BatchedServiceAreaCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedServiceAreaCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedServiceAreaCycleStatus): Boolean {
        when (status) {
            BatchedServiceAreaCycleStatus.REQUESTED -> return next == BatchedServiceAreaCycleStatus.GRANTED
            BatchedServiceAreaCycleStatus.GRANTED -> {
                when (next) {
                    BatchedServiceAreaCycleStatus.REVOKED, BatchedServiceAreaCycleStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedServiceAreaCycleStatus.REVOKED -> {
                when (next) {
                    BatchedServiceAreaCycleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedServiceAreaCycleStatus) {
        if (!canMoveTo(next)) {
            error("batched service area cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round((quantity.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): BatchedServiceAreaCycle {
        return BatchedServiceAreaCycle(
            code = code,
            name = name,
            shiftCode = shiftCode,
            graceDays = graceDays,
            quantity = quantity,
            checksum = checksum,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched service area cycle in status requested. */
fun newBatchedServiceAreaCycle(code: String, name: String): BatchedServiceAreaCycle {
    return BatchedServiceAreaCycle(
        code = code,
        name = name,
        shiftCode = "",
        graceDays = 0,
        quantity = 0,
        checksum = "",
        title = "",
        status = BatchedServiceAreaCycleStatus.REQUESTED,
    )
}
