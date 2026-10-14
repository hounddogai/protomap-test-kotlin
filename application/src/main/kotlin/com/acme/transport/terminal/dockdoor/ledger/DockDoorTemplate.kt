package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door template is in its life cycle. */
enum class DockDoorTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a dock door template and its current status. */
data class DockDoorTemplate(
    var code: String,
    var label: String,
    var country: String,
    var fuelLiters: Double,
    var depot: String,
    var maximum: Double,
    var status: DockDoorTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorTemplateStatus): Boolean {
        when (status) {
            DockDoorTemplateStatus.DRAFT -> return next == DockDoorTemplateStatus.ACTIVE || next == DockDoorTemplateStatus.SUSPENDED
            DockDoorTemplateStatus.PENDING -> {
                when (next) {
                    DockDoorTemplateStatus.CLOSED -> return true
                }
                return false
            }
            DockDoorTemplateStatus.ACTIVE -> {
                when (next) {
                    DockDoorTemplateStatus.SUSPENDED -> return true
                }
                return false
            }
            DockDoorTemplateStatus.SUSPENDED -> return next == DockDoorTemplateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DockDoorTemplateStatus) {
        if (!canMoveTo(next)) {
            error("dock door template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"dock door template"} $code"

    fun weightedVolume(): Double = round((maximum + fuelLiters) * 100) / 100

    fun snapshot(): DockDoorTemplate {
        return DockDoorTemplate(
            code = code,
            label = label,
            country = country,
            fuelLiters = fuelLiters,
            depot = depot,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a dock door template in status draft. */
fun newDockDoorTemplate(code: String, label: String): DockDoorTemplate {
    return DockDoorTemplate(
        code = code,
        label = label,
        country = "",
        fuelLiters = 0.0,
        depot = "",
        maximum = 0.0,
        status = DockDoorTemplateStatus.DRAFT,
    )
}
