package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched backhaul request records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a batched backhaul request is in its life cycle. */
enum class BatchedBackhaulRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A batched backhaul request. Its code is unique within a tenant. */
data class BatchedBackhaulRequest(
    var code: String,
    var label: String,
    var ceiling: Double,
    var dock: String,
    var region: String,
    var endsAt: Int,
    var priority: Int,
    var odometer: Int,
    var status: BatchedBackhaulRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedBackhaulRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedBackhaulRequestStatus): Boolean {
        when (status) {
            BatchedBackhaulRequestStatus.REQUESTED -> return next == BatchedBackhaulRequestStatus.GRANTED
            BatchedBackhaulRequestStatus.GRANTED -> return next == BatchedBackhaulRequestStatus.REVOKED
            BatchedBackhaulRequestStatus.REVOKED -> return next == BatchedBackhaulRequestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedBackhaulRequestStatus) {
        if (!canMoveTo(next)) {
            error("batched backhaul request $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"batched backhaul request"} $code"

    fun weightedLoad(): Double = round((ceiling - endsAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedBackhaulRequest {
        return BatchedBackhaulRequest(
            code = code,
            label = label,
            ceiling = ceiling,
            dock = dock,
            region = region,
            endsAt = endsAt,
            priority = priority,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a batched backhaul request in status requested. */
fun newBatchedBackhaulRequest(code: String, label: String): BatchedBackhaulRequest {
    return BatchedBackhaulRequest(
        code = code,
        label = label,
        ceiling = 0.0,
        dock = "",
        region = "",
        endsAt = 0,
        priority = 0,
        odometer = 0,
        status = BatchedBackhaulRequestStatus.REQUESTED,
    )
}
