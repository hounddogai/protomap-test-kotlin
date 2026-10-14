package com.acme.transport.network.hub.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hub agreement records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub agreement is in its life cycle. */
enum class HubAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A hub agreement as the hub team keeps it. */
data class HubAgreement(
    var code: String,
    var label: String,
    var startsAt: Int,
    var segment: String,
    var amount: Double,
    var minimum: Double,
    var revision: Int,
    var odometer: Int,
    var discount: Double,
    var description: String,
    var status: HubAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubAgreementStatus): Boolean {
        when (status) {
            HubAgreementStatus.DRAFT -> return next == HubAgreementStatus.SUSPENDED || next == HubAgreementStatus.CLOSED
            HubAgreementStatus.PENDING -> return next == HubAgreementStatus.CLOSED
            HubAgreementStatus.ACTIVE -> return next == HubAgreementStatus.SUSPENDED
            HubAgreementStatus.SUSPENDED -> return next == HubAgreementStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: HubAgreementStatus) {
        if (!canMoveTo(next)) {
            error("hub agreement $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedValue(): Double = round((startsAt.toDouble() - minimum) * 100) / 100

    fun snapshot(): HubAgreement {
        return HubAgreement(
            code = code,
            label = label,
            startsAt = startsAt,
            segment = segment,
            amount = amount,
            minimum = minimum,
            revision = revision,
            odometer = odometer,
            discount = discount,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a hub agreement in status draft. */
fun newHubAgreement(code: String, label: String): HubAgreement {
    return HubAgreement(
        code = code,
        label = label,
        startsAt = 0,
        segment = "",
        amount = 0.0,
        minimum = 0.0,
        revision = 0,
        odometer = 0,
        discount = 0.0,
        description = "",
        status = HubAgreementStatus.DRAFT,
    )
}
