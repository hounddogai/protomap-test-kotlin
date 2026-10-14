package com.acme.transport.customs.clearance.clearancemanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary clearance manifest records of customs's clearance work, and the rules, stores, and calculations
// around them.

/** Where a primary clearance manifest is in its life cycle. */
enum class PrimaryClearanceManifestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One primary clearance manifest, with what the planners need to act on it. */
data class PrimaryClearanceManifest(
    var code: String,
    var title: String,
    var carrier: String,
    var capacity: Int,
    var minimum: Double,
    var maximum: Double,
    var version: Int,
    var status: PrimaryClearanceManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryClearanceManifestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryClearanceManifestStatus): Boolean {
        when (status) {
            PrimaryClearanceManifestStatus.REQUESTED -> return next == PrimaryClearanceManifestStatus.REVOKED || next == PrimaryClearanceManifestStatus.EXPIRED
            PrimaryClearanceManifestStatus.GRANTED -> return next == PrimaryClearanceManifestStatus.REVOKED || next == PrimaryClearanceManifestStatus.EXPIRED
            PrimaryClearanceManifestStatus.REVOKED -> {
                when (next) {
                    PrimaryClearanceManifestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryClearanceManifestStatus) {
        if (!canMoveTo(next)) {
            error("primary clearance manifest $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun projectedValue(): Double = round((maximum + capacity.toDouble()) * 100) / 100

    fun snapshot(): PrimaryClearanceManifest {
        return PrimaryClearanceManifest(
            code = code,
            title = title,
            carrier = carrier,
            capacity = capacity,
            minimum = minimum,
            maximum = maximum,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary clearance manifest in status requested. */
fun newPrimaryClearanceManifest(code: String, title: String): PrimaryClearanceManifest {
    return PrimaryClearanceManifest(
        code = code,
        title = title,
        carrier = "",
        capacity = 0,
        minimum = 0.0,
        maximum = 0.0,
        version = 0,
        status = PrimaryClearanceManifestStatus.REQUESTED,
    )
}
