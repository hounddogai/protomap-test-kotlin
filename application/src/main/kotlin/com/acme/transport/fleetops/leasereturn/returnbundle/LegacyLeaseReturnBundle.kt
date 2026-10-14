package com.acme.transport.fleetops.leasereturn.returnbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy lease return bundle records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a legacy lease return bundle is in its life cycle. */
enum class LegacyLeaseReturnBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a legacy lease return bundle and its current status. */
data class LegacyLeaseReturnBundle(
    var code: String,
    var name: String,
    var category: String,
    var visible: Boolean,
    var distanceKm: Double,
    var checksum: String,
    var shiftCode: String,
    var trailerId: String,
    var loadKg: Double,
    var budget: Double,
    var summary: String,
    var status: LegacyLeaseReturnBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLeaseReturnBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLeaseReturnBundleStatus): Boolean {
        when (status) {
            LegacyLeaseReturnBundleStatus.NEW -> {
                when (next) {
                    LegacyLeaseReturnBundleStatus.RUNNING -> return true
                }
                return false
            }
            LegacyLeaseReturnBundleStatus.QUEUED -> return next == LegacyLeaseReturnBundleStatus.DONE || next == LegacyLeaseReturnBundleStatus.FAILED
            LegacyLeaseReturnBundleStatus.RUNNING -> {
                when (next) {
                    LegacyLeaseReturnBundleStatus.DONE, LegacyLeaseReturnBundleStatus.FAILED -> return true
                }
                return false
            }
            LegacyLeaseReturnBundleStatus.DONE -> return next == LegacyLeaseReturnBundleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyLeaseReturnBundleStatus) {
        if (!canMoveTo(next)) {
            error("legacy lease return bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round((loadKg + distanceKm) * 100) / 100

    fun snapshot(): LegacyLeaseReturnBundle {
        return LegacyLeaseReturnBundle(
            code = code,
            name = name,
            category = category,
            visible = visible,
            distanceKm = distanceKm,
            checksum = checksum,
            shiftCode = shiftCode,
            trailerId = trailerId,
            loadKg = loadKg,
            budget = budget,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a legacy lease return bundle in status new. */
fun newLegacyLeaseReturnBundle(code: String, name: String): LegacyLeaseReturnBundle {
    return LegacyLeaseReturnBundle(
        code = code,
        name = name,
        category = "",
        visible = false,
        distanceKm = 0.0,
        checksum = "",
        shiftCode = "",
        trailerId = "",
        loadKg = 0.0,
        budget = 0.0,
        summary = "",
        status = LegacyLeaseReturnBundleStatus.NEW,
    )
}
