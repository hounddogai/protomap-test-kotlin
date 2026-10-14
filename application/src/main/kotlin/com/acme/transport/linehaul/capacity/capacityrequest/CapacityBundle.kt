package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity bundle is in its life cycle. */
enum class CapacityBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a capacity bundle and its current status. */
data class CapacityBundle(
    var code: String,
    var title: String,
    var priority: Int,
    var graceDays: Int,
    var source: String,
    var externalRef: String,
    var ratio: Double,
    var status: CapacityBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityBundleStatus): Boolean {
        when (status) {
            CapacityBundleStatus.OPEN -> return next == CapacityBundleStatus.RESOLVED
            CapacityBundleStatus.IN_REVIEW -> {
                when (next) {
                    CapacityBundleStatus.RESOLVED -> return true
                }
                return false
            }
            CapacityBundleStatus.RESOLVED -> return next == CapacityBundleStatus.REOPENED
            CapacityBundleStatus.REOPENED -> {
                when (next) {
                    CapacityBundleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityBundleStatus) {
        if (!canMoveTo(next)) {
            error("capacity bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedVolume(): Double = round((ratio + graceDays.toDouble()) * 100) / 100

    fun snapshot(): CapacityBundle {
        return CapacityBundle(
            code = code,
            title = title,
            priority = priority,
            graceDays = graceDays,
            source = source,
            externalRef = externalRef,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capacity bundle in status open. */
fun newCapacityBundle(code: String, title: String): CapacityBundle {
    return CapacityBundle(
        code = code,
        title = title,
        priority = 0,
        graceDays = 0,
        source = "",
        externalRef = "",
        ratio = 0.0,
        status = CapacityBundleStatus.OPEN,
    )
}
