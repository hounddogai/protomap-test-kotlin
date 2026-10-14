package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit allocation is in its life cycle. */
enum class AuditAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a audit allocation and its current status. */
data class AuditAllocation(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var currency: String,
    var locale: String,
    var graceDays: Int,
    var priority: Int,
    var stopCount: Int,
    var endsAt: Int,
    var ratio: Double,
    var status: AuditAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditAllocationStatus): Boolean {
        when (status) {
            AuditAllocationStatus.DRAFT -> {
                when (next) {
                    AuditAllocationStatus.PENDING, AuditAllocationStatus.ACTIVE -> return true
                }
                return false
            }
            AuditAllocationStatus.PENDING -> return next == AuditAllocationStatus.CLOSED
            AuditAllocationStatus.ACTIVE -> return next == AuditAllocationStatus.CLOSED
            AuditAllocationStatus.SUSPENDED -> {
                when (next) {
                    AuditAllocationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditAllocationStatus) {
        if (!canMoveTo(next)) {
            error("audit allocation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"audit allocation"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedVolume(): Double = round((priority.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): AuditAllocation {
        return AuditAllocation(
            code = code,
            title = title,
            tags = tags,
            currency = currency,
            locale = locale,
            graceDays = graceDays,
            priority = priority,
            stopCount = stopCount,
            endsAt = endsAt,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a audit allocation in status draft. */
fun newAuditAllocation(code: String, title: String): AuditAllocation {
    return AuditAllocation(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        currency = "",
        locale = "",
        graceDays = 0,
        priority = 0,
        stopCount = 0,
        endsAt = 0,
        ratio = 0.0,
        status = AuditAllocationStatus.DRAFT,
    )
}
