package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual service plan quota records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a manual service plan quota is in its life cycle. */
enum class ManualServicePlanQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One manual service plan quota, with what the planners need to act on it. */
data class ManualServicePlanQuota(
    var code: String,
    var label: String,
    var checksum: String,
    var tags: MutableList<String>,
    var expiresAt: Int,
    var region: String,
    var title: String,
    var threshold: Double,
    var status: ManualServicePlanQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualServicePlanQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualServicePlanQuotaStatus): Boolean {
        when (status) {
            ManualServicePlanQuotaStatus.OPEN -> {
                when (next) {
                    ManualServicePlanQuotaStatus.IN_REVIEW, ManualServicePlanQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualServicePlanQuotaStatus.IN_REVIEW -> return next == ManualServicePlanQuotaStatus.RESOLVED
            ManualServicePlanQuotaStatus.RESOLVED -> return next == ManualServicePlanQuotaStatus.ARCHIVED
            ManualServicePlanQuotaStatus.REOPENED -> {
                when (next) {
                    ManualServicePlanQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualServicePlanQuotaStatus) {
        if (!canMoveTo(next)) {
            error("manual service plan quota $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"manual service plan quota"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedTotal(): Double = round(threshold * expiresAt.toDouble() * 100) / 100

    fun snapshot(): ManualServicePlanQuota {
        return ManualServicePlanQuota(
            code = code,
            label = label,
            checksum = checksum,
            tags = tags,
            expiresAt = expiresAt,
            region = region,
            title = title,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a manual service plan quota in status open. */
fun newManualServicePlanQuota(code: String, label: String): ManualServicePlanQuota {
    return ManualServicePlanQuota(
        code = code,
        label = label,
        checksum = "",
        tags = mutableListOf<String>(),
        expiresAt = 0,
        region = "",
        title = "",
        threshold = 0.0,
        status = ManualServicePlanQuotaStatus.OPEN,
    )
}
