package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped rate card bundle is in its life cycle. */
enum class CappedRateCardBundleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One capped rate card bundle, with what the planners need to act on it. */
data class CappedRateCardBundle(
    var code: String,
    var title: String,
    var loadKg: Double,
    var startsAt: Int,
    var category: String,
    var fuelLiters: Double,
    var notes: String,
    var score: Double,
    var rate: Double,
    var expiresAt: Int,
    var pinned: Boolean,
    var status: CappedRateCardBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRateCardBundleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRateCardBundleStatus): Boolean {
        when (status) {
            CappedRateCardBundleStatus.PLANNED -> return next == CappedRateCardBundleStatus.IN_PROGRESS || next == CappedRateCardBundleStatus.CANCELLED
            CappedRateCardBundleStatus.SCHEDULED -> return next == CappedRateCardBundleStatus.COMPLETED || next == CappedRateCardBundleStatus.CANCELLED
            CappedRateCardBundleStatus.IN_PROGRESS -> return next == CappedRateCardBundleStatus.COMPLETED || next == CappedRateCardBundleStatus.CANCELLED
            CappedRateCardBundleStatus.COMPLETED -> return next == CappedRateCardBundleStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CappedRateCardBundleStatus) {
        if (!canMoveTo(next)) {
            error("capped rate card bundle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun effectiveExposure(): Double = round((fuelLiters + score) * 100) / 100

    fun snapshot(): CappedRateCardBundle {
        return CappedRateCardBundle(
            code = code,
            title = title,
            loadKg = loadKg,
            startsAt = startsAt,
            category = category,
            fuelLiters = fuelLiters,
            notes = notes,
            score = score,
            rate = rate,
            expiresAt = expiresAt,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped rate card bundle in status planned. */
fun newCappedRateCardBundle(code: String, title: String): CappedRateCardBundle {
    return CappedRateCardBundle(
        code = code,
        title = title,
        loadKg = 0.0,
        startsAt = 0,
        category = "",
        fuelLiters = 0.0,
        notes = "",
        score = 0.0,
        rate = 0.0,
        expiresAt = 0,
        pinned = false,
        status = CappedRateCardBundleStatus.PLANNED,
    )
}
