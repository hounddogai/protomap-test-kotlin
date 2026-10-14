package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.kit.text.normalize

// The dock door agreement records of terminal's dock door work, and the rules, stores, and calculations
// around them.

/** Where a dock door agreement is in its life cycle. */
enum class DockDoorAgreementStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A dock door agreement as the dock door team keeps it. */
data class DockDoorAgreement(
    var code: String,
    var name: String,
    var revision: Int,
    var region: String,
    var dock: String,
    var routeCode: String,
    var status: DockDoorAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorAgreementStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorAgreementStatus): Boolean {
        when (status) {
            DockDoorAgreementStatus.PROPOSED -> {
                when (next) {
                    DockDoorAgreementStatus.APPROVED, DockDoorAgreementStatus.WITHDRAWN -> return true
                }
                return false
            }
            DockDoorAgreementStatus.APPROVED -> return next == DockDoorAgreementStatus.REJECTED || next == DockDoorAgreementStatus.WITHDRAWN
            DockDoorAgreementStatus.REJECTED -> return next == DockDoorAgreementStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DockDoorAgreementStatus) {
        if (!canMoveTo(next)) {
            error("dock door agreement $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): DockDoorAgreement {
        return DockDoorAgreement(
            code = code,
            name = name,
            revision = revision,
            region = region,
            dock = dock,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a dock door agreement in status proposed. */
fun newDockDoorAgreement(code: String, name: String): DockDoorAgreement {
    return DockDoorAgreement(
        code = code,
        name = name,
        revision = 0,
        region = "",
        dock = "",
        routeCode = "",
        status = DockDoorAgreementStatus.PROPOSED,
    )
}
