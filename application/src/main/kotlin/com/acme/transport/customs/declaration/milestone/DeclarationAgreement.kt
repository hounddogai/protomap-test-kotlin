package com.acme.transport.customs.declaration.milestone

import com.acme.transport.kit.text.normalize

/** Where a declaration agreement is in its life cycle. */
enum class DeclarationAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a declaration agreement and its current status. */
data class DeclarationAgreement(
    var code: String,
    var title: String,
    var channel: String,
    var vehicleId: String,
    var reviewer: String,
    var externalRef: String,
    var active: Boolean,
    var ownerId: String,
    var summary: String,
    var amount: Double,
    var status: DeclarationAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationAgreementStatus): Boolean {
        when (status) {
            DeclarationAgreementStatus.DRAFT -> {
                when (next) {
                    DeclarationAgreementStatus.CLOSED -> return true
                }
                return false
            }
            DeclarationAgreementStatus.PENDING -> {
                when (next) {
                    DeclarationAgreementStatus.SUSPENDED -> return true
                }
                return false
            }
            DeclarationAgreementStatus.ACTIVE -> return next == DeclarationAgreementStatus.SUSPENDED || next == DeclarationAgreementStatus.CLOSED
            DeclarationAgreementStatus.SUSPENDED -> {
                when (next) {
                    DeclarationAgreementStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationAgreementStatus) {
        if (!canMoveTo(next)) {
            error("declaration agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = amount

    fun snapshot(): DeclarationAgreement {
        return DeclarationAgreement(
            code = code,
            title = title,
            channel = channel,
            vehicleId = vehicleId,
            reviewer = reviewer,
            externalRef = externalRef,
            active = active,
            ownerId = ownerId,
            summary = summary,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a declaration agreement in status draft. */
fun newDeclarationAgreement(code: String, title: String): DeclarationAgreement {
    return DeclarationAgreement(
        code = code,
        title = title,
        channel = "",
        vehicleId = "",
        reviewer = "",
        externalRef = "",
        active = false,
        ownerId = "",
        summary = "",
        amount = 0.0,
        status = DeclarationAgreementStatus.DRAFT,
    )
}
