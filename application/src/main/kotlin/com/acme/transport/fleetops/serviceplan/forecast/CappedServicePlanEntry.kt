package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped service plan entry is in its life cycle. */
enum class CappedServicePlanEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a capped service plan entry and its current status. */
data class CappedServicePlanEntry(
    var code: String,
    var title: String,
    var quantity: Int,
    var pallets: Int,
    var name: String,
    var dock: String,
    var cost: Double,
    var budget: Double,
    var fuelLiters: Double,
    var retries: Int,
    var discount: Double,
    var status: CappedServicePlanEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedServicePlanEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedServicePlanEntryStatus): Boolean {
        when (status) {
            CappedServicePlanEntryStatus.DRAFT -> return next == CappedServicePlanEntryStatus.PENDING || next == CappedServicePlanEntryStatus.SUSPENDED
            CappedServicePlanEntryStatus.PENDING -> return next == CappedServicePlanEntryStatus.SUSPENDED
            CappedServicePlanEntryStatus.ACTIVE -> return next == CappedServicePlanEntryStatus.SUSPENDED
            CappedServicePlanEntryStatus.SUSPENDED -> return next == CappedServicePlanEntryStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedServicePlanEntryStatus) {
        if (!canMoveTo(next)) {
            error("capped service plan entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round((cost + pallets.toDouble()) * 100) / 100

    fun snapshot(): CappedServicePlanEntry {
        return CappedServicePlanEntry(
            code = code,
            title = title,
            quantity = quantity,
            pallets = pallets,
            name = name,
            dock = dock,
            cost = cost,
            budget = budget,
            fuelLiters = fuelLiters,
            retries = retries,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped service plan entry in status draft. */
fun newCappedServicePlanEntry(code: String, title: String): CappedServicePlanEntry {
    return CappedServicePlanEntry(
        code = code,
        title = title,
        quantity = 0,
        pallets = 0,
        name = "",
        dock = "",
        cost = 0.0,
        budget = 0.0,
        fuelLiters = 0.0,
        retries = 0,
        discount = 0.0,
        status = CappedServicePlanEntryStatus.DRAFT,
    )
}
