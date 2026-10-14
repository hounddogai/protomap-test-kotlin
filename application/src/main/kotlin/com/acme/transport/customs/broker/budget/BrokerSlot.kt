package com.acme.transport.customs.broker.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker slot is in its life cycle. */
enum class BrokerSlotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A broker slot as the broker team keeps it. */
data class BrokerSlot(
    var code: String,
    var name: String,
    var lane: String,
    var approved: Boolean,
    var createdAt: Int,
    var segment: String,
    var visible: Boolean,
    var etaMinutes: Int,
    var status: BrokerSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerSlotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerSlotStatus): Boolean {
        when (status) {
            BrokerSlotStatus.CREATED -> {
                when (next) {
                    BrokerSlotStatus.PUBLISHED, BrokerSlotStatus.RETIRED -> return true
                }
                return false
            }
            BrokerSlotStatus.VALIDATED -> {
                when (next) {
                    BrokerSlotStatus.PUBLISHED, BrokerSlotStatus.RETIRED -> return true
                }
                return false
            }
            BrokerSlotStatus.PUBLISHED -> return next == BrokerSlotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BrokerSlotStatus) {
        if (!canMoveTo(next)) {
            error("broker slot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"broker slot"} $code"

    fun adjustedVolume(): Double = round(createdAt.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): BrokerSlot {
        return BrokerSlot(
            code = code,
            name = name,
            lane = lane,
            approved = approved,
            createdAt = createdAt,
            segment = segment,
            visible = visible,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a broker slot in status created. */
fun newBrokerSlot(code: String, name: String): BrokerSlot {
    return BrokerSlot(
        code = code,
        name = name,
        lane = "",
        approved = false,
        createdAt = 0,
        segment = "",
        visible = false,
        etaMinutes = 0,
        status = BrokerSlotStatus.CREATED,
    )
}
