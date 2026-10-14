package com.acme.transport.network.hub.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted hub audit is in its life cycle. */
enum class WeightedHubAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted hub audit. Its code is unique within a tenant. */
data class WeightedHubAudit(
    var code: String,
    var title: String,
    var name: String,
    var locale: String,
    var quantity: Int,
    var endsAt: Int,
    var carrier: String,
    var capacity: Int,
    var pinned: Boolean,
    var status: WeightedHubAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedHubAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedHubAuditStatus): Boolean {
        when (status) {
            WeightedHubAuditStatus.OPEN -> {
                when (next) {
                    WeightedHubAuditStatus.IN_REVIEW -> return true
                }
                return false
            }
            WeightedHubAuditStatus.IN_REVIEW -> return next == WeightedHubAuditStatus.RESOLVED || next == WeightedHubAuditStatus.ARCHIVED
            WeightedHubAuditStatus.RESOLVED -> {
                when (next) {
                    WeightedHubAuditStatus.REOPENED -> return true
                }
                return false
            }
            WeightedHubAuditStatus.REOPENED -> {
                when (next) {
                    WeightedHubAuditStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedHubAuditStatus) {
        if (!canMoveTo(next)) {
            error("weighted hub audit $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedLoad(): Double = round((endsAt.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): WeightedHubAudit {
        return WeightedHubAudit(
            code = code,
            title = title,
            name = name,
            locale = locale,
            quantity = quantity,
            endsAt = endsAt,
            carrier = carrier,
            capacity = capacity,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted hub audit in status open. */
fun newWeightedHubAudit(code: String, title: String): WeightedHubAudit {
    return WeightedHubAudit(
        code = code,
        title = title,
        name = "",
        locale = "",
        quantity = 0,
        endsAt = 0,
        carrier = "",
        capacity = 0,
        pinned = false,
        status = WeightedHubAuditStatus.OPEN,
    )
}
