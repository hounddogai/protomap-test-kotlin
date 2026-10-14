package com.acme.transport.customs.broker.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker group is in its life cycle. */
enum class BrokerGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A broker group. Its code is unique within a tenant. */
data class BrokerGroup(
    var code: String,
    var name: String,
    var threshold: Double,
    var startsAt: Int,
    var phone: String,
    var distanceKm: Double,
    var status: BrokerGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerGroupStatus): Boolean {
        when (status) {
            BrokerGroupStatus.PLANNED -> return next == BrokerGroupStatus.COMPLETED || next == BrokerGroupStatus.CANCELLED
            BrokerGroupStatus.SCHEDULED -> {
                when (next) {
                    BrokerGroupStatus.IN_PROGRESS, BrokerGroupStatus.CANCELLED -> return true
                }
                return false
            }
            BrokerGroupStatus.IN_PROGRESS -> {
                when (next) {
                    BrokerGroupStatus.COMPLETED, BrokerGroupStatus.CANCELLED -> return true
                }
                return false
            }
            BrokerGroupStatus.COMPLETED -> {
                when (next) {
                    BrokerGroupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerGroupStatus) {
        if (!canMoveTo(next)) {
            error("broker group $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"broker group"} $code"

    fun adjustedValue(): Double = round((startsAt.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): BrokerGroup {
        return BrokerGroup(
            code = code,
            name = name,
            threshold = threshold,
            startsAt = startsAt,
            phone = phone,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a broker group in status planned. */
fun newBrokerGroup(code: String, name: String): BrokerGroup {
    return BrokerGroup(
        code = code,
        name = name,
        threshold = 0.0,
        startsAt = 0,
        phone = "",
        distanceKm = 0.0,
        status = BrokerGroupStatus.PLANNED,
    )
}
