package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The violation quota records of safety's violation work, and the rules, stores, and calculations around
// them.

/** Where a violation quota is in its life cycle. */
enum class ViolationQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A violation quota. Its code is unique within a tenant. */
data class ViolationQuota(
    var code: String,
    var title: String,
    var ratio: Double,
    var limit: Int,
    var priority: Int,
    var segment: String,
    var status: ViolationQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationQuotaStatus): Boolean {
        when (status) {
            ViolationQuotaStatus.IDLE -> {
                when (next) {
                    ViolationQuotaStatus.STOPPED -> return true
                }
                return false
            }
            ViolationQuotaStatus.WARMING -> return next == ViolationQuotaStatus.DRAINING || next == ViolationQuotaStatus.STOPPED
            ViolationQuotaStatus.READY -> return next == ViolationQuotaStatus.DRAINING
            ViolationQuotaStatus.DRAINING -> return next == ViolationQuotaStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ViolationQuotaStatus) {
        if (!canMoveTo(next)) {
            error("violation quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"violation quota"} $code"

    fun adjustedExposure(): Double = round((limit.toDouble() - ratio) * 100) / 100

    fun snapshot(): ViolationQuota {
        return ViolationQuota(
            code = code,
            title = title,
            ratio = ratio,
            limit = limit,
            priority = priority,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a violation quota in status idle. */
fun newViolationQuota(code: String, title: String): ViolationQuota {
    return ViolationQuota(
        code = code,
        title = title,
        ratio = 0.0,
        limit = 0,
        priority = 0,
        segment = "",
        status = ViolationQuotaStatus.IDLE,
    )
}
