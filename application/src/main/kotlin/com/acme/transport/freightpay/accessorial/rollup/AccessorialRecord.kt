package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial record is in its life cycle. */
enum class AccessorialRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A accessorial record as the accessorial team keeps it. */
data class AccessorialRecord(
    var code: String,
    var label: String,
    var unit: String,
    var timezone: String,
    var fuelLiters: Double,
    var discount: Double,
    var createdAt: Int,
    var status: AccessorialRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialRecordStatus): Boolean {
        when (status) {
            AccessorialRecordStatus.REQUESTED -> return next == AccessorialRecordStatus.REVOKED
            AccessorialRecordStatus.GRANTED -> return next == AccessorialRecordStatus.REVOKED
            AccessorialRecordStatus.REVOKED -> {
                when (next) {
                    AccessorialRecordStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialRecordStatus) {
        if (!canMoveTo(next)) {
            error("accessorial record $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedValue(): Double = round((createdAt.toDouble() - discount) * 100) / 100

    fun snapshot(): AccessorialRecord {
        return AccessorialRecord(
            code = code,
            label = label,
            unit = unit,
            timezone = timezone,
            fuelLiters = fuelLiters,
            discount = discount,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a accessorial record in status requested. */
fun newAccessorialRecord(code: String, label: String): AccessorialRecord {
    return AccessorialRecord(
        code = code,
        label = label,
        unit = "",
        timezone = "",
        fuelLiters = 0.0,
        discount = 0.0,
        createdAt = 0,
        status = AccessorialRecordStatus.REQUESTED,
    )
}
