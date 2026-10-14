package com.acme.transport.customs.hold.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hold group records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold group is in its life cycle. */
enum class HoldGroupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One hold group, with what the planners need to act on it. */
data class HoldGroup(
    var code: String,
    var name: String,
    var driverId: String,
    var locale: String,
    var lane: String,
    var active: Boolean,
    var version: Int,
    var currency: String,
    var amount: Double,
    var depot: String,
    var retries: Int,
    var status: HoldGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldGroupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldGroupStatus): Boolean {
        when (status) {
            HoldGroupStatus.OPEN -> return next == HoldGroupStatus.IN_REVIEW || next == HoldGroupStatus.RESOLVED
            HoldGroupStatus.IN_REVIEW -> {
                when (next) {
                    HoldGroupStatus.REOPENED -> return true
                }
                return false
            }
            HoldGroupStatus.RESOLVED -> {
                when (next) {
                    HoldGroupStatus.REOPENED, HoldGroupStatus.ARCHIVED -> return true
                }
                return false
            }
            HoldGroupStatus.REOPENED -> {
                when (next) {
                    HoldGroupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldGroupStatus) {
        if (!canMoveTo(next)) {
            error("hold group $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"hold group"} $code"

    fun weightedLoad(): Double = round(amount * retries.toDouble() * 100) / 100

    fun snapshot(): HoldGroup {
        return HoldGroup(
            code = code,
            name = name,
            driverId = driverId,
            locale = locale,
            lane = lane,
            active = active,
            version = version,
            currency = currency,
            amount = amount,
            depot = depot,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a hold group in status open. */
fun newHoldGroup(code: String, name: String): HoldGroup {
    return HoldGroup(
        code = code,
        name = name,
        driverId = "",
        locale = "",
        lane = "",
        active = false,
        version = 0,
        currency = "",
        amount = 0.0,
        depot = "",
        retries = 0,
        status = HoldGroupStatus.OPEN,
    )
}
