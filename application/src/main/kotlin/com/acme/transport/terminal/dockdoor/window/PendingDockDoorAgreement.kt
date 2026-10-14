package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending dock door agreement is in its life cycle. */
enum class PendingDockDoorAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending dock door agreement. Its code is unique within a tenant. */
data class PendingDockDoorAgreement(
    var code: String,
    var label: String,
    var capacity: Int,
    var name: String,
    var graceDays: Int,
    var shiftCode: String,
    var country: String,
    var title: String,
    var status: PendingDockDoorAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDockDoorAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDockDoorAgreementStatus): Boolean {
        when (status) {
            PendingDockDoorAgreementStatus.DRAFT -> return next == PendingDockDoorAgreementStatus.PENDING || next == PendingDockDoorAgreementStatus.ACTIVE
            PendingDockDoorAgreementStatus.PENDING -> return next == PendingDockDoorAgreementStatus.ACTIVE
            PendingDockDoorAgreementStatus.ACTIVE -> {
                when (next) {
                    PendingDockDoorAgreementStatus.SUSPENDED, PendingDockDoorAgreementStatus.CLOSED -> return true
                }
                return false
            }
            PendingDockDoorAgreementStatus.SUSPENDED -> {
                when (next) {
                    PendingDockDoorAgreementStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingDockDoorAgreementStatus) {
        if (!canMoveTo(next)) {
            error("pending dock door agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveValue(): Double = round((capacity.toDouble() + graceDays.toDouble()) * 100) / 100

    fun snapshot(): PendingDockDoorAgreement {
        return PendingDockDoorAgreement(
            code = code,
            label = label,
            capacity = capacity,
            name = name,
            graceDays = graceDays,
            shiftCode = shiftCode,
            country = country,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a pending dock door agreement in status draft. */
fun newPendingDockDoorAgreement(code: String, label: String): PendingDockDoorAgreement {
    return PendingDockDoorAgreement(
        code = code,
        label = label,
        capacity = 0,
        name = "",
        graceDays = 0,
        shiftCode = "",
        country = "",
        title = "",
        status = PendingDockDoorAgreementStatus.DRAFT,
    )
}
