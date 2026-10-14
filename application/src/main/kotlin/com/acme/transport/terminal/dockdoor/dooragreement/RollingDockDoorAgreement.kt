package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.kit.text.normalize

// The rolling dock door agreement records of terminal's dock door work, and the rules, stores, and
// calculations around them.

/** Where a rolling dock door agreement is in its life cycle. */
enum class RollingDockDoorAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One rolling dock door agreement, with what the planners need to act on it. */
data class RollingDockDoorAgreement(
    var code: String,
    var title: String,
    var visible: Boolean,
    var phone: String,
    var contactEmail: String,
    var depot: String,
    var externalRef: String,
    var floorValue: Double,
    var label: String,
    var status: RollingDockDoorAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDockDoorAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDockDoorAgreementStatus): Boolean {
        when (status) {
            RollingDockDoorAgreementStatus.IDLE -> return next == RollingDockDoorAgreementStatus.READY || next == RollingDockDoorAgreementStatus.STOPPED
            RollingDockDoorAgreementStatus.WARMING -> return next == RollingDockDoorAgreementStatus.READY || next == RollingDockDoorAgreementStatus.STOPPED
            RollingDockDoorAgreementStatus.READY -> {
                when (next) {
                    RollingDockDoorAgreementStatus.DRAINING, RollingDockDoorAgreementStatus.STOPPED -> return true
                }
                return false
            }
            RollingDockDoorAgreementStatus.DRAINING -> {
                when (next) {
                    RollingDockDoorAgreementStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingDockDoorAgreementStatus) {
        if (!canMoveTo(next)) {
            error("rolling dock door agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun measuredValue(): Double = floorValue

    fun snapshot(): RollingDockDoorAgreement {
        return RollingDockDoorAgreement(
            code = code,
            title = title,
            visible = visible,
            phone = phone,
            contactEmail = contactEmail,
            depot = depot,
            externalRef = externalRef,
            floorValue = floorValue,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rolling dock door agreement in status idle. */
fun newRollingDockDoorAgreement(code: String, title: String): RollingDockDoorAgreement {
    return RollingDockDoorAgreement(
        code = code,
        title = title,
        visible = false,
        phone = "",
        contactEmail = "",
        depot = "",
        externalRef = "",
        floorValue = 0.0,
        label = "",
        status = RollingDockDoorAgreementStatus.IDLE,
    )
}
