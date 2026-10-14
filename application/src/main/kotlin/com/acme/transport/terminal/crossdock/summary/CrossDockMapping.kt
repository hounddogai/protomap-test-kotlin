package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock mapping is in its life cycle. */
enum class CrossDockMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A cross dock mapping as the cross dock team keeps it. */
data class CrossDockMapping(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var approved: Boolean,
    var discount: Double,
    var unit: String,
    var graceDays: Int,
    var cost: Double,
    var status: CrossDockMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockMappingStatus): Boolean {
        when (status) {
            CrossDockMappingStatus.PLANNED -> return next == CrossDockMappingStatus.SCHEDULED || next == CrossDockMappingStatus.COMPLETED
            CrossDockMappingStatus.SCHEDULED -> {
                when (next) {
                    CrossDockMappingStatus.IN_PROGRESS, CrossDockMappingStatus.CANCELLED -> return true
                }
                return false
            }
            CrossDockMappingStatus.IN_PROGRESS -> return next == CrossDockMappingStatus.COMPLETED || next == CrossDockMappingStatus.CANCELLED
            CrossDockMappingStatus.COMPLETED -> {
                when (next) {
                    CrossDockMappingStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockMappingStatus) {
        if (!canMoveTo(next)) {
            error("cross dock mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netValue(): Double = round(cost * discount * 100) / 100

    fun snapshot(): CrossDockMapping {
        return CrossDockMapping(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            approved = approved,
            discount = discount,
            unit = unit,
            graceDays = graceDays,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a cross dock mapping in status planned. */
fun newCrossDockMapping(code: String, title: String): CrossDockMapping {
    return CrossDockMapping(
        code = code,
        title = title,
        durationMinutes = 0,
        approved = false,
        discount = 0.0,
        unit = "",
        graceDays = 0,
        cost = 0.0,
        status = CrossDockMappingStatus.PLANNED,
    )
}
