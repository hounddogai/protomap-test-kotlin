package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer yard quota records of terminal's trailer yard work, and the rules, stores, and calculations
// around them.

/** Where a trailer yard quota is in its life cycle. */
enum class TrailerYardQuotaStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a trailer yard quota and its current status. */
data class TrailerYardQuota(
    var code: String,
    var label: String,
    var depot: String,
    var minimum: Double,
    var retries: Int,
    var discount: Double,
    var shiftCode: String,
    var visible: Boolean,
    var version: Int,
    var channel: String,
    var driverId: String,
    var status: TrailerYardQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardQuotaStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardQuotaStatus): Boolean {
        when (status) {
            TrailerYardQuotaStatus.CREATED -> return next == TrailerYardQuotaStatus.RETIRED
            TrailerYardQuotaStatus.VALIDATED -> return next == TrailerYardQuotaStatus.PUBLISHED
            TrailerYardQuotaStatus.PUBLISHED -> {
                when (next) {
                    TrailerYardQuotaStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardQuotaStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard quota $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun netValue(): Double = round(minimum * discount * 100) / 100

    fun snapshot(): TrailerYardQuota {
        return TrailerYardQuota(
            code = code,
            label = label,
            depot = depot,
            minimum = minimum,
            retries = retries,
            discount = discount,
            shiftCode = shiftCode,
            visible = visible,
            version = version,
            channel = channel,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a trailer yard quota in status created. */
fun newTrailerYardQuota(code: String, label: String): TrailerYardQuota {
    return TrailerYardQuota(
        code = code,
        label = label,
        depot = "",
        minimum = 0.0,
        retries = 0,
        discount = 0.0,
        shiftCode = "",
        visible = false,
        version = 0,
        channel = "",
        driverId = "",
        status = TrailerYardQuotaStatus.CREATED,
    )
}
