package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling dispute adjustment is in its life cycle. */
enum class RollingDisputeAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling dispute adjustment. Its code is unique within a tenant. */
data class RollingDisputeAdjustment(
    var code: String,
    var name: String,
    var cost: Double,
    var graceDays: Int,
    var createdAt: Int,
    var channel: String,
    var reviewer: String,
    var attributes: MutableMap<String, String>,
    var status: RollingDisputeAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDisputeAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDisputeAdjustmentStatus): Boolean {
        when (status) {
            RollingDisputeAdjustmentStatus.CREATED -> {
                when (next) {
                    RollingDisputeAdjustmentStatus.VALIDATED -> return true
                }
                return false
            }
            RollingDisputeAdjustmentStatus.VALIDATED -> return next == RollingDisputeAdjustmentStatus.PUBLISHED || next == RollingDisputeAdjustmentStatus.RETIRED
            RollingDisputeAdjustmentStatus.PUBLISHED -> {
                when (next) {
                    RollingDisputeAdjustmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDisputeAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("rolling dispute adjustment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rolling dispute adjustment"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netExposure(): Double = round((graceDays.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): RollingDisputeAdjustment {
        return RollingDisputeAdjustment(
            code = code,
            name = name,
            cost = cost,
            graceDays = graceDays,
            createdAt = createdAt,
            channel = channel,
            reviewer = reviewer,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rolling dispute adjustment in status created. */
fun newRollingDisputeAdjustment(code: String, name: String): RollingDisputeAdjustment {
    return RollingDisputeAdjustment(
        code = code,
        name = name,
        cost = 0.0,
        graceDays = 0,
        createdAt = 0,
        channel = "",
        reviewer = "",
        attributes = mutableMapOf<String, String>(),
        status = RollingDisputeAdjustmentStatus.CREATED,
    )
}
