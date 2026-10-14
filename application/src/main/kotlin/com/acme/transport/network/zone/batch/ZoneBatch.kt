package com.acme.transport.network.zone.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone batch records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone batch is in its life cycle. */
enum class ZoneBatchStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A zone batch as the zone team keeps it. */
data class ZoneBatch(
    var code: String,
    var title: String,
    var rate: Double,
    var vehicleId: String,
    var amount: Double,
    var archived: Boolean,
    var dock: String,
    var limit: Int,
    var phone: String,
    var attempts: Int,
    var status: ZoneBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneBatchStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneBatchStatus): Boolean {
        when (status) {
            ZoneBatchStatus.PROPOSED -> {
                when (next) {
                    ZoneBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
            ZoneBatchStatus.APPROVED -> return next == ZoneBatchStatus.REJECTED || next == ZoneBatchStatus.WITHDRAWN
            ZoneBatchStatus.REJECTED -> {
                when (next) {
                    ZoneBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneBatchStatus) {
        if (!canMoveTo(next)) {
            error("zone batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedValue(): Double = round(attempts.toDouble() * amount * 100) / 100

    fun snapshot(): ZoneBatch {
        return ZoneBatch(
            code = code,
            title = title,
            rate = rate,
            vehicleId = vehicleId,
            amount = amount,
            archived = archived,
            dock = dock,
            limit = limit,
            phone = phone,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a zone batch in status proposed. */
fun newZoneBatch(code: String, title: String): ZoneBatch {
    return ZoneBatch(
        code = code,
        title = title,
        rate = 0.0,
        vehicleId = "",
        amount = 0.0,
        archived = false,
        dock = "",
        limit = 0,
        phone = "",
        attempts = 0,
        status = ZoneBatchStatus.PROPOSED,
    )
}
