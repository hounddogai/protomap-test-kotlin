package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay profile is in its life cycle. */
enum class RelayProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A relay profile as the relay team keeps it. */
data class RelayProfile(
    var code: String,
    var title: String,
    var floorValue: Double,
    var threshold: Double,
    var priority: Int,
    var limit: Int,
    var approved: Boolean,
    var quantity: Int,
    var status: RelayProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayProfileStatus): Boolean {
        when (status) {
            RelayProfileStatus.REQUESTED -> {
                when (next) {
                    RelayProfileStatus.GRANTED -> return true
                }
                return false
            }
            RelayProfileStatus.GRANTED -> return next == RelayProfileStatus.EXPIRED
            RelayProfileStatus.REVOKED -> {
                when (next) {
                    RelayProfileStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RelayProfileStatus) {
        if (!canMoveTo(next)) {
            error("relay profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"relay profile"} $code"

    fun weightedTotal(): Double = round((threshold - priority.toDouble()) * 100) / 100

    fun snapshot(): RelayProfile {
        return RelayProfile(
            code = code,
            title = title,
            floorValue = floorValue,
            threshold = threshold,
            priority = priority,
            limit = limit,
            approved = approved,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a relay profile in status requested. */
fun newRelayProfile(code: String, title: String): RelayProfile {
    return RelayProfile(
        code = code,
        title = title,
        floorValue = 0.0,
        threshold = 0.0,
        priority = 0,
        limit = 0,
        approved = false,
        quantity = 0,
        status = RelayProfileStatus.REQUESTED,
    )
}
