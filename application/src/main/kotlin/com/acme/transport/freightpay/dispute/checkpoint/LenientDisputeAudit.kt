package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient dispute audit is in its life cycle. */
enum class LenientDisputeAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lenient dispute audit as the dispute team keeps it. */
data class LenientDisputeAudit(
    var code: String,
    var title: String,
    var timezone: String,
    var fuelLiters: Double,
    var tags: MutableList<String>,
    var score: Double,
    var batchSize: Int,
    var driverId: String,
    var status: LenientDisputeAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDisputeAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDisputeAuditStatus): Boolean {
        when (status) {
            LenientDisputeAuditStatus.OPEN -> {
                when (next) {
                    LenientDisputeAuditStatus.IN_REVIEW, LenientDisputeAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientDisputeAuditStatus.IN_REVIEW -> {
                when (next) {
                    LenientDisputeAuditStatus.RESOLVED -> return true
                }
                return false
            }
            LenientDisputeAuditStatus.RESOLVED -> return next == LenientDisputeAuditStatus.REOPENED || next == LenientDisputeAuditStatus.ARCHIVED
            LenientDisputeAuditStatus.REOPENED -> {
                when (next) {
                    LenientDisputeAuditStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDisputeAuditStatus) {
        if (!canMoveTo(next)) {
            error("lenient dispute audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient dispute audit"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round(batchSize.toDouble() * score * 100) / 100

    fun snapshot(): LenientDisputeAudit {
        return LenientDisputeAudit(
            code = code,
            title = title,
            timezone = timezone,
            fuelLiters = fuelLiters,
            tags = tags,
            score = score,
            batchSize = batchSize,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient dispute audit in status open. */
fun newLenientDisputeAudit(code: String, title: String): LenientDisputeAudit {
    return LenientDisputeAudit(
        code = code,
        title = title,
        timezone = "",
        fuelLiters = 0.0,
        tags = mutableListOf<String>(),
        score = 0.0,
        batchSize = 0,
        driverId = "",
        status = LenientDisputeAuditStatus.OPEN,
    )
}
