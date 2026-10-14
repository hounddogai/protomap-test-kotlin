package com.acme.transport.network.cutoff.entry

import com.acme.transport.kit.text.normalize

// The cutoff entry records of network's cutoff work, and the rules, stores, and calculations around them.

/** Where a cutoff entry is in its life cycle. */
enum class CutoffEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a cutoff entry and its current status. */
data class CutoffEntry(
    var code: String,
    var name: String,
    var currency: String,
    var startsAt: Int,
    var label: String,
    var status: CutoffEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffEntryStatus): Boolean {
        when (status) {
            CutoffEntryStatus.DRAFT -> return next == CutoffEntryStatus.PENDING
            CutoffEntryStatus.PENDING -> {
                when (next) {
                    CutoffEntryStatus.CLOSED -> return true
                }
                return false
            }
            CutoffEntryStatus.ACTIVE -> {
                when (next) {
                    CutoffEntryStatus.CLOSED -> return true
                }
                return false
            }
            CutoffEntryStatus.SUSPENDED -> {
                when (next) {
                    CutoffEntryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffEntryStatus) {
        if (!canMoveTo(next)) {
            error("cutoff entry $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $name"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): CutoffEntry {
        return CutoffEntry(
            code = code,
            name = name,
            currency = currency,
            startsAt = startsAt,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a cutoff entry in status draft. */
fun newCutoffEntry(code: String, name: String): CutoffEntry {
    return CutoffEntry(
        code = code,
        name = name,
        currency = "",
        startsAt = 0,
        label = "",
        status = CutoffEntryStatus.DRAFT,
    )
}
