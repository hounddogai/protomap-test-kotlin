package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door audit is in its life cycle. */
enum class DockDoorAuditStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a dock door audit and its current status. */
data class DockDoorAudit(
    var code: String,
    var name: String,
    var version: Int,
    var cost: Double,
    var fuelLiters: Double,
    var status: DockDoorAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorAuditStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorAuditStatus): Boolean {
        when (status) {
            DockDoorAuditStatus.REQUESTED -> return next == DockDoorAuditStatus.REVOKED || next == DockDoorAuditStatus.EXPIRED
            DockDoorAuditStatus.GRANTED -> return next == DockDoorAuditStatus.REVOKED || next == DockDoorAuditStatus.EXPIRED
            DockDoorAuditStatus.REVOKED -> {
                when (next) {
                    DockDoorAuditStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorAuditStatus) {
        if (!canMoveTo(next)) {
            error("dock door audit $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun adjustedTotal(): Double = round(fuelLiters * version.toDouble() * 100) / 100

    fun snapshot(): DockDoorAudit {
        return DockDoorAudit(
            code = code,
            name = name,
            version = version,
            cost = cost,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dock door audit in status requested. */
fun newDockDoorAudit(code: String, name: String): DockDoorAudit {
    return DockDoorAudit(
        code = code,
        name = name,
        version = 0,
        cost = 0.0,
        fuelLiters = 0.0,
        status = DockDoorAuditStatus.REQUESTED,
    )
}
