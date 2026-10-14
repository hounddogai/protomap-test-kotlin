package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped backhaul window is in its life cycle. */
enum class CappedBackhaulWindowStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped backhaul window. Its code is unique within a tenant. */
data class CappedBackhaulWindow(
    var code: String,
    var title: String,
    var country: String,
    var approved: Boolean,
    var channel: String,
    var expiresAt: Int,
    var lane: String,
    var quantity: Int,
    var status: CappedBackhaulWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedBackhaulWindowStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedBackhaulWindowStatus): Boolean {
        when (status) {
            CappedBackhaulWindowStatus.REQUESTED -> {
                when (next) {
                    CappedBackhaulWindowStatus.EXPIRED -> return true
                }
                return false
            }
            CappedBackhaulWindowStatus.GRANTED -> {
                when (next) {
                    CappedBackhaulWindowStatus.REVOKED, CappedBackhaulWindowStatus.EXPIRED -> return true
                }
                return false
            }
            CappedBackhaulWindowStatus.REVOKED -> return next == CappedBackhaulWindowStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedBackhaulWindowStatus) {
        if (!canMoveTo(next)) {
            error("capped backhaul window $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun netExposure(): Double = round(quantity.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): CappedBackhaulWindow {
        return CappedBackhaulWindow(
            code = code,
            title = title,
            country = country,
            approved = approved,
            channel = channel,
            expiresAt = expiresAt,
            lane = lane,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped backhaul window in status requested. */
fun newCappedBackhaulWindow(code: String, title: String): CappedBackhaulWindow {
    return CappedBackhaulWindow(
        code = code,
        title = title,
        country = "",
        approved = false,
        channel = "",
        expiresAt = 0,
        lane = "",
        quantity = 0,
        status = CappedBackhaulWindowStatus.REQUESTED,
    )
}
