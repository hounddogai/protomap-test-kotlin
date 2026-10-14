package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.text.normalize

/** Where a pending near miss threshold is in its life cycle. */
enum class PendingNearMissThresholdStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending near miss threshold. Its code is unique within a tenant. */
data class PendingNearMissThreshold(
    var code: String,
    var name: String,
    var cost: Double,
    var visible: Boolean,
    var shiftCode: String,
    var status: PendingNearMissThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingNearMissThresholdStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingNearMissThresholdStatus): Boolean {
        when (status) {
            PendingNearMissThresholdStatus.CREATED -> {
                when (next) {
                    PendingNearMissThresholdStatus.VALIDATED, PendingNearMissThresholdStatus.RETIRED -> return true
                }
                return false
            }
            PendingNearMissThresholdStatus.VALIDATED -> {
                when (next) {
                    PendingNearMissThresholdStatus.PUBLISHED, PendingNearMissThresholdStatus.RETIRED -> return true
                }
                return false
            }
            PendingNearMissThresholdStatus.PUBLISHED -> {
                when (next) {
                    PendingNearMissThresholdStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingNearMissThresholdStatus) {
        if (!canMoveTo(next)) {
            error("pending near miss threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending near miss threshold"} $code"

    fun measuredValue(): Double = cost

    fun snapshot(): PendingNearMissThreshold {
        return PendingNearMissThreshold(
            code = code,
            name = name,
            cost = cost,
            visible = visible,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending near miss threshold in status created. */
fun newPendingNearMissThreshold(code: String, name: String): PendingNearMissThreshold {
    return PendingNearMissThreshold(
        code = code,
        name = name,
        cost = 0.0,
        visible = false,
        shiftCode = "",
        status = PendingNearMissThresholdStatus.CREATED,
    )
}
