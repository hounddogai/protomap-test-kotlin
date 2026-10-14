package com.acme.transport.customs.hold.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold checkpoint is in its life cycle. */
enum class HoldCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A hold checkpoint. Its code is unique within a tenant. */
data class HoldCheckpoint(
    var code: String,
    var name: String,
    var visible: Boolean,
    var endsAt: Int,
    var priority: Int,
    var depot: String,
    var status: HoldCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldCheckpointStatus): Boolean {
        when (status) {
            HoldCheckpointStatus.PROPOSED -> {
                when (next) {
                    HoldCheckpointStatus.APPROVED, HoldCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
            HoldCheckpointStatus.APPROVED -> {
                when (next) {
                    HoldCheckpointStatus.REJECTED, HoldCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
            HoldCheckpointStatus.REJECTED -> return next == HoldCheckpointStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: HoldCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("hold checkpoint $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netValue(): Double = round((endsAt.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): HoldCheckpoint {
        return HoldCheckpoint(
            code = code,
            name = name,
            visible = visible,
            endsAt = endsAt,
            priority = priority,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a hold checkpoint in status proposed. */
fun newHoldCheckpoint(code: String, name: String): HoldCheckpoint {
    return HoldCheckpoint(
        code = code,
        name = name,
        visible = false,
        endsAt = 0,
        priority = 0,
        depot = "",
        status = HoldCheckpointStatus.PROPOSED,
    )
}
