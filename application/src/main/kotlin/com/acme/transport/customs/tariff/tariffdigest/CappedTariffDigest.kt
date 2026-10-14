package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped tariff digest records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a capped tariff digest is in its life cycle. */
enum class CappedTariffDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One capped tariff digest, with what the planners need to act on it. */
data class CappedTariffDigest(
    var code: String,
    var title: String,
    var lane: String,
    var pallets: Int,
    var minimum: Double,
    var discount: Double,
    var amount: Double,
    var category: String,
    var status: CappedTariffDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTariffDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTariffDigestStatus): Boolean {
        when (status) {
            CappedTariffDigestStatus.IDLE -> {
                when (next) {
                    CappedTariffDigestStatus.DRAINING -> return true
                }
                return false
            }
            CappedTariffDigestStatus.WARMING -> return next == CappedTariffDigestStatus.DRAINING
            CappedTariffDigestStatus.READY -> return next == CappedTariffDigestStatus.STOPPED
            CappedTariffDigestStatus.DRAINING -> return next == CappedTariffDigestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CappedTariffDigestStatus) {
        if (!canMoveTo(next)) {
            error("capped tariff digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped tariff digest"} $code"

    fun weightedValue(): Double = round((amount + pallets.toDouble()) * 100) / 100

    fun snapshot(): CappedTariffDigest {
        return CappedTariffDigest(
            code = code,
            title = title,
            lane = lane,
            pallets = pallets,
            minimum = minimum,
            discount = discount,
            amount = amount,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capped tariff digest in status idle. */
fun newCappedTariffDigest(code: String, title: String): CappedTariffDigest {
    return CappedTariffDigest(
        code = code,
        title = title,
        lane = "",
        pallets = 0,
        minimum = 0.0,
        discount = 0.0,
        amount = 0.0,
        category = "",
        status = CappedTariffDigestStatus.IDLE,
    )
}
