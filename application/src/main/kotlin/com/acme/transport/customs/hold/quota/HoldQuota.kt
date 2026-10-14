package com.acme.transport.customs.hold.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hold quota records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold quota is in its life cycle. */
enum class HoldQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A hold quota. Its code is unique within a tenant. */
data class HoldQuota(
    var code: String,
    var name: String,
    var discount: Double,
    var margin: Double,
    var driverId: String,
    var batchSize: Int,
    var pinned: Boolean,
    var status: HoldQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldQuotaStatus): Boolean {
        when (status) {
            HoldQuotaStatus.IDLE -> return next == HoldQuotaStatus.WARMING
            HoldQuotaStatus.WARMING -> return next == HoldQuotaStatus.READY || next == HoldQuotaStatus.DRAINING
            HoldQuotaStatus.READY -> {
                when (next) {
                    HoldQuotaStatus.DRAINING, HoldQuotaStatus.STOPPED -> return true
                }
                return false
            }
            HoldQuotaStatus.DRAINING -> return next == HoldQuotaStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: HoldQuotaStatus) {
        if (!canMoveTo(next)) {
            error("hold quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round(discount * batchSize.toDouble() * 100) / 100

    fun snapshot(): HoldQuota {
        return HoldQuota(
            code = code,
            name = name,
            discount = discount,
            margin = margin,
            driverId = driverId,
            batchSize = batchSize,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hold quota in status idle. */
fun newHoldQuota(code: String, name: String): HoldQuota {
    return HoldQuota(
        code = code,
        name = name,
        discount = 0.0,
        margin = 0.0,
        driverId = "",
        batchSize = 0,
        pinned = false,
        status = HoldQuotaStatus.IDLE,
    )
}
