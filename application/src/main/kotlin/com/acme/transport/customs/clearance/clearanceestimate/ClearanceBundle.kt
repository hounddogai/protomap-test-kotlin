package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance bundle is in its life cycle. */
enum class ClearanceBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One clearance bundle, with what the planners need to act on it. */
data class ClearanceBundle(
    var code: String,
    var label: String,
    var reviewer: String,
    var carrier: String,
    var trailerId: String,
    var rate: Double,
    var checksum: String,
    var batchSize: Int,
    var status: ClearanceBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceBundleStatus): Boolean {
        when (status) {
            ClearanceBundleStatus.REQUESTED -> {
                when (next) {
                    ClearanceBundleStatus.EXPIRED -> return true
                }
                return false
            }
            ClearanceBundleStatus.GRANTED -> return next == ClearanceBundleStatus.REVOKED || next == ClearanceBundleStatus.EXPIRED
            ClearanceBundleStatus.REVOKED -> return next == ClearanceBundleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ClearanceBundleStatus) {
        if (!canMoveTo(next)) {
            error("clearance bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedValue(): Double = round(rate * batchSize.toDouble() * 100) / 100

    fun snapshot(): ClearanceBundle {
        return ClearanceBundle(
            code = code,
            label = label,
            reviewer = reviewer,
            carrier = carrier,
            trailerId = trailerId,
            rate = rate,
            checksum = checksum,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a clearance bundle in status requested. */
fun newClearanceBundle(code: String, label: String): ClearanceBundle {
    return ClearanceBundle(
        code = code,
        label = label,
        reviewer = "",
        carrier = "",
        trailerId = "",
        rate = 0.0,
        checksum = "",
        batchSize = 0,
        status = ClearanceBundleStatus.REQUESTED,
    )
}
