package com.acme.transport.network.node.nodegroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped node group records of network's node work, and the rules, stores, and calculations around them.

/** Where a capped node group is in its life cycle. */
enum class CappedNodeGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A capped node group as the node team keeps it. */
data class CappedNodeGroup(
    var code: String,
    var label: String,
    var approved: Boolean,
    var carrier: String,
    var etaMinutes: Int,
    var maximum: Double,
    var version: Int,
    var limit: Int,
    var category: String,
    var retries: Int,
    var status: CappedNodeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedNodeGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedNodeGroupStatus): Boolean {
        when (status) {
            CappedNodeGroupStatus.PLANNED -> {
                when (next) {
                    CappedNodeGroupStatus.SCHEDULED, CappedNodeGroupStatus.IN_PROGRESS -> return true
                }
                return false
            }
            CappedNodeGroupStatus.SCHEDULED -> return next == CappedNodeGroupStatus.IN_PROGRESS || next == CappedNodeGroupStatus.COMPLETED
            CappedNodeGroupStatus.IN_PROGRESS -> {
                when (next) {
                    CappedNodeGroupStatus.CANCELLED -> return true
                }
                return false
            }
            CappedNodeGroupStatus.COMPLETED -> return next == CappedNodeGroupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CappedNodeGroupStatus) {
        if (!canMoveTo(next)) {
            error("capped node group $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedExposure(): Double = round((version.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): CappedNodeGroup {
        return CappedNodeGroup(
            code = code,
            label = label,
            approved = approved,
            carrier = carrier,
            etaMinutes = etaMinutes,
            maximum = maximum,
            version = version,
            limit = limit,
            category = category,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped node group in status planned. */
fun newCappedNodeGroup(code: String, label: String): CappedNodeGroup {
    return CappedNodeGroup(
        code = code,
        label = label,
        approved = false,
        carrier = "",
        etaMinutes = 0,
        maximum = 0.0,
        version = 0,
        limit = 0,
        category = "",
        retries = 0,
        status = CappedNodeGroupStatus.PLANNED,
    )
}
