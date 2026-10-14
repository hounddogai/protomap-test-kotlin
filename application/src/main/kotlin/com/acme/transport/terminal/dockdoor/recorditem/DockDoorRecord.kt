package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dock door record records of terminal's dock door work, and the rules, stores, and calculations around
// them.

/** Where a dock door record is in its life cycle. */
enum class DockDoorRecordStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a dock door record and its current status. */
data class DockDoorRecord(
    var code: String,
    var title: String,
    var sequence: Int,
    var country: String,
    var routeCode: String,
    var score: Double,
    var distanceKm: Double,
    var quantity: Int,
    var fuelLiters: Double,
    var status: DockDoorRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorRecordStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorRecordStatus): Boolean {
        when (status) {
            DockDoorRecordStatus.DRAFT -> {
                when (next) {
                    DockDoorRecordStatus.SUSPENDED -> return true
                }
                return false
            }
            DockDoorRecordStatus.PENDING -> return next == DockDoorRecordStatus.ACTIVE || next == DockDoorRecordStatus.SUSPENDED
            DockDoorRecordStatus.ACTIVE -> return next == DockDoorRecordStatus.SUSPENDED
            DockDoorRecordStatus.SUSPENDED -> {
                when (next) {
                    DockDoorRecordStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorRecordStatus) {
        if (!canMoveTo(next)) {
            error("dock door record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedExposure(): Double = round((quantity.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): DockDoorRecord {
        return DockDoorRecord(
            code = code,
            title = title,
            sequence = sequence,
            country = country,
            routeCode = routeCode,
            score = score,
            distanceKm = distanceKm,
            quantity = quantity,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a dock door record in status draft. */
fun newDockDoorRecord(code: String, title: String): DockDoorRecord {
    return DockDoorRecord(
        code = code,
        title = title,
        sequence = 0,
        country = "",
        routeCode = "",
        score = 0.0,
        distanceKm = 0.0,
        quantity = 0,
        fuelLiters = 0.0,
        status = DockDoorRecordStatus.DRAFT,
    )
}
