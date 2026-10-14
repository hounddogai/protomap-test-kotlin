package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered camera event slot is in its life cycle. */
enum class TieredCameraEventSlotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tiered camera event slot as the camera event team keeps it. */
data class TieredCameraEventSlot(
    var code: String,
    var title: String,
    var shiftCode: String,
    var windowDays: Int,
    var depot: String,
    var phone: String,
    var cost: Double,
    var name: String,
    var rate: Double,
    var segment: String,
    var amount: Double,
    var status: TieredCameraEventSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCameraEventSlotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCameraEventSlotStatus): Boolean {
        when (status) {
            TieredCameraEventSlotStatus.IDLE -> return next == TieredCameraEventSlotStatus.WARMING
            TieredCameraEventSlotStatus.WARMING -> return next == TieredCameraEventSlotStatus.READY || next == TieredCameraEventSlotStatus.STOPPED
            TieredCameraEventSlotStatus.READY -> return next == TieredCameraEventSlotStatus.DRAINING
            TieredCameraEventSlotStatus.DRAINING -> return next == TieredCameraEventSlotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TieredCameraEventSlotStatus) {
        if (!canMoveTo(next)) {
            error("tiered camera event slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netLoad(): Double = round(amount * rate * 100) / 100

    fun snapshot(): TieredCameraEventSlot {
        return TieredCameraEventSlot(
            code = code,
            title = title,
            shiftCode = shiftCode,
            windowDays = windowDays,
            depot = depot,
            phone = phone,
            cost = cost,
            name = name,
            rate = rate,
            segment = segment,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tiered camera event slot in status idle. */
fun newTieredCameraEventSlot(code: String, title: String): TieredCameraEventSlot {
    return TieredCameraEventSlot(
        code = code,
        title = title,
        shiftCode = "",
        windowDays = 0,
        depot = "",
        phone = "",
        cost = 0.0,
        name = "",
        rate = 0.0,
        segment = "",
        amount = 0.0,
        status = TieredCameraEventSlotStatus.IDLE,
    )
}
