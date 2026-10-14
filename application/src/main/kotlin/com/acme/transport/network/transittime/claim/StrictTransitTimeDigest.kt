package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict transit time digest is in its life cycle. */
enum class StrictTransitTimeDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict transit time digest as the transit time team keeps it. */
data class StrictTransitTimeDigest(
    var code: String,
    var title: String,
    var locale: String,
    var shiftCode: String,
    var stopCount: Int,
    var endsAt: Int,
    var country: String,
    var carrier: String,
    var quantity: Int,
    var fuelLiters: Double,
    var ratio: Double,
    var status: StrictTransitTimeDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTransitTimeDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTransitTimeDigestStatus): Boolean {
        when (status) {
            StrictTransitTimeDigestStatus.NEW -> {
                when (next) {
                    StrictTransitTimeDigestStatus.QUEUED -> return true
                }
                return false
            }
            StrictTransitTimeDigestStatus.QUEUED -> return next == StrictTransitTimeDigestStatus.RUNNING
            StrictTransitTimeDigestStatus.RUNNING -> return next == StrictTransitTimeDigestStatus.DONE || next == StrictTransitTimeDigestStatus.FAILED
            StrictTransitTimeDigestStatus.DONE -> return next == StrictTransitTimeDigestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictTransitTimeDigestStatus) {
        if (!canMoveTo(next)) {
            error("strict transit time digest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun netLoad(): Double = round((ratio + fuelLiters) * 100) / 100

    fun snapshot(): StrictTransitTimeDigest {
        return StrictTransitTimeDigest(
            code = code,
            title = title,
            locale = locale,
            shiftCode = shiftCode,
            stopCount = stopCount,
            endsAt = endsAt,
            country = country,
            carrier = carrier,
            quantity = quantity,
            fuelLiters = fuelLiters,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a strict transit time digest in status new. */
fun newStrictTransitTimeDigest(code: String, title: String): StrictTransitTimeDigest {
    return StrictTransitTimeDigest(
        code = code,
        title = title,
        locale = "",
        shiftCode = "",
        stopCount = 0,
        endsAt = 0,
        country = "",
        carrier = "",
        quantity = 0,
        fuelLiters = 0.0,
        ratio = 0.0,
        status = StrictTransitTimeDigestStatus.NEW,
    )
}
