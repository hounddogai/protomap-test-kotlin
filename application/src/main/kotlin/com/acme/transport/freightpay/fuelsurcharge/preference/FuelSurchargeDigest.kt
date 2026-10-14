package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge digest is in its life cycle. */
enum class FuelSurchargeDigestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fuel surcharge digest as the fuel surcharge team keeps it. */
data class FuelSurchargeDigest(
    var code: String,
    var title: String,
    var discount: Double,
    var rate: Double,
    var graceDays: Int,
    var minimum: Double,
    var status: FuelSurchargeDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeDigestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeDigestStatus): Boolean {
        when (status) {
            FuelSurchargeDigestStatus.CREATED -> {
                when (next) {
                    FuelSurchargeDigestStatus.VALIDATED, FuelSurchargeDigestStatus.RETIRED -> return true
                }
                return false
            }
            FuelSurchargeDigestStatus.VALIDATED -> {
                when (next) {
                    FuelSurchargeDigestStatus.PUBLISHED, FuelSurchargeDigestStatus.RETIRED -> return true
                }
                return false
            }
            FuelSurchargeDigestStatus.PUBLISHED -> return next == FuelSurchargeDigestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeDigestStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedLoad(): Double = round(minimum * discount * 100) / 100

    fun snapshot(): FuelSurchargeDigest {
        return FuelSurchargeDigest(
            code = code,
            title = title,
            discount = discount,
            rate = rate,
            graceDays = graceDays,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel surcharge digest in status created. */
fun newFuelSurchargeDigest(code: String, title: String): FuelSurchargeDigest {
    return FuelSurchargeDigest(
        code = code,
        title = title,
        discount = 0.0,
        rate = 0.0,
        graceDays = 0,
        minimum = 0.0,
        status = FuelSurchargeDigestStatus.CREATED,
    )
}
