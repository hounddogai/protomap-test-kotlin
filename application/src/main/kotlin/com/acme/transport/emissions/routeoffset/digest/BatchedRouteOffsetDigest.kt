package com.acme.transport.emissions.routeoffset.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched route offset digest records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a batched route offset digest is in its life cycle. */
enum class BatchedRouteOffsetDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a batched route offset digest and its current status. */
data class BatchedRouteOffsetDigest(
    var code: String,
    var name: String,
    var approved: Boolean,
    var floorValue: Double,
    var etaMinutes: Int,
    var cost: Double,
    var category: String,
    var carrier: String,
    var distanceKm: Double,
    var status: BatchedRouteOffsetDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRouteOffsetDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRouteOffsetDigestStatus): Boolean {
        when (status) {
            BatchedRouteOffsetDigestStatus.NEW -> return next == BatchedRouteOffsetDigestStatus.QUEUED || next == BatchedRouteOffsetDigestStatus.RUNNING
            BatchedRouteOffsetDigestStatus.QUEUED -> {
                when (next) {
                    BatchedRouteOffsetDigestStatus.RUNNING, BatchedRouteOffsetDigestStatus.DONE -> return true
                }
                return false
            }
            BatchedRouteOffsetDigestStatus.RUNNING -> return next == BatchedRouteOffsetDigestStatus.DONE || next == BatchedRouteOffsetDigestStatus.FAILED
            BatchedRouteOffsetDigestStatus.DONE -> {
                when (next) {
                    BatchedRouteOffsetDigestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedRouteOffsetDigestStatus) {
        if (!canMoveTo(next)) {
            error("batched route offset digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveLoad(): Double = round((floorValue + cost) * 100) / 100

    fun snapshot(): BatchedRouteOffsetDigest {
        return BatchedRouteOffsetDigest(
            code = code,
            name = name,
            approved = approved,
            floorValue = floorValue,
            etaMinutes = etaMinutes,
            cost = cost,
            category = category,
            carrier = carrier,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a batched route offset digest in status new. */
fun newBatchedRouteOffsetDigest(code: String, name: String): BatchedRouteOffsetDigest {
    return BatchedRouteOffsetDigest(
        code = code,
        name = name,
        approved = false,
        floorValue = 0.0,
        etaMinutes = 0,
        cost = 0.0,
        category = "",
        carrier = "",
        distanceKm = 0.0,
        status = BatchedRouteOffsetDigestStatus.NEW,
    )
}
