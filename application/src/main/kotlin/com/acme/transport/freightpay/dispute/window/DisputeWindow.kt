package com.acme.transport.freightpay.dispute.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dispute window records of freightpay's dispute work, and the rules, stores, and calculations around
// them.

/** Where a dispute window is in its life cycle. */
enum class DisputeWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a dispute window and its current status. */
data class DisputeWindow(
    var code: String,
    var title: String,
    var source: String,
    var expiresAt: Int,
    var odometer: Int,
    var checksum: String,
    var depot: String,
    var channel: String,
    var unit: String,
    var status: DisputeWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeWindowStatus): Boolean {
        when (status) {
            DisputeWindowStatus.CREATED -> {
                when (next) {
                    DisputeWindowStatus.VALIDATED, DisputeWindowStatus.PUBLISHED -> return true
                }
                return false
            }
            DisputeWindowStatus.VALIDATED -> return next == DisputeWindowStatus.PUBLISHED
            DisputeWindowStatus.PUBLISHED -> return next == DisputeWindowStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DisputeWindowStatus) {
        if (!canMoveTo(next)) {
            error("dispute window $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round((expiresAt.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): DisputeWindow {
        return DisputeWindow(
            code = code,
            title = title,
            source = source,
            expiresAt = expiresAt,
            odometer = odometer,
            checksum = checksum,
            depot = depot,
            channel = channel,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a dispute window in status created. */
fun newDisputeWindow(code: String, title: String): DisputeWindow {
    return DisputeWindow(
        code = code,
        title = title,
        source = "",
        expiresAt = 0,
        odometer = 0,
        checksum = "",
        depot = "",
        channel = "",
        unit = "",
        status = DisputeWindowStatus.CREATED,
    )
}
