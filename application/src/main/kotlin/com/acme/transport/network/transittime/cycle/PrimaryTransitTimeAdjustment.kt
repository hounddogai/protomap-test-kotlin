package com.acme.transport.network.transittime.cycle

import com.acme.transport.kit.text.normalize

/** Where a primary transit time adjustment is in its life cycle. */
enum class PrimaryTransitTimeAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A primary transit time adjustment. Its code is unique within a tenant. */
data class PrimaryTransitTimeAdjustment(
    var code: String,
    var title: String,
    var channel: String,
    var segment: String,
    var shiftCode: String,
    var carrier: String,
    var quantity: Int,
    var country: String,
    var status: PrimaryTransitTimeAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTransitTimeAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTransitTimeAdjustmentStatus): Boolean {
        when (status) {
            PrimaryTransitTimeAdjustmentStatus.PROPOSED -> return next == PrimaryTransitTimeAdjustmentStatus.APPROVED || next == PrimaryTransitTimeAdjustmentStatus.WITHDRAWN
            PrimaryTransitTimeAdjustmentStatus.APPROVED -> {
                when (next) {
                    PrimaryTransitTimeAdjustmentStatus.REJECTED, PrimaryTransitTimeAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryTransitTimeAdjustmentStatus.REJECTED -> {
                when (next) {
                    PrimaryTransitTimeAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTransitTimeAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("primary transit time adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): PrimaryTransitTimeAdjustment {
        return PrimaryTransitTimeAdjustment(
            code = code,
            title = title,
            channel = channel,
            segment = segment,
            shiftCode = shiftCode,
            carrier = carrier,
            quantity = quantity,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a primary transit time adjustment in status proposed. */
fun newPrimaryTransitTimeAdjustment(code: String, title: String): PrimaryTransitTimeAdjustment {
    return PrimaryTransitTimeAdjustment(
        code = code,
        title = title,
        channel = "",
        segment = "",
        shiftCode = "",
        carrier = "",
        quantity = 0,
        country = "",
        status = PrimaryTransitTimeAdjustmentStatus.PROPOSED,
    )
}
