package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending service plan rollup records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a pending service plan rollup is in its life cycle. */
enum class PendingServicePlanRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending service plan rollup. Its code is unique within a tenant. */
data class PendingServicePlanRollup(
    var code: String,
    var title: String,
    var limit: Int,
    var shiftCode: String,
    var version: Int,
    var status: PendingServicePlanRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingServicePlanRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingServicePlanRollupStatus): Boolean {
        when (status) {
            PendingServicePlanRollupStatus.PLANNED -> {
                when (next) {
                    PendingServicePlanRollupStatus.SCHEDULED, PendingServicePlanRollupStatus.COMPLETED -> return true
                }
                return false
            }
            PendingServicePlanRollupStatus.SCHEDULED -> {
                when (next) {
                    PendingServicePlanRollupStatus.IN_PROGRESS, PendingServicePlanRollupStatus.CANCELLED -> return true
                }
                return false
            }
            PendingServicePlanRollupStatus.IN_PROGRESS -> return next == PendingServicePlanRollupStatus.COMPLETED || next == PendingServicePlanRollupStatus.CANCELLED
            PendingServicePlanRollupStatus.COMPLETED -> {
                when (next) {
                    PendingServicePlanRollupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingServicePlanRollupStatus) {
        if (!canMoveTo(next)) {
            error("pending service plan rollup $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"pending service plan rollup"} $code"

    fun netValue(): Double = round(limit.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): PendingServicePlanRollup {
        return PendingServicePlanRollup(
            code = code,
            title = title,
            limit = limit,
            shiftCode = shiftCode,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending service plan rollup in status planned. */
fun newPendingServicePlanRollup(code: String, title: String): PendingServicePlanRollup {
    return PendingServicePlanRollup(
        code = code,
        title = title,
        limit = 0,
        shiftCode = "",
        version = 0,
        status = PendingServicePlanRollupStatus.PLANNED,
    )
}
