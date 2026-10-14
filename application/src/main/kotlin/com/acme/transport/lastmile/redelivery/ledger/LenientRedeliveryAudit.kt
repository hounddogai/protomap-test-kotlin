package com.acme.transport.lastmile.redelivery.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient redelivery audit is in its life cycle. */
enum class LenientRedeliveryAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lenient redelivery audit as the redelivery team keeps it. */
data class LenientRedeliveryAudit(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var tags: MutableList<String>,
    var odometer: Int,
    var approved: Boolean,
    var minimum: Double,
    var ceiling: Double,
    var margin: Double,
    var status: LenientRedeliveryAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRedeliveryAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRedeliveryAuditStatus): Boolean {
        when (status) {
            LenientRedeliveryAuditStatus.IDLE -> return next == LenientRedeliveryAuditStatus.READY
            LenientRedeliveryAuditStatus.WARMING -> {
                when (next) {
                    LenientRedeliveryAuditStatus.STOPPED -> return true
                }
                return false
            }
            LenientRedeliveryAuditStatus.READY -> {
                when (next) {
                    LenientRedeliveryAuditStatus.DRAINING -> return true
                }
                return false
            }
            LenientRedeliveryAuditStatus.DRAINING -> {
                when (next) {
                    LenientRedeliveryAuditStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientRedeliveryAuditStatus) {
        if (!canMoveTo(next)) {
            error("lenient redelivery audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedTotal(): Double = round(margin * distanceKm * 100) / 100

    fun snapshot(): LenientRedeliveryAudit {
        return LenientRedeliveryAudit(
            code = code,
            label = label,
            distanceKm = distanceKm,
            tags = tags,
            odometer = odometer,
            approved = approved,
            minimum = minimum,
            ceiling = ceiling,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient redelivery audit in status idle. */
fun newLenientRedeliveryAudit(code: String, label: String): LenientRedeliveryAudit {
    return LenientRedeliveryAudit(
        code = code,
        label = label,
        distanceKm = 0.0,
        tags = mutableListOf<String>(),
        odometer = 0,
        approved = false,
        minimum = 0.0,
        ceiling = 0.0,
        margin = 0.0,
        status = LenientRedeliveryAuditStatus.IDLE,
    )
}
