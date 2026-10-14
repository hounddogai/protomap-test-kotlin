package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dwell mapping records of terminal's dwell work, and the rules, stores, and calculations around them.

/** Where a dwell mapping is in its life cycle. */
enum class DwellMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a dwell mapping and its current status. */
data class DwellMapping(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var durationMinutes: Int,
    var label: String,
    var odometer: Int,
    var notes: String,
    var phone: String,
    var depot: String,
    var archived: Boolean,
    var status: DwellMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellMappingStatus): Boolean {
        when (status) {
            DwellMappingStatus.NEW -> {
                when (next) {
                    DwellMappingStatus.DONE -> return true
                }
                return false
            }
            DwellMappingStatus.QUEUED -> return next == DwellMappingStatus.DONE || next == DwellMappingStatus.FAILED
            DwellMappingStatus.RUNNING -> {
                when (next) {
                    DwellMappingStatus.FAILED -> return true
                }
                return false
            }
            DwellMappingStatus.DONE -> return next == DwellMappingStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DwellMappingStatus) {
        if (!canMoveTo(next)) {
            error("dwell mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun weightedLoad(): Double = round((odometer.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): DwellMapping {
        return DwellMapping(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            durationMinutes = durationMinutes,
            label = label,
            odometer = odometer,
            notes = notes,
            phone = phone,
            depot = depot,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a dwell mapping in status new. */
fun newDwellMapping(code: String, title: String): DwellMapping {
    return DwellMapping(
        code = code,
        title = title,
        fuelLiters = 0.0,
        durationMinutes = 0,
        label = "",
        odometer = 0,
        notes = "",
        phone = "",
        depot = "",
        archived = false,
        status = DwellMappingStatus.NEW,
    )
}
