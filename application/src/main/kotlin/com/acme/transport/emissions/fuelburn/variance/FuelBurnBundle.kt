package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn bundle is in its life cycle. */
enum class FuelBurnBundleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fuel burn bundle. Its code is unique within a tenant. */
data class FuelBurnBundle(
    var code: String,
    var title: String,
    var margin: Double,
    var pinned: Boolean,
    var discount: Double,
    var category: String,
    var loadKg: Double,
    var archived: Boolean,
    var priority: Int,
    var currency: String,
    var status: FuelBurnBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnBundleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnBundleStatus): Boolean {
        when (status) {
            FuelBurnBundleStatus.DRAFT -> return next == FuelBurnBundleStatus.CLOSED
            FuelBurnBundleStatus.PENDING -> return next == FuelBurnBundleStatus.ACTIVE || next == FuelBurnBundleStatus.CLOSED
            FuelBurnBundleStatus.ACTIVE -> {
                when (next) {
                    FuelBurnBundleStatus.SUSPENDED -> return true
                }
                return false
            }
            FuelBurnBundleStatus.SUSPENDED -> return next == FuelBurnBundleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: FuelBurnBundleStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fuel burn bundle"} $code"

    fun effectiveExposure(): Double = round(discount * loadKg * 100) / 100

    fun snapshot(): FuelBurnBundle {
        return FuelBurnBundle(
            code = code,
            title = title,
            margin = margin,
            pinned = pinned,
            discount = discount,
            category = category,
            loadKg = loadKg,
            archived = archived,
            priority = priority,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fuel burn bundle in status draft. */
fun newFuelBurnBundle(code: String, title: String): FuelBurnBundle {
    return FuelBurnBundle(
        code = code,
        title = title,
        margin = 0.0,
        pinned = false,
        discount = 0.0,
        category = "",
        loadKg = 0.0,
        archived = false,
        priority = 0,
        currency = "",
        status = FuelBurnBundleStatus.DRAFT,
    )
}
