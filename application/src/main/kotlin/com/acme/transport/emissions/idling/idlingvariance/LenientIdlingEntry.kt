package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient idling entry is in its life cycle. */
enum class LenientIdlingEntryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One lenient idling entry, with what the planners need to act on it. */
data class LenientIdlingEntry(
    var code: String,
    var title: String,
    var maximum: Double,
    var vehicleId: String,
    var limit: Int,
    var contactEmail: String,
    var score: Double,
    var driverId: String,
    var name: String,
    var updatedAt: Int,
    var status: LenientIdlingEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientIdlingEntryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientIdlingEntryStatus): Boolean {
        when (status) {
            LenientIdlingEntryStatus.IDLE -> {
                when (next) {
                    LenientIdlingEntryStatus.READY, LenientIdlingEntryStatus.DRAINING -> return true
                }
                return false
            }
            LenientIdlingEntryStatus.WARMING -> return next == LenientIdlingEntryStatus.READY || next == LenientIdlingEntryStatus.STOPPED
            LenientIdlingEntryStatus.READY -> {
                when (next) {
                    LenientIdlingEntryStatus.DRAINING, LenientIdlingEntryStatus.STOPPED -> return true
                }
                return false
            }
            LenientIdlingEntryStatus.DRAINING -> {
                when (next) {
                    LenientIdlingEntryStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientIdlingEntryStatus) {
        if (!canMoveTo(next)) {
            error("lenient idling entry $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedTotal(): Double = round((maximum - limit.toDouble()) * 100) / 100

    fun snapshot(): LenientIdlingEntry {
        return LenientIdlingEntry(
            code = code,
            title = title,
            maximum = maximum,
            vehicleId = vehicleId,
            limit = limit,
            contactEmail = contactEmail,
            score = score,
            driverId = driverId,
            name = name,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient idling entry in status idle. */
fun newLenientIdlingEntry(code: String, title: String): LenientIdlingEntry {
    return LenientIdlingEntry(
        code = code,
        title = title,
        maximum = 0.0,
        vehicleId = "",
        limit = 0,
        contactEmail = "",
        score = 0.0,
        driverId = "",
        name = "",
        updatedAt = 0,
        status = LenientIdlingEntryStatus.IDLE,
    )
}
