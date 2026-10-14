package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge record is in its life cycle. */
enum class FuelSurchargeRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a fuel surcharge record and its current status. */
data class FuelSurchargeRecord(
    var code: String,
    var name: String,
    var ceiling: Double,
    var externalRef: String,
    var attributes: MutableMap<String, String>,
    var depot: String,
    var timezone: String,
    var category: String,
    var discount: Double,
    var currency: String,
    var status: FuelSurchargeRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeRecordStatus): Boolean {
        when (status) {
            FuelSurchargeRecordStatus.PLANNED -> {
                when (next) {
                    FuelSurchargeRecordStatus.IN_PROGRESS, FuelSurchargeRecordStatus.CANCELLED -> return true
                }
                return false
            }
            FuelSurchargeRecordStatus.SCHEDULED -> {
                when (next) {
                    FuelSurchargeRecordStatus.IN_PROGRESS, FuelSurchargeRecordStatus.CANCELLED -> return true
                }
                return false
            }
            FuelSurchargeRecordStatus.IN_PROGRESS -> return next == FuelSurchargeRecordStatus.COMPLETED || next == FuelSurchargeRecordStatus.CANCELLED
            FuelSurchargeRecordStatus.COMPLETED -> return next == FuelSurchargeRecordStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeRecordStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fuel surcharge record"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round(discount * ceiling * 100) / 100

    fun snapshot(): FuelSurchargeRecord {
        return FuelSurchargeRecord(
            code = code,
            name = name,
            ceiling = ceiling,
            externalRef = externalRef,
            attributes = attributes,
            depot = depot,
            timezone = timezone,
            category = category,
            discount = discount,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fuel surcharge record in status planned. */
fun newFuelSurchargeRecord(code: String, name: String): FuelSurchargeRecord {
    return FuelSurchargeRecord(
        code = code,
        name = name,
        ceiling = 0.0,
        externalRef = "",
        attributes = mutableMapOf<String, String>(),
        depot = "",
        timezone = "",
        category = "",
        discount = 0.0,
        currency = "",
        status = FuelSurchargeRecordStatus.PLANNED,
    )
}
