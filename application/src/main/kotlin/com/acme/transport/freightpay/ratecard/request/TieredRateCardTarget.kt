package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered rate card target is in its life cycle. */
enum class TieredRateCardTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a tiered rate card target and its current status. */
data class TieredRateCardTarget(
    var code: String,
    var title: String,
    var quantity: Int,
    var currency: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var depot: String,
    var cost: Double,
    var minimum: Double,
    var status: TieredRateCardTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRateCardTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRateCardTargetStatus): Boolean {
        when (status) {
            TieredRateCardTargetStatus.PLANNED -> {
                when (next) {
                    TieredRateCardTargetStatus.SCHEDULED, TieredRateCardTargetStatus.CANCELLED -> return true
                }
                return false
            }
            TieredRateCardTargetStatus.SCHEDULED -> return next == TieredRateCardTargetStatus.COMPLETED
            TieredRateCardTargetStatus.IN_PROGRESS -> return next == TieredRateCardTargetStatus.CANCELLED
            TieredRateCardTargetStatus.COMPLETED -> return next == TieredRateCardTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredRateCardTargetStatus) {
        if (!canMoveTo(next)) {
            error("tiered rate card target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((cost + quantity.toDouble()) * 100) / 100

    fun snapshot(): TieredRateCardTarget {
        return TieredRateCardTarget(
            code = code,
            title = title,
            quantity = quantity,
            currency = currency,
            name = name,
            attributes = attributes,
            depot = depot,
            cost = cost,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tiered rate card target in status planned. */
fun newTieredRateCardTarget(code: String, title: String): TieredRateCardTarget {
    return TieredRateCardTarget(
        code = code,
        title = title,
        quantity = 0,
        currency = "",
        name = "",
        attributes = mutableMapOf<String, String>(),
        depot = "",
        cost = 0.0,
        minimum = 0.0,
        status = TieredRateCardTargetStatus.PLANNED,
    )
}
