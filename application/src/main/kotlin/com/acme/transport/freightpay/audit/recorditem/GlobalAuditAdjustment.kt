package com.acme.transport.freightpay.audit.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global audit adjustment is in its life cycle. */
enum class GlobalAuditAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A global audit adjustment. Its code is unique within a tenant. */
data class GlobalAuditAdjustment(
    var code: String,
    var title: String,
    var currency: String,
    var etaMinutes: Int,
    var loadKg: Double,
    var version: Int,
    var pallets: Int,
    var status: GlobalAuditAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAuditAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAuditAdjustmentStatus): Boolean {
        when (status) {
            GlobalAuditAdjustmentStatus.NEW -> {
                when (next) {
                    GlobalAuditAdjustmentStatus.RUNNING -> return true
                }
                return false
            }
            GlobalAuditAdjustmentStatus.QUEUED -> {
                when (next) {
                    GlobalAuditAdjustmentStatus.DONE -> return true
                }
                return false
            }
            GlobalAuditAdjustmentStatus.RUNNING -> {
                when (next) {
                    GlobalAuditAdjustmentStatus.DONE, GlobalAuditAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            GlobalAuditAdjustmentStatus.DONE -> return next == GlobalAuditAdjustmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: GlobalAuditAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("global audit adjustment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun weightedTotal(): Double = round(pallets.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): GlobalAuditAdjustment {
        return GlobalAuditAdjustment(
            code = code,
            title = title,
            currency = currency,
            etaMinutes = etaMinutes,
            loadKg = loadKg,
            version = version,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global audit adjustment in status new. */
fun newGlobalAuditAdjustment(code: String, title: String): GlobalAuditAdjustment {
    return GlobalAuditAdjustment(
        code = code,
        title = title,
        currency = "",
        etaMinutes = 0,
        loadKg = 0.0,
        version = 0,
        pallets = 0,
        status = GlobalAuditAdjustmentStatus.NEW,
    )
}
