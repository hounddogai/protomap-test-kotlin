package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker audit is in its life cycle. */
enum class LockerAuditStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a locker audit and its current status. */
data class LockerAudit(
    var code: String,
    var name: String,
    var weight: Double,
    var driverId: String,
    var channel: String,
    var vehicleId: String,
    var visible: Boolean,
    var updatedAt: Int,
    var cost: Double,
    var status: LockerAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerAuditStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerAuditStatus): Boolean {
        when (status) {
            LockerAuditStatus.CREATED -> {
                when (next) {
                    LockerAuditStatus.VALIDATED, LockerAuditStatus.RETIRED -> return true
                }
                return false
            }
            LockerAuditStatus.VALIDATED -> return next == LockerAuditStatus.PUBLISHED
            LockerAuditStatus.PUBLISHED -> return next == LockerAuditStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LockerAuditStatus) {
        if (!canMoveTo(next)) {
            error("locker audit $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedExposure(): Double = round(updatedAt.toDouble() * weight * 100) / 100

    fun snapshot(): LockerAudit {
        return LockerAudit(
            code = code,
            name = name,
            weight = weight,
            driverId = driverId,
            channel = channel,
            vehicleId = vehicleId,
            visible = visible,
            updatedAt = updatedAt,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a locker audit in status created. */
fun newLockerAudit(code: String, name: String): LockerAudit {
    return LockerAudit(
        code = code,
        name = name,
        weight = 0.0,
        driverId = "",
        channel = "",
        vehicleId = "",
        visible = false,
        updatedAt = 0,
        cost = 0.0,
        status = LockerAuditStatus.CREATED,
    )
}
