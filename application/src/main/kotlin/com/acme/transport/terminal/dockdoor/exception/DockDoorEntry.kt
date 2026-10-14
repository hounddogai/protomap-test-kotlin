package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door entry is in its life cycle. */
enum class DockDoorEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A dock door entry as the dock door team keeps it. */
data class DockDoorEntry(
    var code: String,
    var label: String,
    var retries: Int,
    var maximum: Double,
    var externalRef: String,
    var endsAt: Int,
    var locale: String,
    var distanceKm: Double,
    var carrier: String,
    var driverId: String,
    var status: DockDoorEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorEntryStatus): Boolean {
        when (status) {
            DockDoorEntryStatus.NEW -> return next == DockDoorEntryStatus.QUEUED
            DockDoorEntryStatus.QUEUED -> return next == DockDoorEntryStatus.RUNNING
            DockDoorEntryStatus.RUNNING -> {
                when (next) {
                    DockDoorEntryStatus.DONE, DockDoorEntryStatus.FAILED -> return true
                }
                return false
            }
            DockDoorEntryStatus.DONE -> {
                when (next) {
                    DockDoorEntryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorEntryStatus) {
        if (!canMoveTo(next)) {
            error("dock door entry $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netLoad(): Double = round(retries.toDouble() * distanceKm * 100) / 100

    fun snapshot(): DockDoorEntry {
        return DockDoorEntry(
            code = code,
            label = label,
            retries = retries,
            maximum = maximum,
            externalRef = externalRef,
            endsAt = endsAt,
            locale = locale,
            distanceKm = distanceKm,
            carrier = carrier,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a dock door entry in status new. */
fun newDockDoorEntry(code: String, label: String): DockDoorEntry {
    return DockDoorEntry(
        code = code,
        label = label,
        retries = 0,
        maximum = 0.0,
        externalRef = "",
        endsAt = 0,
        locale = "",
        distanceKm = 0.0,
        carrier = "",
        driverId = "",
        status = DockDoorEntryStatus.NEW,
    )
}
