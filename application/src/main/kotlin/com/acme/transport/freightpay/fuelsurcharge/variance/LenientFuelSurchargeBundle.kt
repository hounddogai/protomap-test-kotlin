package com.acme.transport.freightpay.fuelsurcharge.variance

import com.acme.transport.kit.text.normalize

/** Where a lenient fuel surcharge bundle is in its life cycle. */
enum class LenientFuelSurchargeBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One lenient fuel surcharge bundle, with what the planners need to act on it. */
data class LenientFuelSurchargeBundle(
    var code: String,
    var title: String,
    var archived: Boolean,
    var country: String,
    var rate: Double,
    var status: LenientFuelSurchargeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientFuelSurchargeBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientFuelSurchargeBundleStatus): Boolean {
        when (status) {
            LenientFuelSurchargeBundleStatus.REQUESTED -> {
                when (next) {
                    LenientFuelSurchargeBundleStatus.REVOKED -> return true
                }
                return false
            }
            LenientFuelSurchargeBundleStatus.GRANTED -> return next == LenientFuelSurchargeBundleStatus.EXPIRED
            LenientFuelSurchargeBundleStatus.REVOKED -> {
                when (next) {
                    LenientFuelSurchargeBundleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientFuelSurchargeBundleStatus) {
        if (!canMoveTo(next)) {
            error("lenient fuel surcharge bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = rate

    fun snapshot(): LenientFuelSurchargeBundle {
        return LenientFuelSurchargeBundle(
            code = code,
            title = title,
            archived = archived,
            country = country,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient fuel surcharge bundle in status requested. */
fun newLenientFuelSurchargeBundle(code: String, title: String): LenientFuelSurchargeBundle {
    return LenientFuelSurchargeBundle(
        code = code,
        title = title,
        archived = false,
        country = "",
        rate = 0.0,
        status = LenientFuelSurchargeBundleStatus.REQUESTED,
    )
}
