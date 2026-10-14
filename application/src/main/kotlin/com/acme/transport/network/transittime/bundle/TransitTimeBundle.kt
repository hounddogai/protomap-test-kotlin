package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The transit time bundle records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time bundle is in its life cycle. */
enum class TransitTimeBundleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A transit time bundle as the transit time team keeps it. */
data class TransitTimeBundle(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var category: String,
    var depot: String,
    var externalRef: String,
    var stopCount: Int,
    var version: Int,
    var status: TransitTimeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeBundleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeBundleStatus): Boolean {
        when (status) {
            TransitTimeBundleStatus.PLANNED -> return next == TransitTimeBundleStatus.IN_PROGRESS || next == TransitTimeBundleStatus.COMPLETED
            TransitTimeBundleStatus.SCHEDULED -> {
                when (next) {
                    TransitTimeBundleStatus.COMPLETED -> return true
                }
                return false
            }
            TransitTimeBundleStatus.IN_PROGRESS -> return next == TransitTimeBundleStatus.CANCELLED
            TransitTimeBundleStatus.COMPLETED -> {
                when (next) {
                    TransitTimeBundleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeBundleStatus) {
        if (!canMoveTo(next)) {
            error("transit time bundle $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun netVolume(): Double = round(stopCount.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): TransitTimeBundle {
        return TransitTimeBundle(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            category = category,
            depot = depot,
            externalRef = externalRef,
            stopCount = stopCount,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a transit time bundle in status planned. */
fun newTransitTimeBundle(code: String, name: String): TransitTimeBundle {
    return TransitTimeBundle(
        code = code,
        name = name,
        etaMinutes = 0,
        category = "",
        depot = "",
        externalRef = "",
        stopCount = 0,
        version = 0,
        status = TransitTimeBundleStatus.PLANNED,
    )
}
