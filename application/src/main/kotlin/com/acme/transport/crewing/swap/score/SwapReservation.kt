package com.acme.transport.crewing.swap.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap reservation is in its life cycle. */
enum class SwapReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A swap reservation as the swap team keeps it. */
data class SwapReservation(
    var code: String,
    var label: String,
    var graceDays: Int,
    var revision: Int,
    var updatedAt: Int,
    var locale: String,
    var visible: Boolean,
    var attributes: MutableMap<String, String>,
    var checksum: String,
    var odometer: Int,
    var status: SwapReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapReservationStatus): Boolean {
        when (status) {
            SwapReservationStatus.IDLE -> return next == SwapReservationStatus.DRAINING || next == SwapReservationStatus.STOPPED
            SwapReservationStatus.WARMING -> {
                when (next) {
                    SwapReservationStatus.READY, SwapReservationStatus.DRAINING -> return true
                }
                return false
            }
            SwapReservationStatus.READY -> return next == SwapReservationStatus.DRAINING || next == SwapReservationStatus.STOPPED
            SwapReservationStatus.DRAINING -> {
                when (next) {
                    SwapReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapReservationStatus) {
        if (!canMoveTo(next)) {
            error("swap reservation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedTotal(): Double = round((revision.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): SwapReservation {
        return SwapReservation(
            code = code,
            label = label,
            graceDays = graceDays,
            revision = revision,
            updatedAt = updatedAt,
            locale = locale,
            visible = visible,
            attributes = attributes,
            checksum = checksum,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a swap reservation in status idle. */
fun newSwapReservation(code: String, label: String): SwapReservation {
    return SwapReservation(
        code = code,
        label = label,
        graceDays = 0,
        revision = 0,
        updatedAt = 0,
        locale = "",
        visible = false,
        attributes = mutableMapOf<String, String>(),
        checksum = "",
        odometer = 0,
        status = SwapReservationStatus.IDLE,
    )
}
