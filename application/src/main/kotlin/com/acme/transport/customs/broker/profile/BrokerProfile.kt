package com.acme.transport.customs.broker.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The broker profile records of customs's broker work, and the rules, stores, and calculations around them.

/** Where a broker profile is in its life cycle. */
enum class BrokerProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A broker profile as the broker team keeps it. */
data class BrokerProfile(
    var code: String,
    var title: String,
    var region: String,
    var priority: Int,
    var sequence: Int,
    var status: BrokerProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerProfileStatus): Boolean {
        when (status) {
            BrokerProfileStatus.PLANNED -> return next == BrokerProfileStatus.CANCELLED
            BrokerProfileStatus.SCHEDULED -> {
                when (next) {
                    BrokerProfileStatus.COMPLETED, BrokerProfileStatus.CANCELLED -> return true
                }
                return false
            }
            BrokerProfileStatus.IN_PROGRESS -> {
                when (next) {
                    BrokerProfileStatus.COMPLETED, BrokerProfileStatus.CANCELLED -> return true
                }
                return false
            }
            BrokerProfileStatus.COMPLETED -> return next == BrokerProfileStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BrokerProfileStatus) {
        if (!canMoveTo(next)) {
            error("broker profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netExposure(): Double = round((sequence.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): BrokerProfile {
        return BrokerProfile(
            code = code,
            title = title,
            region = region,
            priority = priority,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a broker profile in status planned. */
fun newBrokerProfile(code: String, title: String): BrokerProfile {
    return BrokerProfile(
        code = code,
        title = title,
        region = "",
        priority = 0,
        sequence = 0,
        status = BrokerProfileStatus.PLANNED,
    )
}
