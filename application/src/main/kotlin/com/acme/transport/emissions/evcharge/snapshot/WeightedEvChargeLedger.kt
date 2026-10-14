package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.kit.text.normalize

/** Where a weighted ev charge ledger is in its life cycle. */
enum class WeightedEvChargeLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A weighted ev charge ledger. Its code is unique within a tenant. */
data class WeightedEvChargeLedger(
    var code: String,
    var title: String,
    var checksum: String,
    var category: String,
    var region: String,
    var notes: String,
    var quantity: Int,
    var status: WeightedEvChargeLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedEvChargeLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedEvChargeLedgerStatus): Boolean {
        when (status) {
            WeightedEvChargeLedgerStatus.CREATED -> return next == WeightedEvChargeLedgerStatus.PUBLISHED
            WeightedEvChargeLedgerStatus.VALIDATED -> return next == WeightedEvChargeLedgerStatus.PUBLISHED
            WeightedEvChargeLedgerStatus.PUBLISHED -> return next == WeightedEvChargeLedgerStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedEvChargeLedgerStatus) {
        if (!canMoveTo(next)) {
            error("weighted ev charge ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): WeightedEvChargeLedger {
        return WeightedEvChargeLedger(
            code = code,
            title = title,
            checksum = checksum,
            category = category,
            region = region,
            notes = notes,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a weighted ev charge ledger in status created. */
fun newWeightedEvChargeLedger(code: String, title: String): WeightedEvChargeLedger {
    return WeightedEvChargeLedger(
        code = code,
        title = title,
        checksum = "",
        category = "",
        region = "",
        notes = "",
        quantity = 0,
        status = WeightedEvChargeLedgerStatus.CREATED,
    )
}
