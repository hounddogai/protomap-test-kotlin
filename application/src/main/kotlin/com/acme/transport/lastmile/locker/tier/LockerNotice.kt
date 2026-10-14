package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker notice is in its life cycle. */
enum class LockerNoticeStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A locker notice as the locker team keeps it. */
data class LockerNotice(
    var code: String,
    var name: String,
    var cost: Double,
    var description: String,
    var maximum: Double,
    var discount: Double,
    var source: String,
    var vehicleId: String,
    var category: String,
    var externalRef: String,
    var stopCount: Int,
    var status: LockerNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerNoticeStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerNoticeStatus): Boolean {
        when (status) {
            LockerNoticeStatus.REQUESTED -> return next == LockerNoticeStatus.REVOKED || next == LockerNoticeStatus.EXPIRED
            LockerNoticeStatus.GRANTED -> {
                when (next) {
                    LockerNoticeStatus.REVOKED, LockerNoticeStatus.EXPIRED -> return true
                }
                return false
            }
            LockerNoticeStatus.REVOKED -> {
                when (next) {
                    LockerNoticeStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerNoticeStatus) {
        if (!canMoveTo(next)) {
            error("locker notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"locker notice"} $code"

    fun adjustedExposure(): Double = round(maximum * cost * 100) / 100

    fun snapshot(): LockerNotice {
        return LockerNotice(
            code = code,
            name = name,
            cost = cost,
            description = description,
            maximum = maximum,
            discount = discount,
            source = source,
            vehicleId = vehicleId,
            category = category,
            externalRef = externalRef,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a locker notice in status requested. */
fun newLockerNotice(code: String, name: String): LockerNotice {
    return LockerNotice(
        code = code,
        name = name,
        cost = 0.0,
        description = "",
        maximum = 0.0,
        discount = 0.0,
        source = "",
        vehicleId = "",
        category = "",
        externalRef = "",
        stopCount = 0,
        status = LockerNoticeStatus.REQUESTED,
    )
}
