package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone slot is in its life cycle. */
enum class ZoneSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a zone slot and its current status. */
data class ZoneSlot(
    var code: String,
    var title: String,
    var discount: Double,
    var region: String,
    var sequence: Int,
    var locale: String,
    var active: Boolean,
    var endsAt: Int,
    var quantity: Int,
    var status: ZoneSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneSlotStatus): Boolean {
        when (status) {
            ZoneSlotStatus.OPEN -> return next == ZoneSlotStatus.IN_REVIEW
            ZoneSlotStatus.IN_REVIEW -> return next == ZoneSlotStatus.ARCHIVED
            ZoneSlotStatus.RESOLVED -> {
                when (next) {
                    ZoneSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            ZoneSlotStatus.REOPENED -> return next == ZoneSlotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ZoneSlotStatus) {
        if (!canMoveTo(next)) {
            error("zone slot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedExposure(): Double = round((sequence.toDouble() + discount) * 100) / 100

    fun snapshot(): ZoneSlot {
        return ZoneSlot(
            code = code,
            title = title,
            discount = discount,
            region = region,
            sequence = sequence,
            locale = locale,
            active = active,
            endsAt = endsAt,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a zone slot in status open. */
fun newZoneSlot(code: String, title: String): ZoneSlot {
    return ZoneSlot(
        code = code,
        title = title,
        discount = 0.0,
        region = "",
        sequence = 0,
        locale = "",
        active = false,
        endsAt = 0,
        quantity = 0,
        status = ZoneSlotStatus.OPEN,
    )
}
