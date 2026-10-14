package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive ev charge ledger is in its life cycle. */
enum class AdaptiveEvChargeLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One adaptive ev charge ledger, with what the planners need to act on it. */
data class AdaptiveEvChargeLedger(
    var code: String,
    var title: String,
    var channel: String,
    var vehicleId: String,
    var segment: String,
    var graceDays: Int,
    var capacity: Int,
    var currency: String,
    var locale: String,
    var loadKg: Double,
    var lane: String,
    var status: AdaptiveEvChargeLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveEvChargeLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveEvChargeLedgerStatus): Boolean {
        when (status) {
            AdaptiveEvChargeLedgerStatus.REQUESTED -> {
                when (next) {
                    AdaptiveEvChargeLedgerStatus.GRANTED, AdaptiveEvChargeLedgerStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveEvChargeLedgerStatus.GRANTED -> return next == AdaptiveEvChargeLedgerStatus.EXPIRED
            AdaptiveEvChargeLedgerStatus.REVOKED -> {
                when (next) {
                    AdaptiveEvChargeLedgerStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveEvChargeLedgerStatus) {
        if (!canMoveTo(next)) {
            error("adaptive ev charge ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netLoad(): Double = round(capacity.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): AdaptiveEvChargeLedger {
        return AdaptiveEvChargeLedger(
            code = code,
            title = title,
            channel = channel,
            vehicleId = vehicleId,
            segment = segment,
            graceDays = graceDays,
            capacity = capacity,
            currency = currency,
            locale = locale,
            loadKg = loadKg,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive ev charge ledger in status requested. */
fun newAdaptiveEvChargeLedger(code: String, title: String): AdaptiveEvChargeLedger {
    return AdaptiveEvChargeLedger(
        code = code,
        title = title,
        channel = "",
        vehicleId = "",
        segment = "",
        graceDays = 0,
        capacity = 0,
        currency = "",
        locale = "",
        loadKg = 0.0,
        lane = "",
        status = AdaptiveEvChargeLedgerStatus.REQUESTED,
    )
}
