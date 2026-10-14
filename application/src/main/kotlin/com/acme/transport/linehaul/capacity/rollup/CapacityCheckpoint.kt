package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity checkpoint is in its life cycle. */
enum class CapacityCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capacity checkpoint. Its code is unique within a tenant. */
data class CapacityCheckpoint(
    var code: String,
    var title: String,
    var country: String,
    var fuelLiters: Double,
    var discount: Double,
    var status: CapacityCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityCheckpointStatus): Boolean {
        when (status) {
            CapacityCheckpointStatus.PROPOSED -> {
                when (next) {
                    CapacityCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
            CapacityCheckpointStatus.APPROVED -> {
                when (next) {
                    CapacityCheckpointStatus.REJECTED, CapacityCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
            CapacityCheckpointStatus.REJECTED -> return next == CapacityCheckpointStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CapacityCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("capacity checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedTotal(): Double = round((fuelLiters + discount) * 100) / 100

    fun snapshot(): CapacityCheckpoint {
        return CapacityCheckpoint(
            code = code,
            title = title,
            country = country,
            fuelLiters = fuelLiters,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capacity checkpoint in status proposed. */
fun newCapacityCheckpoint(code: String, title: String): CapacityCheckpoint {
    return CapacityCheckpoint(
        code = code,
        title = title,
        country = "",
        fuelLiters = 0.0,
        discount = 0.0,
        status = CapacityCheckpointStatus.PROPOSED,
    )
}
