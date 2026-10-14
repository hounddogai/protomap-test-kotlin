package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.text.normalize

// The tiered service area record records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a tiered service area record is in its life cycle. */
enum class TieredServiceAreaRecordStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered service area record as the service area team keeps it. */
data class TieredServiceAreaRecord(
    var code: String,
    var name: String,
    var unit: String,
    var title: String,
    var vehicleId: String,
    var country: String,
    var phone: String,
    var currency: String,
    var priority: Int,
    var status: TieredServiceAreaRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredServiceAreaRecordStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredServiceAreaRecordStatus): Boolean {
        when (status) {
            TieredServiceAreaRecordStatus.OPEN -> {
                when (next) {
                    TieredServiceAreaRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredServiceAreaRecordStatus.IN_REVIEW -> return next == TieredServiceAreaRecordStatus.REOPENED || next == TieredServiceAreaRecordStatus.ARCHIVED
            TieredServiceAreaRecordStatus.RESOLVED -> return next == TieredServiceAreaRecordStatus.ARCHIVED
            TieredServiceAreaRecordStatus.REOPENED -> return next == TieredServiceAreaRecordStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredServiceAreaRecordStatus) {
        if (!canMoveTo(next)) {
            error("tiered service area record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): TieredServiceAreaRecord {
        return TieredServiceAreaRecord(
            code = code,
            name = name,
            unit = unit,
            title = title,
            vehicleId = vehicleId,
            country = country,
            phone = phone,
            currency = currency,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tiered service area record in status open. */
fun newTieredServiceAreaRecord(code: String, name: String): TieredServiceAreaRecord {
    return TieredServiceAreaRecord(
        code = code,
        name = name,
        unit = "",
        title = "",
        vehicleId = "",
        country = "",
        phone = "",
        currency = "",
        priority = 0,
        status = TieredServiceAreaRecordStatus.OPEN,
    )
}
