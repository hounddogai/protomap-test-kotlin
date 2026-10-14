package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door profile is in its life cycle. */
enum class DockDoorProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a dock door profile and its current status. */
data class DockDoorProfile(
    var code: String,
    var label: String,
    var attempts: Int,
    var checksum: String,
    var summary: String,
    var limit: Int,
    var category: String,
    var amount: Double,
    var rate: Double,
    var pallets: Int,
    var loadKg: Double,
    var status: DockDoorProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorProfileStatus): Boolean {
        when (status) {
            DockDoorProfileStatus.DRAFT -> return next == DockDoorProfileStatus.SUSPENDED
            DockDoorProfileStatus.PENDING -> return next == DockDoorProfileStatus.CLOSED
            DockDoorProfileStatus.ACTIVE -> return next == DockDoorProfileStatus.SUSPENDED || next == DockDoorProfileStatus.CLOSED
            DockDoorProfileStatus.SUSPENDED -> return next == DockDoorProfileStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DockDoorProfileStatus) {
        if (!canMoveTo(next)) {
            error("dock door profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"dock door profile"} $code"

    fun projectedVolume(): Double = round(pallets.toDouble() * rate * 100) / 100

    fun snapshot(): DockDoorProfile {
        return DockDoorProfile(
            code = code,
            label = label,
            attempts = attempts,
            checksum = checksum,
            summary = summary,
            limit = limit,
            category = category,
            amount = amount,
            rate = rate,
            pallets = pallets,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a dock door profile in status draft. */
fun newDockDoorProfile(code: String, label: String): DockDoorProfile {
    return DockDoorProfile(
        code = code,
        label = label,
        attempts = 0,
        checksum = "",
        summary = "",
        limit = 0,
        category = "",
        amount = 0.0,
        rate = 0.0,
        pallets = 0,
        loadKg = 0.0,
        status = DockDoorProfileStatus.DRAFT,
    )
}
