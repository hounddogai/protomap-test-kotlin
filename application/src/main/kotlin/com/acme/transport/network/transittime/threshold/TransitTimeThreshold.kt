package com.acme.transport.network.transittime.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The transit time threshold records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time threshold is in its life cycle. */
enum class TransitTimeThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A transit time threshold as the transit time team keeps it. */
data class TransitTimeThreshold(
    var code: String,
    var title: String,
    var description: String,
    var createdAt: Int,
    var capacity: Int,
    var fuelLiters: Double,
    var phone: String,
    var label: String,
    var notes: String,
    var status: TransitTimeThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeThresholdStatus): Boolean {
        when (status) {
            TransitTimeThresholdStatus.REQUESTED -> {
                when (next) {
                    TransitTimeThresholdStatus.GRANTED -> return true
                }
                return false
            }
            TransitTimeThresholdStatus.GRANTED -> {
                when (next) {
                    TransitTimeThresholdStatus.REVOKED, TransitTimeThresholdStatus.EXPIRED -> return true
                }
                return false
            }
            TransitTimeThresholdStatus.REVOKED -> {
                when (next) {
                    TransitTimeThresholdStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeThresholdStatus) {
        if (!canMoveTo(next)) {
            error("transit time threshold $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((createdAt.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): TransitTimeThreshold {
        return TransitTimeThreshold(
            code = code,
            title = title,
            description = description,
            createdAt = createdAt,
            capacity = capacity,
            fuelLiters = fuelLiters,
            phone = phone,
            label = label,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a transit time threshold in status requested. */
fun newTransitTimeThreshold(code: String, title: String): TransitTimeThreshold {
    return TransitTimeThreshold(
        code = code,
        title = title,
        description = "",
        createdAt = 0,
        capacity = 0,
        fuelLiters = 0.0,
        phone = "",
        label = "",
        notes = "",
        status = TransitTimeThresholdStatus.REQUESTED,
    )
}
