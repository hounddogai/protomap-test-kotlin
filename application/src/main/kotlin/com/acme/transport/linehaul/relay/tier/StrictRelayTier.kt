package com.acme.transport.linehaul.relay.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict relay tier records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a strict relay tier is in its life cycle. */
enum class StrictRelayTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a strict relay tier and its current status. */
data class StrictRelayTier(
    var code: String,
    var label: String,
    var country: String,
    var pinned: Boolean,
    var shiftCode: String,
    var sequence: Int,
    var distanceKm: Double,
    var batchSize: Int,
    var locale: String,
    var retries: Int,
    var status: StrictRelayTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRelayTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRelayTierStatus): Boolean {
        when (status) {
            StrictRelayTierStatus.NEW -> {
                when (next) {
                    StrictRelayTierStatus.FAILED -> return true
                }
                return false
            }
            StrictRelayTierStatus.QUEUED -> return next == StrictRelayTierStatus.RUNNING || next == StrictRelayTierStatus.FAILED
            StrictRelayTierStatus.RUNNING -> {
                when (next) {
                    StrictRelayTierStatus.DONE -> return true
                }
                return false
            }
            StrictRelayTierStatus.DONE -> return next == StrictRelayTierStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictRelayTierStatus) {
        if (!canMoveTo(next)) {
            error("strict relay tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveExposure(): Double = round((sequence.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): StrictRelayTier {
        return StrictRelayTier(
            code = code,
            label = label,
            country = country,
            pinned = pinned,
            shiftCode = shiftCode,
            sequence = sequence,
            distanceKm = distanceKm,
            batchSize = batchSize,
            locale = locale,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict relay tier in status new. */
fun newStrictRelayTier(code: String, label: String): StrictRelayTier {
    return StrictRelayTier(
        code = code,
        label = label,
        country = "",
        pinned = false,
        shiftCode = "",
        sequence = 0,
        distanceKm = 0.0,
        batchSize = 0,
        locale = "",
        retries = 0,
        status = StrictRelayTierStatus.NEW,
    )
}
