package com.acme.transport.linehaul.trailer.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient trailer bundle records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a lenient trailer bundle is in its life cycle. */
enum class LenientTrailerBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a lenient trailer bundle and its current status. */
data class LenientTrailerBundle(
    var code: String,
    var title: String,
    var ratio: Double,
    var notes: String,
    var loadKg: Double,
    var retries: Int,
    var vehicleId: String,
    var createdAt: Int,
    var threshold: Double,
    var ownerId: String,
    var status: LenientTrailerBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientTrailerBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientTrailerBundleStatus): Boolean {
        when (status) {
            LenientTrailerBundleStatus.OPEN -> return next == LenientTrailerBundleStatus.ARCHIVED
            LenientTrailerBundleStatus.IN_REVIEW -> {
                when (next) {
                    LenientTrailerBundleStatus.REOPENED, LenientTrailerBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientTrailerBundleStatus.RESOLVED -> return next == LenientTrailerBundleStatus.REOPENED || next == LenientTrailerBundleStatus.ARCHIVED
            LenientTrailerBundleStatus.REOPENED -> {
                when (next) {
                    LenientTrailerBundleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientTrailerBundleStatus) {
        if (!canMoveTo(next)) {
            error("lenient trailer bundle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((ratio - loadKg) * 100) / 100

    fun snapshot(): LenientTrailerBundle {
        return LenientTrailerBundle(
            code = code,
            title = title,
            ratio = ratio,
            notes = notes,
            loadKg = loadKg,
            retries = retries,
            vehicleId = vehicleId,
            createdAt = createdAt,
            threshold = threshold,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient trailer bundle in status open. */
fun newLenientTrailerBundle(code: String, title: String): LenientTrailerBundle {
    return LenientTrailerBundle(
        code = code,
        title = title,
        ratio = 0.0,
        notes = "",
        loadKg = 0.0,
        retries = 0,
        vehicleId = "",
        createdAt = 0,
        threshold = 0.0,
        ownerId = "",
        status = LenientTrailerBundleStatus.OPEN,
    )
}
