package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive swap window is in its life cycle. */
enum class AdaptiveSwapWindowStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A adaptive swap window as the swap team keeps it. */
data class AdaptiveSwapWindow(
    var code: String,
    var label: String,
    var cost: Double,
    var country: String,
    var checksum: String,
    var tags: MutableList<String>,
    var dock: String,
    var distanceKm: Double,
    var source: String,
    var priority: Int,
    var status: AdaptiveSwapWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveSwapWindowStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveSwapWindowStatus): Boolean {
        when (status) {
            AdaptiveSwapWindowStatus.IDLE -> {
                when (next) {
                    AdaptiveSwapWindowStatus.WARMING, AdaptiveSwapWindowStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveSwapWindowStatus.WARMING -> {
                when (next) {
                    AdaptiveSwapWindowStatus.READY, AdaptiveSwapWindowStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveSwapWindowStatus.READY -> {
                when (next) {
                    AdaptiveSwapWindowStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveSwapWindowStatus.DRAINING -> return next == AdaptiveSwapWindowStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AdaptiveSwapWindowStatus) {
        if (!canMoveTo(next)) {
            error("adaptive swap window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round((distanceKm - cost) * 100) / 100

    fun snapshot(): AdaptiveSwapWindow {
        return AdaptiveSwapWindow(
            code = code,
            label = label,
            cost = cost,
            country = country,
            checksum = checksum,
            tags = tags,
            dock = dock,
            distanceKm = distanceKm,
            source = source,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive swap window in status idle. */
fun newAdaptiveSwapWindow(code: String, label: String): AdaptiveSwapWindow {
    return AdaptiveSwapWindow(
        code = code,
        label = label,
        cost = 0.0,
        country = "",
        checksum = "",
        tags = mutableListOf<String>(),
        dock = "",
        distanceKm = 0.0,
        source = "",
        priority = 0,
        status = AdaptiveSwapWindowStatus.IDLE,
    )
}
