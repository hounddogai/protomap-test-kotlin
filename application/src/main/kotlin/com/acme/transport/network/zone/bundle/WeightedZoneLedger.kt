package com.acme.transport.network.zone.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted zone ledger is in its life cycle. */
enum class WeightedZoneLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted zone ledger. Its code is unique within a tenant. */
data class WeightedZoneLedger(
    var code: String,
    var title: String,
    var sequence: Int,
    var createdAt: Int,
    var fuelLiters: Double,
    var contactEmail: String,
    var locale: String,
    var quantity: Int,
    var currency: String,
    var shiftCode: String,
    var label: String,
    var status: WeightedZoneLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedZoneLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedZoneLedgerStatus): Boolean {
        when (status) {
            WeightedZoneLedgerStatus.OPEN -> {
                when (next) {
                    WeightedZoneLedgerStatus.IN_REVIEW -> return true
                }
                return false
            }
            WeightedZoneLedgerStatus.IN_REVIEW -> {
                when (next) {
                    WeightedZoneLedgerStatus.RESOLVED, WeightedZoneLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedZoneLedgerStatus.RESOLVED -> return next == WeightedZoneLedgerStatus.REOPENED
            WeightedZoneLedgerStatus.REOPENED -> {
                when (next) {
                    WeightedZoneLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedZoneLedgerStatus) {
        if (!canMoveTo(next)) {
            error("weighted zone ledger $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $title"

    fun adjustedValue(): Double = round((createdAt.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): WeightedZoneLedger {
        return WeightedZoneLedger(
            code = code,
            title = title,
            sequence = sequence,
            createdAt = createdAt,
            fuelLiters = fuelLiters,
            contactEmail = contactEmail,
            locale = locale,
            quantity = quantity,
            currency = currency,
            shiftCode = shiftCode,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted zone ledger in status open. */
fun newWeightedZoneLedger(code: String, title: String): WeightedZoneLedger {
    return WeightedZoneLedger(
        code = code,
        title = title,
        sequence = 0,
        createdAt = 0,
        fuelLiters = 0.0,
        contactEmail = "",
        locale = "",
        quantity = 0,
        currency = "",
        shiftCode = "",
        label = "",
        status = WeightedZoneLedgerStatus.OPEN,
    )
}
