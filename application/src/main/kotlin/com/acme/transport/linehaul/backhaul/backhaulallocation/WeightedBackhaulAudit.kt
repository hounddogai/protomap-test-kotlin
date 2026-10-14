package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.text.normalize

/** Where a weighted backhaul audit is in its life cycle. */
enum class WeightedBackhaulAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One weighted backhaul audit, with what the planners need to act on it. */
data class WeightedBackhaulAudit(
    var code: String,
    var name: String,
    var dock: String,
    var region: String,
    var externalRef: String,
    var amount: Double,
    var status: WeightedBackhaulAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedBackhaulAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedBackhaulAuditStatus): Boolean {
        when (status) {
            WeightedBackhaulAuditStatus.DRAFT -> {
                when (next) {
                    WeightedBackhaulAuditStatus.PENDING, WeightedBackhaulAuditStatus.ACTIVE -> return true
                }
                return false
            }
            WeightedBackhaulAuditStatus.PENDING -> {
                when (next) {
                    WeightedBackhaulAuditStatus.ACTIVE, WeightedBackhaulAuditStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedBackhaulAuditStatus.ACTIVE -> return next == WeightedBackhaulAuditStatus.SUSPENDED || next == WeightedBackhaulAuditStatus.CLOSED
            WeightedBackhaulAuditStatus.SUSPENDED -> return next == WeightedBackhaulAuditStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: WeightedBackhaulAuditStatus) {
        if (!canMoveTo(next)) {
            error("weighted backhaul audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted backhaul audit"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): WeightedBackhaulAudit {
        return WeightedBackhaulAudit(
            code = code,
            name = name,
            dock = dock,
            region = region,
            externalRef = externalRef,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a weighted backhaul audit in status draft. */
fun newWeightedBackhaulAudit(code: String, name: String): WeightedBackhaulAudit {
    return WeightedBackhaulAudit(
        code = code,
        name = name,
        dock = "",
        region = "",
        externalRef = "",
        amount = 0.0,
        status = WeightedBackhaulAuditStatus.DRAFT,
    )
}
