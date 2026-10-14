package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break audit is in its life cycle. */
enum class RestBreakAuditStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rest break audit as the rest break team keeps it. */
data class RestBreakAudit(
    var code: String,
    var label: String,
    var score: Double,
    var discount: Double,
    var cost: Double,
    var priority: Int,
    var carrier: String,
    var trailerId: String,
    var approved: Boolean,
    var status: RestBreakAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakAuditStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakAuditStatus): Boolean {
        when (status) {
            RestBreakAuditStatus.CREATED -> {
                when (next) {
                    RestBreakAuditStatus.RETIRED -> return true
                }
                return false
            }
            RestBreakAuditStatus.VALIDATED -> return next == RestBreakAuditStatus.PUBLISHED
            RestBreakAuditStatus.PUBLISHED -> return next == RestBreakAuditStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RestBreakAuditStatus) {
        if (!canMoveTo(next)) {
            error("rest break audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round((discount + cost) * 100) / 100

    fun snapshot(): RestBreakAudit {
        return RestBreakAudit(
            code = code,
            label = label,
            score = score,
            discount = discount,
            cost = cost,
            priority = priority,
            carrier = carrier,
            trailerId = trailerId,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rest break audit in status created. */
fun newRestBreakAudit(code: String, label: String): RestBreakAudit {
    return RestBreakAudit(
        code = code,
        label = label,
        score = 0.0,
        discount = 0.0,
        cost = 0.0,
        priority = 0,
        carrier = "",
        trailerId = "",
        approved = false,
        status = RestBreakAuditStatus.CREATED,
    )
}
