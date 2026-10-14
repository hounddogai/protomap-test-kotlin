package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.text.normalize

/** Where a adaptive trailer agreement is in its life cycle. */
enum class AdaptiveTrailerAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive trailer agreement as the trailer team keeps it. */
data class AdaptiveTrailerAgreement(
    var code: String,
    var title: String,
    var currency: String,
    var tags: MutableList<String>,
    var vehicleId: String,
    var quantity: Int,
    var status: AdaptiveTrailerAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTrailerAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTrailerAgreementStatus): Boolean {
        when (status) {
            AdaptiveTrailerAgreementStatus.PLANNED -> return next == AdaptiveTrailerAgreementStatus.COMPLETED
            AdaptiveTrailerAgreementStatus.SCHEDULED -> return next == AdaptiveTrailerAgreementStatus.CANCELLED
            AdaptiveTrailerAgreementStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveTrailerAgreementStatus.COMPLETED, AdaptiveTrailerAgreementStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveTrailerAgreementStatus.COMPLETED -> {
                when (next) {
                    AdaptiveTrailerAgreementStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveTrailerAgreementStatus) {
        if (!canMoveTo(next)) {
            error("adaptive trailer agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): AdaptiveTrailerAgreement {
        return AdaptiveTrailerAgreement(
            code = code,
            title = title,
            currency = currency,
            tags = tags,
            vehicleId = vehicleId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive trailer agreement in status planned. */
fun newAdaptiveTrailerAgreement(code: String, title: String): AdaptiveTrailerAgreement {
    return AdaptiveTrailerAgreement(
        code = code,
        title = title,
        currency = "",
        tags = mutableListOf<String>(),
        vehicleId = "",
        quantity = 0,
        status = AdaptiveTrailerAgreementStatus.PLANNED,
    )
}
