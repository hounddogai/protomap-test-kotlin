package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional capacity checkpoint is in its life cycle. */
enum class RegionalCapacityCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A regional capacity checkpoint. Its code is unique within a tenant. */
data class RegionalCapacityCheckpoint(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var locale: String,
    var title: String,
    var depot: String,
    var createdAt: Int,
    var region: String,
    var status: RegionalCapacityCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCapacityCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCapacityCheckpointStatus): Boolean {
        when (status) {
            RegionalCapacityCheckpointStatus.PROPOSED -> return next == RegionalCapacityCheckpointStatus.APPROVED || next == RegionalCapacityCheckpointStatus.WITHDRAWN
            RegionalCapacityCheckpointStatus.APPROVED -> {
                when (next) {
                    RegionalCapacityCheckpointStatus.REJECTED, RegionalCapacityCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalCapacityCheckpointStatus.REJECTED -> {
                when (next) {
                    RegionalCapacityCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCapacityCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("regional capacity checkpoint $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round(durationMinutes.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): RegionalCapacityCheckpoint {
        return RegionalCapacityCheckpoint(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            locale = locale,
            title = title,
            depot = depot,
            createdAt = createdAt,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a regional capacity checkpoint in status proposed. */
fun newRegionalCapacityCheckpoint(code: String, name: String): RegionalCapacityCheckpoint {
    return RegionalCapacityCheckpoint(
        code = code,
        name = name,
        durationMinutes = 0,
        locale = "",
        title = "",
        depot = "",
        createdAt = 0,
        region = "",
        status = RegionalCapacityCheckpointStatus.PROPOSED,
    )
}
