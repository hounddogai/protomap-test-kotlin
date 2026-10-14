package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge window is in its life cycle. */
enum class FuelSurchargeWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fuel surcharge window as the fuel surcharge team keeps it. */
data class FuelSurchargeWindow(
    var code: String,
    var name: String,
    var dock: String,
    var ownerId: String,
    var title: String,
    var country: String,
    var description: String,
    var threshold: Double,
    var startsAt: Int,
    var shiftCode: String,
    var status: FuelSurchargeWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeWindowStatus): Boolean {
        when (status) {
            FuelSurchargeWindowStatus.DRAFT -> {
                when (next) {
                    FuelSurchargeWindowStatus.ACTIVE, FuelSurchargeWindowStatus.CLOSED -> return true
                }
                return false
            }
            FuelSurchargeWindowStatus.PENDING -> return next == FuelSurchargeWindowStatus.CLOSED
            FuelSurchargeWindowStatus.ACTIVE -> {
                when (next) {
                    FuelSurchargeWindowStatus.SUSPENDED, FuelSurchargeWindowStatus.CLOSED -> return true
                }
                return false
            }
            FuelSurchargeWindowStatus.SUSPENDED -> {
                when (next) {
                    FuelSurchargeWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeWindowStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge window $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fuel surcharge window"} $code"

    fun adjustedExposure(): Double = round(startsAt.toDouble() * threshold * 100) / 100

    fun snapshot(): FuelSurchargeWindow {
        return FuelSurchargeWindow(
            code = code,
            name = name,
            dock = dock,
            ownerId = ownerId,
            title = title,
            country = country,
            description = description,
            threshold = threshold,
            startsAt = startsAt,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a fuel surcharge window in status draft. */
fun newFuelSurchargeWindow(code: String, name: String): FuelSurchargeWindow {
    return FuelSurchargeWindow(
        code = code,
        name = name,
        dock = "",
        ownerId = "",
        title = "",
        country = "",
        description = "",
        threshold = 0.0,
        startsAt = 0,
        shiftCode = "",
        status = FuelSurchargeWindowStatus.DRAFT,
    )
}
