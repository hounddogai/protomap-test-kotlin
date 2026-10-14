package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The freight invoice preference records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a freight invoice preference is in its life cycle. */
enum class FreightInvoicePreferenceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a freight invoice preference and its current status. */
data class FreightInvoicePreference(
    var code: String,
    var label: String,
    var description: String,
    var endsAt: Int,
    var amount: Double,
    var approved: Boolean,
    var phone: String,
    var ownerId: String,
    var cost: Double,
    var minimum: Double,
    var status: FreightInvoicePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoicePreferenceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoicePreferenceStatus): Boolean {
        when (status) {
            FreightInvoicePreferenceStatus.CREATED -> return next == FreightInvoicePreferenceStatus.PUBLISHED || next == FreightInvoicePreferenceStatus.RETIRED
            FreightInvoicePreferenceStatus.VALIDATED -> return next == FreightInvoicePreferenceStatus.RETIRED
            FreightInvoicePreferenceStatus.PUBLISHED -> return next == FreightInvoicePreferenceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FreightInvoicePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice preference $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"freight invoice preference"} $code"

    fun netVolume(): Double = round((endsAt.toDouble() + cost) * 100) / 100

    fun snapshot(): FreightInvoicePreference {
        return FreightInvoicePreference(
            code = code,
            label = label,
            description = description,
            endsAt = endsAt,
            amount = amount,
            approved = approved,
            phone = phone,
            ownerId = ownerId,
            cost = cost,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a freight invoice preference in status created. */
fun newFreightInvoicePreference(code: String, label: String): FreightInvoicePreference {
    return FreightInvoicePreference(
        code = code,
        label = label,
        description = "",
        endsAt = 0,
        amount = 0.0,
        approved = false,
        phone = "",
        ownerId = "",
        cost = 0.0,
        minimum = 0.0,
        status = FreightInvoicePreferenceStatus.CREATED,
    )
}
