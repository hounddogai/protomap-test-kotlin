package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time summary is in its life cycle. */
enum class TransitTimeSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A transit time summary as the transit time team keeps it. */
data class TransitTimeSummary(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var ceiling: Double,
    var title: String,
    var pallets: Int,
    var country: String,
    var category: String,
    var trailerId: String,
    var shiftCode: String,
    var status: TransitTimeSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeSummaryStatus): Boolean {
        when (status) {
            TransitTimeSummaryStatus.PROPOSED -> {
                when (next) {
                    TransitTimeSummaryStatus.APPROVED -> return true
                }
                return false
            }
            TransitTimeSummaryStatus.APPROVED -> {
                when (next) {
                    TransitTimeSummaryStatus.REJECTED, TransitTimeSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            TransitTimeSummaryStatus.REJECTED -> return next == TransitTimeSummaryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TransitTimeSummaryStatus) {
        if (!canMoveTo(next)) {
            error("transit time summary $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedExposure(): Double = round(pallets.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): TransitTimeSummary {
        return TransitTimeSummary(
            code = code,
            name = name,
            updatedAt = updatedAt,
            ceiling = ceiling,
            title = title,
            pallets = pallets,
            country = country,
            category = category,
            trailerId = trailerId,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a transit time summary in status proposed. */
fun newTransitTimeSummary(code: String, name: String): TransitTimeSummary {
    return TransitTimeSummary(
        code = code,
        name = name,
        updatedAt = 0,
        ceiling = 0.0,
        title = "",
        pallets = 0,
        country = "",
        category = "",
        trailerId = "",
        shiftCode = "",
        status = TransitTimeSummaryStatus.PROPOSED,
    )
}
