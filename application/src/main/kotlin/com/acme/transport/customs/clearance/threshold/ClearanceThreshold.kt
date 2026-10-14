package com.acme.transport.customs.clearance.threshold

import com.acme.transport.kit.text.normalize

// The clearance threshold records of customs's clearance work, and the rules, stores, and calculations around
// them.

/** Where a clearance threshold is in its life cycle. */
enum class ClearanceThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A clearance threshold. Its code is unique within a tenant. */
data class ClearanceThreshold(
    var code: String,
    var label: String,
    var visible: Boolean,
    var checksum: String,
    var stopCount: Int,
    var status: ClearanceThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceThresholdStatus): Boolean {
        when (status) {
            ClearanceThresholdStatus.IDLE -> return next == ClearanceThresholdStatus.WARMING || next == ClearanceThresholdStatus.READY
            ClearanceThresholdStatus.WARMING -> return next == ClearanceThresholdStatus.READY || next == ClearanceThresholdStatus.DRAINING
            ClearanceThresholdStatus.READY -> {
                when (next) {
                    ClearanceThresholdStatus.DRAINING, ClearanceThresholdStatus.STOPPED -> return true
                }
                return false
            }
            ClearanceThresholdStatus.DRAINING -> {
                when (next) {
                    ClearanceThresholdStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceThresholdStatus) {
        if (!canMoveTo(next)) {
            error("clearance threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"clearance threshold"} $code"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): ClearanceThreshold {
        return ClearanceThreshold(
            code = code,
            label = label,
            visible = visible,
            checksum = checksum,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a clearance threshold in status idle. */
fun newClearanceThreshold(code: String, label: String): ClearanceThreshold {
    return ClearanceThreshold(
        code = code,
        label = label,
        visible = false,
        checksum = "",
        stopCount = 0,
        status = ClearanceThresholdStatus.IDLE,
    )
}
