package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered dwell group is in its life cycle. */
enum class TieredDwellGroupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tiered dwell group, with what the planners need to act on it. */
data class TieredDwellGroup(
    var code: String,
    var label: String,
    var cost: Double,
    var pinned: Boolean,
    var windowDays: Int,
    var notes: String,
    var ownerId: String,
    var expiresAt: Int,
    var currency: String,
    var country: String,
    var trailerId: String,
    var status: TieredDwellGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDwellGroupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDwellGroupStatus): Boolean {
        when (status) {
            TieredDwellGroupStatus.IDLE -> {
                when (next) {
                    TieredDwellGroupStatus.WARMING -> return true
                }
                return false
            }
            TieredDwellGroupStatus.WARMING -> {
                when (next) {
                    TieredDwellGroupStatus.STOPPED -> return true
                }
                return false
            }
            TieredDwellGroupStatus.READY -> return next == TieredDwellGroupStatus.STOPPED
            TieredDwellGroupStatus.DRAINING -> return next == TieredDwellGroupStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TieredDwellGroupStatus) {
        if (!canMoveTo(next)) {
            error("tiered dwell group $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netValue(): Double = round((expiresAt.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): TieredDwellGroup {
        return TieredDwellGroup(
            code = code,
            label = label,
            cost = cost,
            pinned = pinned,
            windowDays = windowDays,
            notes = notes,
            ownerId = ownerId,
            expiresAt = expiresAt,
            currency = currency,
            country = country,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered dwell group in status idle. */
fun newTieredDwellGroup(code: String, label: String): TieredDwellGroup {
    return TieredDwellGroup(
        code = code,
        label = label,
        cost = 0.0,
        pinned = false,
        windowDays = 0,
        notes = "",
        ownerId = "",
        expiresAt = 0,
        currency = "",
        country = "",
        trailerId = "",
        status = TieredDwellGroupStatus.IDLE,
    )
}
