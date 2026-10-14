package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending cutoff score is in its life cycle. */
enum class PendingCutoffScoreStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a pending cutoff score and its current status. */
data class PendingCutoffScore(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var contactEmail: String,
    var locale: String,
    var rate: Double,
    var name: String,
    var ownerId: String,
    var distanceKm: Double,
    var status: PendingCutoffScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCutoffScoreStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCutoffScoreStatus): Boolean {
        when (status) {
            PendingCutoffScoreStatus.IDLE -> {
                when (next) {
                    PendingCutoffScoreStatus.READY -> return true
                }
                return false
            }
            PendingCutoffScoreStatus.WARMING -> return next == PendingCutoffScoreStatus.READY || next == PendingCutoffScoreStatus.DRAINING
            PendingCutoffScoreStatus.READY -> return next == PendingCutoffScoreStatus.DRAINING
            PendingCutoffScoreStatus.DRAINING -> {
                when (next) {
                    PendingCutoffScoreStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingCutoffScoreStatus) {
        if (!canMoveTo(next)) {
            error("pending cutoff score $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun netVolume(): Double = round(updatedAt.toDouble() * distanceKm * 100) / 100

    fun snapshot(): PendingCutoffScore {
        return PendingCutoffScore(
            code = code,
            label = label,
            updatedAt = updatedAt,
            contactEmail = contactEmail,
            locale = locale,
            rate = rate,
            name = name,
            ownerId = ownerId,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a pending cutoff score in status idle. */
fun newPendingCutoffScore(code: String, label: String): PendingCutoffScore {
    return PendingCutoffScore(
        code = code,
        label = label,
        updatedAt = 0,
        contactEmail = "",
        locale = "",
        rate = 0.0,
        name = "",
        ownerId = "",
        distanceKm = 0.0,
        status = PendingCutoffScoreStatus.IDLE,
    )
}
