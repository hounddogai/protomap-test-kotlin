package com.acme.transport.freightpay.fuelsurcharge.surchargeentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped fuel surcharge entry records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a capped fuel surcharge entry is in its life cycle. */
enum class CappedFuelSurchargeEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One capped fuel surcharge entry, with what the planners need to act on it. */
data class CappedFuelSurchargeEntry(
    var code: String,
    var label: String,
    var trailerId: String,
    var tags: MutableList<String>,
    var distanceKm: Double,
    var version: Int,
    var contactEmail: String,
    var minimum: Double,
    var status: CappedFuelSurchargeEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedFuelSurchargeEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedFuelSurchargeEntryStatus): Boolean {
        when (status) {
            CappedFuelSurchargeEntryStatus.NEW -> {
                when (next) {
                    CappedFuelSurchargeEntryStatus.DONE -> return true
                }
                return false
            }
            CappedFuelSurchargeEntryStatus.QUEUED -> {
                when (next) {
                    CappedFuelSurchargeEntryStatus.RUNNING, CappedFuelSurchargeEntryStatus.FAILED -> return true
                }
                return false
            }
            CappedFuelSurchargeEntryStatus.RUNNING -> return next == CappedFuelSurchargeEntryStatus.DONE || next == CappedFuelSurchargeEntryStatus.FAILED
            CappedFuelSurchargeEntryStatus.DONE -> {
                when (next) {
                    CappedFuelSurchargeEntryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedFuelSurchargeEntryStatus) {
        if (!canMoveTo(next)) {
            error("capped fuel surcharge entry $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"capped fuel surcharge entry"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveExposure(): Double = round((distanceKm - version.toDouble()) * 100) / 100

    fun snapshot(): CappedFuelSurchargeEntry {
        return CappedFuelSurchargeEntry(
            code = code,
            label = label,
            trailerId = trailerId,
            tags = tags,
            distanceKm = distanceKm,
            version = version,
            contactEmail = contactEmail,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped fuel surcharge entry in status new. */
fun newCappedFuelSurchargeEntry(code: String, label: String): CappedFuelSurchargeEntry {
    return CappedFuelSurchargeEntry(
        code = code,
        label = label,
        trailerId = "",
        tags = mutableListOf<String>(),
        distanceKm = 0.0,
        version = 0,
        contactEmail = "",
        minimum = 0.0,
        status = CappedFuelSurchargeEntryStatus.NEW,
    )
}
