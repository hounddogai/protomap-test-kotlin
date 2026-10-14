package com.acme.transport.network.zone.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone tier records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone tier is in its life cycle. */
enum class ZoneTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a zone tier and its current status. */
data class ZoneTier(
    var code: String,
    var label: String,
    var segment: String,
    var attempts: Int,
    var quantity: Int,
    var visible: Boolean,
    var summary: String,
    var driverId: String,
    var trailerId: String,
    var status: ZoneTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneTierStatus): Boolean {
        when (status) {
            ZoneTierStatus.IDLE -> {
                when (next) {
                    ZoneTierStatus.DRAINING -> return true
                }
                return false
            }
            ZoneTierStatus.WARMING -> {
                when (next) {
                    ZoneTierStatus.READY -> return true
                }
                return false
            }
            ZoneTierStatus.READY -> return next == ZoneTierStatus.DRAINING || next == ZoneTierStatus.STOPPED
            ZoneTierStatus.DRAINING -> {
                when (next) {
                    ZoneTierStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneTierStatus) {
        if (!canMoveTo(next)) {
            error("zone tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"zone tier"} $code"

    fun weightedLoad(): Double = round((quantity.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): ZoneTier {
        return ZoneTier(
            code = code,
            label = label,
            segment = segment,
            attempts = attempts,
            quantity = quantity,
            visible = visible,
            summary = summary,
            driverId = driverId,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a zone tier in status idle. */
fun newZoneTier(code: String, label: String): ZoneTier {
    return ZoneTier(
        code = code,
        label = label,
        segment = "",
        attempts = 0,
        quantity = 0,
        visible = false,
        summary = "",
        driverId = "",
        trailerId = "",
        status = ZoneTierStatus.IDLE,
    )
}
