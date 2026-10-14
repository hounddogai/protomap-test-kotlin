package com.acme.transport.linehaul.tender.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global tender quota records of linehaul's tender work, and the rules, stores, and calculations around
// them.

/** Where a global tender quota is in its life cycle. */
enum class GlobalTenderQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global tender quota as the tender team keeps it. */
data class GlobalTenderQuota(
    var code: String,
    var label: String,
    var description: String,
    var reviewer: String,
    var carrier: String,
    var floorValue: Double,
    var ceiling: Double,
    var visible: Boolean,
    var attempts: Int,
    var status: GlobalTenderQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTenderQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTenderQuotaStatus): Boolean {
        when (status) {
            GlobalTenderQuotaStatus.OPEN -> {
                when (next) {
                    GlobalTenderQuotaStatus.RESOLVED -> return true
                }
                return false
            }
            GlobalTenderQuotaStatus.IN_REVIEW -> {
                when (next) {
                    GlobalTenderQuotaStatus.REOPENED -> return true
                }
                return false
            }
            GlobalTenderQuotaStatus.RESOLVED -> return next == GlobalTenderQuotaStatus.REOPENED
            GlobalTenderQuotaStatus.REOPENED -> return next == GlobalTenderQuotaStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalTenderQuotaStatus) {
        if (!canMoveTo(next)) {
            error("global tender quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedVolume(): Double = round((ceiling - floorValue) * 100) / 100

    fun snapshot(): GlobalTenderQuota {
        return GlobalTenderQuota(
            code = code,
            label = label,
            description = description,
            reviewer = reviewer,
            carrier = carrier,
            floorValue = floorValue,
            ceiling = ceiling,
            visible = visible,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global tender quota in status open. */
fun newGlobalTenderQuota(code: String, label: String): GlobalTenderQuota {
    return GlobalTenderQuota(
        code = code,
        label = label,
        description = "",
        reviewer = "",
        carrier = "",
        floorValue = 0.0,
        ceiling = 0.0,
        visible = false,
        attempts = 0,
        status = GlobalTenderQuotaStatus.OPEN,
    )
}
