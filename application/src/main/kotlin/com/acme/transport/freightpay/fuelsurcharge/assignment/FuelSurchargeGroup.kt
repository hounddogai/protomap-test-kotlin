package com.acme.transport.freightpay.fuelsurcharge.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge group is in its life cycle. */
enum class FuelSurchargeGroupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fuel surcharge group as the fuel surcharge team keeps it. */
data class FuelSurchargeGroup(
    var code: String,
    var title: String,
    var pallets: Int,
    var ownerId: String,
    var contactEmail: String,
    var country: String,
    var attributes: MutableMap<String, String>,
    var score: Double,
    var status: FuelSurchargeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeGroupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeGroupStatus): Boolean {
        when (status) {
            FuelSurchargeGroupStatus.OPEN -> {
                when (next) {
                    FuelSurchargeGroupStatus.ARCHIVED -> return true
                }
                return false
            }
            FuelSurchargeGroupStatus.IN_REVIEW -> {
                when (next) {
                    FuelSurchargeGroupStatus.RESOLVED -> return true
                }
                return false
            }
            FuelSurchargeGroupStatus.RESOLVED -> {
                when (next) {
                    FuelSurchargeGroupStatus.ARCHIVED -> return true
                }
                return false
            }
            FuelSurchargeGroupStatus.REOPENED -> {
                when (next) {
                    FuelSurchargeGroupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeGroupStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fuel surcharge group"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round((pallets.toDouble() + score) * 100) / 100

    fun snapshot(): FuelSurchargeGroup {
        return FuelSurchargeGroup(
            code = code,
            title = title,
            pallets = pallets,
            ownerId = ownerId,
            contactEmail = contactEmail,
            country = country,
            attributes = attributes,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a fuel surcharge group in status open. */
fun newFuelSurchargeGroup(code: String, title: String): FuelSurchargeGroup {
    return FuelSurchargeGroup(
        code = code,
        title = title,
        pallets = 0,
        ownerId = "",
        contactEmail = "",
        country = "",
        attributes = mutableMapOf<String, String>(),
        score = 0.0,
        status = FuelSurchargeGroupStatus.OPEN,
    )
}
