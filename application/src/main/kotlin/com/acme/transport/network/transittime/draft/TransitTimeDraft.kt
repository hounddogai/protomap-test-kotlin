package com.acme.transport.network.transittime.draft

import com.acme.transport.kit.text.normalize

// The transit time draft records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time draft is in its life cycle. */
enum class TransitTimeDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a transit time draft and its current status. */
data class TransitTimeDraft(
    var code: String,
    var name: String,
    var checksum: String,
    var currency: String,
    var minimum: Double,
    var status: TransitTimeDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeDraftStatus): Boolean {
        when (status) {
            TransitTimeDraftStatus.CREATED -> return next == TransitTimeDraftStatus.RETIRED
            TransitTimeDraftStatus.VALIDATED -> {
                when (next) {
                    TransitTimeDraftStatus.PUBLISHED, TransitTimeDraftStatus.RETIRED -> return true
                }
                return false
            }
            TransitTimeDraftStatus.PUBLISHED -> {
                when (next) {
                    TransitTimeDraftStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeDraftStatus) {
        if (!canMoveTo(next)) {
            error("transit time draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = minimum

    fun snapshot(): TransitTimeDraft {
        return TransitTimeDraft(
            code = code,
            name = name,
            checksum = checksum,
            currency = currency,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a transit time draft in status created. */
fun newTransitTimeDraft(code: String, name: String): TransitTimeDraft {
    return TransitTimeDraft(
        code = code,
        name = name,
        checksum = "",
        currency = "",
        minimum = 0.0,
        status = TransitTimeDraftStatus.CREATED,
    )
}
