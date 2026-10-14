package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched route offset mapping is in its life cycle. */
enum class BatchedRouteOffsetMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One batched route offset mapping, with what the planners need to act on it. */
data class BatchedRouteOffsetMapping(
    var code: String,
    var name: String,
    var budget: Double,
    var externalRef: String,
    var shiftCode: String,
    var carrier: String,
    var cost: Double,
    var status: BatchedRouteOffsetMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRouteOffsetMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRouteOffsetMappingStatus): Boolean {
        when (status) {
            BatchedRouteOffsetMappingStatus.PLANNED -> return next == BatchedRouteOffsetMappingStatus.SCHEDULED || next == BatchedRouteOffsetMappingStatus.COMPLETED
            BatchedRouteOffsetMappingStatus.SCHEDULED -> return next == BatchedRouteOffsetMappingStatus.IN_PROGRESS || next == BatchedRouteOffsetMappingStatus.COMPLETED
            BatchedRouteOffsetMappingStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedRouteOffsetMappingStatus.COMPLETED, BatchedRouteOffsetMappingStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedRouteOffsetMappingStatus.COMPLETED -> {
                when (next) {
                    BatchedRouteOffsetMappingStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedRouteOffsetMappingStatus) {
        if (!canMoveTo(next)) {
            error("batched route offset mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round(budget * cost * 100) / 100

    fun snapshot(): BatchedRouteOffsetMapping {
        return BatchedRouteOffsetMapping(
            code = code,
            name = name,
            budget = budget,
            externalRef = externalRef,
            shiftCode = shiftCode,
            carrier = carrier,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched route offset mapping in status planned. */
fun newBatchedRouteOffsetMapping(code: String, name: String): BatchedRouteOffsetMapping {
    return BatchedRouteOffsetMapping(
        code = code,
        name = name,
        budget = 0.0,
        externalRef = "",
        shiftCode = "",
        carrier = "",
        cost = 0.0,
        status = BatchedRouteOffsetMappingStatus.PLANNED,
    )
}
