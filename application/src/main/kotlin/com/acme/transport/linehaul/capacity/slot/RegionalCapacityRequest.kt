package com.acme.transport.linehaul.capacity.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional capacity request is in its life cycle. */
enum class RegionalCapacityRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A regional capacity request as the capacity team keeps it. */
data class RegionalCapacityRequest(
    var code: String,
    var name: String,
    var amount: Double,
    var version: Int,
    var checksum: String,
    var capacity: Int,
    var status: RegionalCapacityRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCapacityRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCapacityRequestStatus): Boolean {
        when (status) {
            RegionalCapacityRequestStatus.REQUESTED -> return next == RegionalCapacityRequestStatus.GRANTED || next == RegionalCapacityRequestStatus.EXPIRED
            RegionalCapacityRequestStatus.GRANTED -> return next == RegionalCapacityRequestStatus.REVOKED || next == RegionalCapacityRequestStatus.EXPIRED
            RegionalCapacityRequestStatus.REVOKED -> return next == RegionalCapacityRequestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalCapacityRequestStatus) {
        if (!canMoveTo(next)) {
            error("regional capacity request $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"regional capacity request"} $code"

    fun adjustedVolume(): Double = round((version.toDouble() - amount) * 100) / 100

    fun snapshot(): RegionalCapacityRequest {
        return RegionalCapacityRequest(
            code = code,
            name = name,
            amount = amount,
            version = version,
            checksum = checksum,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional capacity request in status requested. */
fun newRegionalCapacityRequest(code: String, name: String): RegionalCapacityRequest {
    return RegionalCapacityRequest(
        code = code,
        name = name,
        amount = 0.0,
        version = 0,
        checksum = "",
        capacity = 0,
        status = RegionalCapacityRequestStatus.REQUESTED,
    )
}
