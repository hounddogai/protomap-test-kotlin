package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge assignment is in its life cycle. */
enum class EvChargeAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a ev charge assignment and its current status. */
data class EvChargeAssignment(
    var code: String,
    var title: String,
    var notes: String,
    var budget: Double,
    var durationMinutes: Int,
    var weight: Double,
    var description: String,
    var dock: String,
    var version: Int,
    var quantity: Int,
    var status: EvChargeAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeAssignmentStatus): Boolean {
        when (status) {
            EvChargeAssignmentStatus.REQUESTED -> {
                when (next) {
                    EvChargeAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            EvChargeAssignmentStatus.GRANTED -> {
                when (next) {
                    EvChargeAssignmentStatus.REVOKED, EvChargeAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            EvChargeAssignmentStatus.REVOKED -> {
                when (next) {
                    EvChargeAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("ev charge assignment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedVolume(): Double = round(quantity.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): EvChargeAssignment {
        return EvChargeAssignment(
            code = code,
            title = title,
            notes = notes,
            budget = budget,
            durationMinutes = durationMinutes,
            weight = weight,
            description = description,
            dock = dock,
            version = version,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a ev charge assignment in status requested. */
fun newEvChargeAssignment(code: String, title: String): EvChargeAssignment {
    return EvChargeAssignment(
        code = code,
        title = title,
        notes = "",
        budget = 0.0,
        durationMinutes = 0,
        weight = 0.0,
        description = "",
        dock = "",
        version = 0,
        quantity = 0,
        status = EvChargeAssignmentStatus.REQUESTED,
    )
}
