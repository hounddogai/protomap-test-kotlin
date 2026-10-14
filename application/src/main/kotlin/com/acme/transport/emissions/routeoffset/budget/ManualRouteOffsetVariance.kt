package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual route offset variance is in its life cycle. */
enum class ManualRouteOffsetVarianceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One manual route offset variance, with what the planners need to act on it. */
data class ManualRouteOffsetVariance(
    var code: String,
    var label: String,
    var archived: Boolean,
    var ratio: Double,
    var odometer: Int,
    var retries: Int,
    var revision: Int,
    var status: ManualRouteOffsetVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRouteOffsetVarianceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRouteOffsetVarianceStatus): Boolean {
        when (status) {
            ManualRouteOffsetVarianceStatus.DRAFT -> {
                when (next) {
                    ManualRouteOffsetVarianceStatus.SUSPENDED -> return true
                }
                return false
            }
            ManualRouteOffsetVarianceStatus.PENDING -> {
                when (next) {
                    ManualRouteOffsetVarianceStatus.ACTIVE, ManualRouteOffsetVarianceStatus.SUSPENDED -> return true
                }
                return false
            }
            ManualRouteOffsetVarianceStatus.ACTIVE -> return next == ManualRouteOffsetVarianceStatus.SUSPENDED
            ManualRouteOffsetVarianceStatus.SUSPENDED -> return next == ManualRouteOffsetVarianceStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ManualRouteOffsetVarianceStatus) {
        if (!canMoveTo(next)) {
            error("manual route offset variance $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedVolume(): Double = round(ratio * retries.toDouble() * 100) / 100

    fun snapshot(): ManualRouteOffsetVariance {
        return ManualRouteOffsetVariance(
            code = code,
            label = label,
            archived = archived,
            ratio = ratio,
            odometer = odometer,
            retries = retries,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual route offset variance in status draft. */
fun newManualRouteOffsetVariance(code: String, label: String): ManualRouteOffsetVariance {
    return ManualRouteOffsetVariance(
        code = code,
        label = label,
        archived = false,
        ratio = 0.0,
        odometer = 0,
        retries = 0,
        revision = 0,
        status = ManualRouteOffsetVarianceStatus.DRAFT,
    )
}
