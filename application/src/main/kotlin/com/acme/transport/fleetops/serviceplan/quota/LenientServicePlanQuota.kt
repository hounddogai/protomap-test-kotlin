package com.acme.transport.fleetops.serviceplan.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient service plan quota records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a lenient service plan quota is in its life cycle. */
enum class LenientServicePlanQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a lenient service plan quota and its current status. */
data class LenientServicePlanQuota(
    var code: String,
    var label: String,
    var batchSize: Int,
    var discount: Double,
    var tags: MutableList<String>,
    var status: LenientServicePlanQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientServicePlanQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientServicePlanQuotaStatus): Boolean {
        when (status) {
            LenientServicePlanQuotaStatus.OPEN -> return next == LenientServicePlanQuotaStatus.REOPENED
            LenientServicePlanQuotaStatus.IN_REVIEW -> {
                when (next) {
                    LenientServicePlanQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientServicePlanQuotaStatus.RESOLVED -> {
                when (next) {
                    LenientServicePlanQuotaStatus.REOPENED, LenientServicePlanQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientServicePlanQuotaStatus.REOPENED -> return next == LenientServicePlanQuotaStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LenientServicePlanQuotaStatus) {
        if (!canMoveTo(next)) {
            error("lenient service plan quota $code cannot move from $status to $next")
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

    fun projectedValue(): Double = round((batchSize.toDouble() - discount) * 100) / 100

    fun snapshot(): LenientServicePlanQuota {
        return LenientServicePlanQuota(
            code = code,
            label = label,
            batchSize = batchSize,
            discount = discount,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient service plan quota in status open. */
fun newLenientServicePlanQuota(code: String, label: String): LenientServicePlanQuota {
    return LenientServicePlanQuota(
        code = code,
        label = label,
        batchSize = 0,
        discount = 0.0,
        tags = mutableListOf<String>(),
        status = LenientServicePlanQuotaStatus.OPEN,
    )
}
