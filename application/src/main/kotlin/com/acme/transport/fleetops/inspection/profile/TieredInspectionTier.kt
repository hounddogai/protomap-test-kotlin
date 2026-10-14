package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered inspection tier is in its life cycle. */
enum class TieredInspectionTierStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One tiered inspection tier, with what the planners need to act on it. */
data class TieredInspectionTier(
    var code: String,
    var name: String,
    var odometer: Int,
    var category: String,
    var discount: Double,
    var country: String,
    var quantity: Int,
    var status: TieredInspectionTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredInspectionTierStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredInspectionTierStatus): Boolean {
        when (status) {
            TieredInspectionTierStatus.CREATED -> return next == TieredInspectionTierStatus.RETIRED
            TieredInspectionTierStatus.VALIDATED -> {
                when (next) {
                    TieredInspectionTierStatus.PUBLISHED -> return true
                }
                return false
            }
            TieredInspectionTierStatus.PUBLISHED -> return next == TieredInspectionTierStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredInspectionTierStatus) {
        if (!canMoveTo(next)) {
            error("tiered inspection tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveLoad(): Double = round(discount * odometer.toDouble() * 100) / 100

    fun snapshot(): TieredInspectionTier {
        return TieredInspectionTier(
            code = code,
            name = name,
            odometer = odometer,
            category = category,
            discount = discount,
            country = country,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tiered inspection tier in status created. */
fun newTieredInspectionTier(code: String, name: String): TieredInspectionTier {
    return TieredInspectionTier(
        code = code,
        name = name,
        odometer = 0,
        category = "",
        discount = 0.0,
        country = "",
        quantity = 0,
        status = TieredInspectionTierStatus.CREATED,
    )
}
