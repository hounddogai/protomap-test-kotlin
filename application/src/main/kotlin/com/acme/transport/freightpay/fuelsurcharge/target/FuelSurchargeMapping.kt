package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.kit.text.normalize

/** Where a fuel surcharge mapping is in its life cycle. */
enum class FuelSurchargeMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One fuel surcharge mapping, with what the planners need to act on it. */
data class FuelSurchargeMapping(
    var code: String,
    var label: String,
    var phone: String,
    var externalRef: String,
    var country: String,
    var locale: String,
    var rate: Double,
    var status: FuelSurchargeMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeMappingStatus): Boolean {
        when (status) {
            FuelSurchargeMappingStatus.REQUESTED -> return next == FuelSurchargeMappingStatus.GRANTED || next == FuelSurchargeMappingStatus.EXPIRED
            FuelSurchargeMappingStatus.GRANTED -> return next == FuelSurchargeMappingStatus.REVOKED
            FuelSurchargeMappingStatus.REVOKED -> {
                when (next) {
                    FuelSurchargeMappingStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeMappingStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fuel surcharge mapping"} $code"

    fun measuredValue(): Double = rate

    fun snapshot(): FuelSurchargeMapping {
        return FuelSurchargeMapping(
            code = code,
            label = label,
            phone = phone,
            externalRef = externalRef,
            country = country,
            locale = locale,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a fuel surcharge mapping in status requested. */
fun newFuelSurchargeMapping(code: String, label: String): FuelSurchargeMapping {
    return FuelSurchargeMapping(
        code = code,
        label = label,
        phone = "",
        externalRef = "",
        country = "",
        locale = "",
        rate = 0.0,
        status = FuelSurchargeMappingStatus.REQUESTED,
    )
}
