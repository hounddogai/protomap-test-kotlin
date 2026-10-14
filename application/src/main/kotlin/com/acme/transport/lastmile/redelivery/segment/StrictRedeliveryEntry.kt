package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.text.normalize

/** Where a strict redelivery entry is in its life cycle. */
enum class StrictRedeliveryEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A strict redelivery entry as the redelivery team keeps it. */
data class StrictRedeliveryEntry(
    var code: String,
    var name: String,
    var externalRef: String,
    var source: String,
    var title: String,
    var batchSize: Int,
    var country: String,
    var status: StrictRedeliveryEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRedeliveryEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRedeliveryEntryStatus): Boolean {
        when (status) {
            StrictRedeliveryEntryStatus.PROPOSED -> return next == StrictRedeliveryEntryStatus.WITHDRAWN
            StrictRedeliveryEntryStatus.APPROVED -> return next == StrictRedeliveryEntryStatus.REJECTED || next == StrictRedeliveryEntryStatus.WITHDRAWN
            StrictRedeliveryEntryStatus.REJECTED -> return next == StrictRedeliveryEntryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictRedeliveryEntryStatus) {
        if (!canMoveTo(next)) {
            error("strict redelivery entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): StrictRedeliveryEntry {
        return StrictRedeliveryEntry(
            code = code,
            name = name,
            externalRef = externalRef,
            source = source,
            title = title,
            batchSize = batchSize,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a strict redelivery entry in status proposed. */
fun newStrictRedeliveryEntry(code: String, name: String): StrictRedeliveryEntry {
    return StrictRedeliveryEntry(
        code = code,
        name = name,
        externalRef = "",
        source = "",
        title = "",
        batchSize = 0,
        country = "",
        status = StrictRedeliveryEntryStatus.PROPOSED,
    )
}
