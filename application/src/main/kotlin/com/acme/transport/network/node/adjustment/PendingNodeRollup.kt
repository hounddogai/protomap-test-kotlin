package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending node rollup is in its life cycle. */
enum class PendingNodeRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A pending node rollup. Its code is unique within a tenant. */
data class PendingNodeRollup(
    var code: String,
    var label: String,
    var region: String,
    var source: String,
    var batchSize: Int,
    var checksum: String,
    var rate: Double,
    var externalRef: String,
    var lane: String,
    var sequence: Int,
    var pinned: Boolean,
    var status: PendingNodeRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingNodeRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingNodeRollupStatus): Boolean {
        when (status) {
            PendingNodeRollupStatus.PROPOSED -> return next == PendingNodeRollupStatus.REJECTED
            PendingNodeRollupStatus.APPROVED -> return next == PendingNodeRollupStatus.REJECTED || next == PendingNodeRollupStatus.WITHDRAWN
            PendingNodeRollupStatus.REJECTED -> return next == PendingNodeRollupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: PendingNodeRollupStatus) {
        if (!canMoveTo(next)) {
            error("pending node rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending node rollup"} $code"

    fun netValue(): Double = round((rate + batchSize.toDouble()) * 100) / 100

    fun snapshot(): PendingNodeRollup {
        return PendingNodeRollup(
            code = code,
            label = label,
            region = region,
            source = source,
            batchSize = batchSize,
            checksum = checksum,
            rate = rate,
            externalRef = externalRef,
            lane = lane,
            sequence = sequence,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending node rollup in status proposed. */
fun newPendingNodeRollup(code: String, label: String): PendingNodeRollup {
    return PendingNodeRollup(
        code = code,
        label = label,
        region = "",
        source = "",
        batchSize = 0,
        checksum = "",
        rate = 0.0,
        externalRef = "",
        lane = "",
        sequence = 0,
        pinned = false,
        status = PendingNodeRollupStatus.PROPOSED,
    )
}
