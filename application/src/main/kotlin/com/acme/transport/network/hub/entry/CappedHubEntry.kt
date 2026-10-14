package com.acme.transport.network.hub.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped hub entry records of network's hub work, and the rules, stores, and calculations around them.

/** Where a capped hub entry is in its life cycle. */
enum class CappedHubEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a capped hub entry and its current status. */
data class CappedHubEntry(
    var code: String,
    var name: String,
    var ratio: Double,
    var source: String,
    var margin: Double,
    var floorValue: Double,
    var ceiling: Double,
    var externalRef: String,
    var phone: String,
    var driverId: String,
    var minimum: Double,
    var status: CappedHubEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedHubEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedHubEntryStatus): Boolean {
        when (status) {
            CappedHubEntryStatus.PROPOSED -> {
                when (next) {
                    CappedHubEntryStatus.REJECTED, CappedHubEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedHubEntryStatus.APPROVED -> return next == CappedHubEntryStatus.WITHDRAWN
            CappedHubEntryStatus.REJECTED -> {
                when (next) {
                    CappedHubEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedHubEntryStatus) {
        if (!canMoveTo(next)) {
            error("capped hub entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped hub entry"} $code"

    fun effectiveLoad(): Double = round((ceiling + minimum) * 100) / 100

    fun snapshot(): CappedHubEntry {
        return CappedHubEntry(
            code = code,
            name = name,
            ratio = ratio,
            source = source,
            margin = margin,
            floorValue = floorValue,
            ceiling = ceiling,
            externalRef = externalRef,
            phone = phone,
            driverId = driverId,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capped hub entry in status proposed. */
fun newCappedHubEntry(code: String, name: String): CappedHubEntry {
    return CappedHubEntry(
        code = code,
        name = name,
        ratio = 0.0,
        source = "",
        margin = 0.0,
        floorValue = 0.0,
        ceiling = 0.0,
        externalRef = "",
        phone = "",
        driverId = "",
        minimum = 0.0,
        status = CappedHubEntryStatus.PROPOSED,
    )
}
