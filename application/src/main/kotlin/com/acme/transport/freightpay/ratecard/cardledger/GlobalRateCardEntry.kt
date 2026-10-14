package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global rate card entry is in its life cycle. */
enum class GlobalRateCardEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global rate card entry as the rate card team keeps it. */
data class GlobalRateCardEntry(
    var code: String,
    var name: String,
    var batchSize: Int,
    var trailerId: String,
    var discount: Double,
    var odometer: Int,
    var tags: MutableList<String>,
    var maximum: Double,
    var distanceKm: Double,
    var ceiling: Double,
    var active: Boolean,
    var status: GlobalRateCardEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRateCardEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRateCardEntryStatus): Boolean {
        when (status) {
            GlobalRateCardEntryStatus.OPEN -> {
                when (next) {
                    GlobalRateCardEntryStatus.IN_REVIEW -> return true
                }
                return false
            }
            GlobalRateCardEntryStatus.IN_REVIEW -> return next == GlobalRateCardEntryStatus.RESOLVED || next == GlobalRateCardEntryStatus.REOPENED
            GlobalRateCardEntryStatus.RESOLVED -> return next == GlobalRateCardEntryStatus.REOPENED || next == GlobalRateCardEntryStatus.ARCHIVED
            GlobalRateCardEntryStatus.REOPENED -> return next == GlobalRateCardEntryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalRateCardEntryStatus) {
        if (!canMoveTo(next)) {
            error("global rate card entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveExposure(): Double = round(maximum * ceiling * 100) / 100

    fun snapshot(): GlobalRateCardEntry {
        return GlobalRateCardEntry(
            code = code,
            name = name,
            batchSize = batchSize,
            trailerId = trailerId,
            discount = discount,
            odometer = odometer,
            tags = tags,
            maximum = maximum,
            distanceKm = distanceKm,
            ceiling = ceiling,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global rate card entry in status open. */
fun newGlobalRateCardEntry(code: String, name: String): GlobalRateCardEntry {
    return GlobalRateCardEntry(
        code = code,
        name = name,
        batchSize = 0,
        trailerId = "",
        discount = 0.0,
        odometer = 0,
        tags = mutableListOf<String>(),
        maximum = 0.0,
        distanceKm = 0.0,
        ceiling = 0.0,
        active = false,
        status = GlobalRateCardEntryStatus.OPEN,
    )
}
