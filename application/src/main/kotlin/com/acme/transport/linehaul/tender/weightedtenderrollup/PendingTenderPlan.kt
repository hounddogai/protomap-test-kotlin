package com.acme.transport.linehaul.tender.weightedtenderrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending tender plan is in its life cycle. */
enum class PendingTenderPlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending tender plan as the tender team keeps it. */
data class PendingTenderPlan(
    var code: String,
    var label: String,
    var rate: Double,
    var unit: String,
    var version: Int,
    var pinned: Boolean,
    var ratio: Double,
    var revision: Int,
    var ceiling: Double,
    var pallets: Int,
    var status: PendingTenderPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTenderPlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTenderPlanStatus): Boolean {
        when (status) {
            PendingTenderPlanStatus.DRAFT -> return next == PendingTenderPlanStatus.SUSPENDED
            PendingTenderPlanStatus.PENDING -> {
                when (next) {
                    PendingTenderPlanStatus.SUSPENDED, PendingTenderPlanStatus.CLOSED -> return true
                }
                return false
            }
            PendingTenderPlanStatus.ACTIVE -> {
                when (next) {
                    PendingTenderPlanStatus.CLOSED -> return true
                }
                return false
            }
            PendingTenderPlanStatus.SUSPENDED -> {
                when (next) {
                    PendingTenderPlanStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingTenderPlanStatus) {
        if (!canMoveTo(next)) {
            error("pending tender plan $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveTotal(): Double = round((pallets.toDouble() + ratio) * 100) / 100

    fun snapshot(): PendingTenderPlan {
        return PendingTenderPlan(
            code = code,
            label = label,
            rate = rate,
            unit = unit,
            version = version,
            pinned = pinned,
            ratio = ratio,
            revision = revision,
            ceiling = ceiling,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending tender plan in status draft. */
fun newPendingTenderPlan(code: String, label: String): PendingTenderPlan {
    return PendingTenderPlan(
        code = code,
        label = label,
        rate = 0.0,
        unit = "",
        version = 0,
        pinned = false,
        ratio = 0.0,
        revision = 0,
        ceiling = 0.0,
        pallets = 0,
        status = PendingTenderPlanStatus.DRAFT,
    )
}
