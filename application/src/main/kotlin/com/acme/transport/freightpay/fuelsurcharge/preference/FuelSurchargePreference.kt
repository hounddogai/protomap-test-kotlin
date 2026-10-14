package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel surcharge preference records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a fuel surcharge preference is in its life cycle. */
enum class FuelSurchargePreferenceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fuel surcharge preference as the fuel surcharge team keeps it. */
data class FuelSurchargePreference(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var threshold: Double,
    var revision: Int,
    var budget: Double,
    var discount: Double,
    var quantity: Int,
    var graceDays: Int,
    var status: FuelSurchargePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargePreferenceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargePreferenceStatus): Boolean {
        when (status) {
            FuelSurchargePreferenceStatus.DRAFT -> {
                when (next) {
                    FuelSurchargePreferenceStatus.SUSPENDED -> return true
                }
                return false
            }
            FuelSurchargePreferenceStatus.PENDING -> return next == FuelSurchargePreferenceStatus.ACTIVE
            FuelSurchargePreferenceStatus.ACTIVE -> {
                when (next) {
                    FuelSurchargePreferenceStatus.SUSPENDED, FuelSurchargePreferenceStatus.CLOSED -> return true
                }
                return false
            }
            FuelSurchargePreferenceStatus.SUSPENDED -> return next == FuelSurchargePreferenceStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FuelSurchargePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge preference $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netExposure(): Double = round(revision.toDouble() * threshold * 100) / 100

    fun snapshot(): FuelSurchargePreference {
        return FuelSurchargePreference(
            code = code,
            title = title,
            attributes = attributes,
            threshold = threshold,
            revision = revision,
            budget = budget,
            discount = discount,
            quantity = quantity,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fuel surcharge preference in status draft. */
fun newFuelSurchargePreference(code: String, title: String): FuelSurchargePreference {
    return FuelSurchargePreference(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        threshold = 0.0,
        revision = 0,
        budget = 0.0,
        discount = 0.0,
        quantity = 0,
        graceDays = 0,
        status = FuelSurchargePreferenceStatus.DRAFT,
    )
}
