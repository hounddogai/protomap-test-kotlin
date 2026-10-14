package com.acme.transport.network.transittime.timebatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time audit is in its life cycle. */
enum class TransitTimeAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A transit time audit. Its code is unique within a tenant. */
data class TransitTimeAudit(
    var code: String,
    var name: String,
    var budget: Double,
    var updatedAt: Int,
    var depot: String,
    var driverId: String,
    var odometer: Int,
    var status: TransitTimeAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeAuditStatus): Boolean {
        when (status) {
            TransitTimeAuditStatus.IDLE -> {
                when (next) {
                    TransitTimeAuditStatus.WARMING, TransitTimeAuditStatus.READY -> return true
                }
                return false
            }
            TransitTimeAuditStatus.WARMING -> return next == TransitTimeAuditStatus.DRAINING || next == TransitTimeAuditStatus.STOPPED
            TransitTimeAuditStatus.READY -> {
                when (next) {
                    TransitTimeAuditStatus.DRAINING, TransitTimeAuditStatus.STOPPED -> return true
                }
                return false
            }
            TransitTimeAuditStatus.DRAINING -> {
                when (next) {
                    TransitTimeAuditStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeAuditStatus) {
        if (!canMoveTo(next)) {
            error("transit time audit $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round((budget + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): TransitTimeAudit {
        return TransitTimeAudit(
            code = code,
            name = name,
            budget = budget,
            updatedAt = updatedAt,
            depot = depot,
            driverId = driverId,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a transit time audit in status idle. */
fun newTransitTimeAudit(code: String, name: String): TransitTimeAudit {
    return TransitTimeAudit(
        code = code,
        name = name,
        budget = 0.0,
        updatedAt = 0,
        depot = "",
        driverId = "",
        odometer = 0,
        status = TransitTimeAuditStatus.IDLE,
    )
}
