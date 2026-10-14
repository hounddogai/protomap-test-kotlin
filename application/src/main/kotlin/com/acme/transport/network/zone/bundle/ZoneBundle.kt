package com.acme.transport.network.zone.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone bundle records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone bundle is in its life cycle. */
enum class ZoneBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a zone bundle and its current status. */
data class ZoneBundle(
    var code: String,
    var title: String,
    var trailerId: String,
    var lane: String,
    var channel: String,
    var label: String,
    var amount: Double,
    var phone: String,
    var minimum: Double,
    var status: ZoneBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneBundleStatus): Boolean {
        when (status) {
            ZoneBundleStatus.NEW -> {
                when (next) {
                    ZoneBundleStatus.RUNNING -> return true
                }
                return false
            }
            ZoneBundleStatus.QUEUED -> {
                when (next) {
                    ZoneBundleStatus.DONE, ZoneBundleStatus.FAILED -> return true
                }
                return false
            }
            ZoneBundleStatus.RUNNING -> return next == ZoneBundleStatus.FAILED
            ZoneBundleStatus.DONE -> {
                when (next) {
                    ZoneBundleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneBundleStatus) {
        if (!canMoveTo(next)) {
            error("zone bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"zone bundle"} $code"

    fun netTotal(): Double = round((minimum + amount) * 100) / 100

    fun snapshot(): ZoneBundle {
        return ZoneBundle(
            code = code,
            title = title,
            trailerId = trailerId,
            lane = lane,
            channel = channel,
            label = label,
            amount = amount,
            phone = phone,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a zone bundle in status new. */
fun newZoneBundle(code: String, title: String): ZoneBundle {
    return ZoneBundle(
        code = code,
        title = title,
        trailerId = "",
        lane = "",
        channel = "",
        label = "",
        amount = 0.0,
        phone = "",
        minimum = 0.0,
        status = ZoneBundleStatus.NEW,
    )
}
