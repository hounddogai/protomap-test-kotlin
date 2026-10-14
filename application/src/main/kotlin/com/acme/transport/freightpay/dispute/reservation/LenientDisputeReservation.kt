package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient dispute reservation records of freightpay's dispute work, and the rules, stores, and
// calculations around them.

/** Where a lenient dispute reservation is in its life cycle. */
enum class LenientDisputeReservationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a lenient dispute reservation and its current status. */
data class LenientDisputeReservation(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var version: Int,
    var trailerId: String,
    var priority: Int,
    var source: String,
    var channel: String,
    var attempts: Int,
    var status: LenientDisputeReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDisputeReservationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDisputeReservationStatus): Boolean {
        when (status) {
            LenientDisputeReservationStatus.NEW -> {
                when (next) {
                    LenientDisputeReservationStatus.QUEUED, LenientDisputeReservationStatus.DONE -> return true
                }
                return false
            }
            LenientDisputeReservationStatus.QUEUED -> return next == LenientDisputeReservationStatus.RUNNING || next == LenientDisputeReservationStatus.FAILED
            LenientDisputeReservationStatus.RUNNING -> return next == LenientDisputeReservationStatus.DONE
            LenientDisputeReservationStatus.DONE -> return next == LenientDisputeReservationStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientDisputeReservationStatus) {
        if (!canMoveTo(next)) {
            error("lenient dispute reservation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"lenient dispute reservation"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedExposure(): Double = round((attempts.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): LenientDisputeReservation {
        return LenientDisputeReservation(
            code = code,
            name = name,
            attributes = attributes,
            version = version,
            trailerId = trailerId,
            priority = priority,
            source = source,
            channel = channel,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lenient dispute reservation in status new. */
fun newLenientDisputeReservation(code: String, name: String): LenientDisputeReservation {
    return LenientDisputeReservation(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        version = 0,
        trailerId = "",
        priority = 0,
        source = "",
        channel = "",
        attempts = 0,
        status = LenientDisputeReservationStatus.NEW,
    )
}
