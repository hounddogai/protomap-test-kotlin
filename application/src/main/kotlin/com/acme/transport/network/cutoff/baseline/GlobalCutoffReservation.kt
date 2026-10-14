package com.acme.transport.network.cutoff.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global cutoff reservation is in its life cycle. */
enum class GlobalCutoffReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global cutoff reservation. Its code is unique within a tenant. */
data class GlobalCutoffReservation(
    var code: String,
    var name: String,
    var graceDays: Int,
    var attempts: Int,
    var retries: Int,
    var status: GlobalCutoffReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCutoffReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCutoffReservationStatus): Boolean {
        when (status) {
            GlobalCutoffReservationStatus.IDLE -> return next == GlobalCutoffReservationStatus.DRAINING
            GlobalCutoffReservationStatus.WARMING -> return next == GlobalCutoffReservationStatus.READY || next == GlobalCutoffReservationStatus.STOPPED
            GlobalCutoffReservationStatus.READY -> {
                when (next) {
                    GlobalCutoffReservationStatus.DRAINING, GlobalCutoffReservationStatus.STOPPED -> return true
                }
                return false
            }
            GlobalCutoffReservationStatus.DRAINING -> {
                when (next) {
                    GlobalCutoffReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCutoffReservationStatus) {
        if (!canMoveTo(next)) {
            error("global cutoff reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round((attempts.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): GlobalCutoffReservation {
        return GlobalCutoffReservation(
            code = code,
            name = name,
            graceDays = graceDays,
            attempts = attempts,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global cutoff reservation in status idle. */
fun newGlobalCutoffReservation(code: String, name: String): GlobalCutoffReservation {
    return GlobalCutoffReservation(
        code = code,
        name = name,
        graceDays = 0,
        attempts = 0,
        retries = 0,
        status = GlobalCutoffReservationStatus.IDLE,
    )
}
