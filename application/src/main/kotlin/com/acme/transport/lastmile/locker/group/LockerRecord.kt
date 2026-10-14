package com.acme.transport.lastmile.locker.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker record is in its life cycle. */
enum class LockerRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A locker record as the locker team keeps it. */
data class LockerRecord(
    var code: String,
    var name: String,
    var externalRef: String,
    var discount: Double,
    var etaMinutes: Int,
    var graceDays: Int,
    var vehicleId: String,
    var status: LockerRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerRecordStatus): Boolean {
        when (status) {
            LockerRecordStatus.PROPOSED -> {
                when (next) {
                    LockerRecordStatus.APPROVED, LockerRecordStatus.REJECTED -> return true
                }
                return false
            }
            LockerRecordStatus.APPROVED -> return next == LockerRecordStatus.REJECTED
            LockerRecordStatus.REJECTED -> {
                when (next) {
                    LockerRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerRecordStatus) {
        if (!canMoveTo(next)) {
            error("locker record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveVolume(): Double = round((graceDays.toDouble() + discount) * 100) / 100

    fun snapshot(): LockerRecord {
        return LockerRecord(
            code = code,
            name = name,
            externalRef = externalRef,
            discount = discount,
            etaMinutes = etaMinutes,
            graceDays = graceDays,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a locker record in status proposed. */
fun newLockerRecord(code: String, name: String): LockerRecord {
    return LockerRecord(
        code = code,
        name = name,
        externalRef = "",
        discount = 0.0,
        etaMinutes = 0,
        graceDays = 0,
        vehicleId = "",
        status = LockerRecordStatus.PROPOSED,
    )
}
