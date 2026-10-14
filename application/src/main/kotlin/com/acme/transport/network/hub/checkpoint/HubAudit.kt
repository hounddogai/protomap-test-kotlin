package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub audit is in its life cycle. */
enum class HubAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A hub audit. Its code is unique within a tenant. */
data class HubAudit(
    var code: String,
    var name: String,
    var minimum: Double,
    var weight: Double,
    var timezone: String,
    var createdAt: Int,
    var status: HubAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubAuditStatus): Boolean {
        when (status) {
            HubAuditStatus.PLANNED -> return next == HubAuditStatus.IN_PROGRESS
            HubAuditStatus.SCHEDULED -> {
                when (next) {
                    HubAuditStatus.IN_PROGRESS -> return true
                }
                return false
            }
            HubAuditStatus.IN_PROGRESS -> {
                when (next) {
                    HubAuditStatus.CANCELLED -> return true
                }
                return false
            }
            HubAuditStatus.COMPLETED -> return next == HubAuditStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: HubAuditStatus) {
        if (!canMoveTo(next)) {
            error("hub audit $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round((createdAt.toDouble() - minimum) * 100) / 100

    fun snapshot(): HubAudit {
        return HubAudit(
            code = code,
            name = name,
            minimum = minimum,
            weight = weight,
            timezone = timezone,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hub audit in status planned. */
fun newHubAudit(code: String, name: String): HubAudit {
    return HubAudit(
        code = code,
        name = name,
        minimum = 0.0,
        weight = 0.0,
        timezone = "",
        createdAt = 0,
        status = HubAuditStatus.PLANNED,
    )
}
