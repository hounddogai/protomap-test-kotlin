package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.text.normalize

// The global near miss record records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a global near miss record is in its life cycle. */
enum class GlobalNearMissRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global near miss record. Its code is unique within a tenant. */
data class GlobalNearMissRecord(
    var code: String,
    var name: String,
    var loadKg: Double,
    var vehicleId: String,
    var region: String,
    var status: GlobalNearMissRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalNearMissRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalNearMissRecordStatus): Boolean {
        when (status) {
            GlobalNearMissRecordStatus.REQUESTED -> {
                when (next) {
                    GlobalNearMissRecordStatus.GRANTED -> return true
                }
                return false
            }
            GlobalNearMissRecordStatus.GRANTED -> return next == GlobalNearMissRecordStatus.EXPIRED
            GlobalNearMissRecordStatus.REVOKED -> return next == GlobalNearMissRecordStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalNearMissRecordStatus) {
        if (!canMoveTo(next)) {
            error("global near miss record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = loadKg

    fun snapshot(): GlobalNearMissRecord {
        return GlobalNearMissRecord(
            code = code,
            name = name,
            loadKg = loadKg,
            vehicleId = vehicleId,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global near miss record in status requested. */
fun newGlobalNearMissRecord(code: String, name: String): GlobalNearMissRecord {
    return GlobalNearMissRecord(
        code = code,
        name = name,
        loadKg = 0.0,
        vehicleId = "",
        region = "",
        status = GlobalNearMissRecordStatus.REQUESTED,
    )
}
