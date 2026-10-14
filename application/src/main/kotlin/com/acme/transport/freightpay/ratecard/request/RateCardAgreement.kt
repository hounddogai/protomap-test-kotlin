package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card agreement is in its life cycle. */
enum class RateCardAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rate card agreement as the rate card team keeps it. */
data class RateCardAgreement(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var loadKg: Double,
    var version: Int,
    var active: Boolean,
    var ownerId: String,
    var fuelLiters: Double,
    var carrier: String,
    var status: RateCardAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardAgreementStatus): Boolean {
        when (status) {
            RateCardAgreementStatus.IDLE -> return next == RateCardAgreementStatus.DRAINING
            RateCardAgreementStatus.WARMING -> {
                when (next) {
                    RateCardAgreementStatus.STOPPED -> return true
                }
                return false
            }
            RateCardAgreementStatus.READY -> return next == RateCardAgreementStatus.DRAINING || next == RateCardAgreementStatus.STOPPED
            RateCardAgreementStatus.DRAINING -> return next == RateCardAgreementStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RateCardAgreementStatus) {
        if (!canMoveTo(next)) {
            error("rate card agreement $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round(version.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): RateCardAgreement {
        return RateCardAgreement(
            code = code,
            name = name,
            tags = tags,
            loadKg = loadKg,
            version = version,
            active = active,
            ownerId = ownerId,
            fuelLiters = fuelLiters,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rate card agreement in status idle. */
fun newRateCardAgreement(code: String, name: String): RateCardAgreement {
    return RateCardAgreement(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        loadKg = 0.0,
        version = 0,
        active = false,
        ownerId = "",
        fuelLiters = 0.0,
        carrier = "",
        status = RateCardAgreementStatus.IDLE,
    )
}
