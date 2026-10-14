package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker entry is in its life cycle. */
enum class BrokerEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A broker entry. Its code is unique within a tenant. */
data class BrokerEntry(
    var code: String,
    var name: String,
    var currency: String,
    var active: Boolean,
    var startsAt: Int,
    var source: String,
    var pallets: Int,
    var ceiling: Double,
    var odometer: Int,
    var status: BrokerEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerEntryStatus): Boolean {
        when (status) {
            BrokerEntryStatus.CREATED -> {
                when (next) {
                    BrokerEntryStatus.VALIDATED, BrokerEntryStatus.PUBLISHED -> return true
                }
                return false
            }
            BrokerEntryStatus.VALIDATED -> return next == BrokerEntryStatus.PUBLISHED || next == BrokerEntryStatus.RETIRED
            BrokerEntryStatus.PUBLISHED -> return next == BrokerEntryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BrokerEntryStatus) {
        if (!canMoveTo(next)) {
            error("broker entry $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netTotal(): Double = round((odometer.toDouble() - ceiling) * 100) / 100

    fun snapshot(): BrokerEntry {
        return BrokerEntry(
            code = code,
            name = name,
            currency = currency,
            active = active,
            startsAt = startsAt,
            source = source,
            pallets = pallets,
            ceiling = ceiling,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a broker entry in status created. */
fun newBrokerEntry(code: String, name: String): BrokerEntry {
    return BrokerEntry(
        code = code,
        name = name,
        currency = "",
        active = false,
        startsAt = 0,
        source = "",
        pallets = 0,
        ceiling = 0.0,
        odometer = 0,
        status = BrokerEntryStatus.CREATED,
    )
}
