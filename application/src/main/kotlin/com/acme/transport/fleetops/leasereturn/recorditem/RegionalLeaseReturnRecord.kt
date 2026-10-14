package com.acme.transport.fleetops.leasereturn.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional lease return record records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a regional lease return record is in its life cycle. */
enum class RegionalLeaseReturnRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a regional lease return record and its current status. */
data class RegionalLeaseReturnRecord(
    var code: String,
    var name: String,
    var routeCode: String,
    var source: String,
    var ceiling: Double,
    var minimum: Double,
    var unit: String,
    var dock: String,
    var notes: String,
    var status: RegionalLeaseReturnRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalLeaseReturnRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalLeaseReturnRecordStatus): Boolean {
        when (status) {
            RegionalLeaseReturnRecordStatus.REQUESTED -> {
                when (next) {
                    RegionalLeaseReturnRecordStatus.GRANTED -> return true
                }
                return false
            }
            RegionalLeaseReturnRecordStatus.GRANTED -> return next == RegionalLeaseReturnRecordStatus.EXPIRED
            RegionalLeaseReturnRecordStatus.REVOKED -> {
                when (next) {
                    RegionalLeaseReturnRecordStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalLeaseReturnRecordStatus) {
        if (!canMoveTo(next)) {
            error("regional lease return record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedTotal(): Double = round((ceiling + minimum) * 100) / 100

    fun snapshot(): RegionalLeaseReturnRecord {
        return RegionalLeaseReturnRecord(
            code = code,
            name = name,
            routeCode = routeCode,
            source = source,
            ceiling = ceiling,
            minimum = minimum,
            unit = unit,
            dock = dock,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional lease return record in status requested. */
fun newRegionalLeaseReturnRecord(code: String, name: String): RegionalLeaseReturnRecord {
    return RegionalLeaseReturnRecord(
        code = code,
        name = name,
        routeCode = "",
        source = "",
        ceiling = 0.0,
        minimum = 0.0,
        unit = "",
        dock = "",
        notes = "",
        status = RegionalLeaseReturnRecordStatus.REQUESTED,
    )
}
