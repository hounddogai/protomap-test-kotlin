package com.acme.transport.linehaul.relay.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive relay review is in its life cycle. */
enum class AdaptiveRelayReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive relay review as the relay team keeps it. */
data class AdaptiveRelayReview(
    var code: String,
    var label: String,
    var carrier: String,
    var startsAt: Int,
    var checksum: String,
    var depot: String,
    var shiftCode: String,
    var version: Int,
    var distanceKm: Double,
    var routeCode: String,
    var region: String,
    var status: AdaptiveRelayReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRelayReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRelayReviewStatus): Boolean {
        when (status) {
            AdaptiveRelayReviewStatus.NEW -> return next == AdaptiveRelayReviewStatus.DONE
            AdaptiveRelayReviewStatus.QUEUED -> {
                when (next) {
                    AdaptiveRelayReviewStatus.RUNNING, AdaptiveRelayReviewStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveRelayReviewStatus.RUNNING -> return next == AdaptiveRelayReviewStatus.DONE || next == AdaptiveRelayReviewStatus.FAILED
            AdaptiveRelayReviewStatus.DONE -> {
                when (next) {
                    AdaptiveRelayReviewStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveRelayReviewStatus) {
        if (!canMoveTo(next)) {
            error("adaptive relay review $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun netVolume(): Double = round((distanceKm + version.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveRelayReview {
        return AdaptiveRelayReview(
            code = code,
            label = label,
            carrier = carrier,
            startsAt = startsAt,
            checksum = checksum,
            depot = depot,
            shiftCode = shiftCode,
            version = version,
            distanceKm = distanceKm,
            routeCode = routeCode,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a adaptive relay review in status new. */
fun newAdaptiveRelayReview(code: String, label: String): AdaptiveRelayReview {
    return AdaptiveRelayReview(
        code = code,
        label = label,
        carrier = "",
        startsAt = 0,
        checksum = "",
        depot = "",
        shiftCode = "",
        version = 0,
        distanceKm = 0.0,
        routeCode = "",
        region = "",
        status = AdaptiveRelayReviewStatus.NEW,
    )
}
