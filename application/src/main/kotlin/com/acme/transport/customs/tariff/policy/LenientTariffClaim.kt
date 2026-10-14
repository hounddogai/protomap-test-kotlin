package com.acme.transport.customs.tariff.policy

import com.acme.transport.kit.text.normalize

/** Where a lenient tariff claim is in its life cycle. */
enum class LenientTariffClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a lenient tariff claim and its current status. */
data class LenientTariffClaim(
    var code: String,
    var title: String,
    var lane: String,
    var phone: String,
    var country: String,
    var quantity: Int,
    var status: LenientTariffClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientTariffClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientTariffClaimStatus): Boolean {
        when (status) {
            LenientTariffClaimStatus.CREATED -> return next == LenientTariffClaimStatus.VALIDATED || next == LenientTariffClaimStatus.RETIRED
            LenientTariffClaimStatus.VALIDATED -> {
                when (next) {
                    LenientTariffClaimStatus.PUBLISHED -> return true
                }
                return false
            }
            LenientTariffClaimStatus.PUBLISHED -> return next == LenientTariffClaimStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientTariffClaimStatus) {
        if (!canMoveTo(next)) {
            error("lenient tariff claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient tariff claim"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): LenientTariffClaim {
        return LenientTariffClaim(
            code = code,
            title = title,
            lane = lane,
            phone = phone,
            country = country,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lenient tariff claim in status created. */
fun newLenientTariffClaim(code: String, title: String): LenientTariffClaim {
    return LenientTariffClaim(
        code = code,
        title = title,
        lane = "",
        phone = "",
        country = "",
        quantity = 0,
        status = LenientTariffClaimStatus.CREATED,
    )
}
