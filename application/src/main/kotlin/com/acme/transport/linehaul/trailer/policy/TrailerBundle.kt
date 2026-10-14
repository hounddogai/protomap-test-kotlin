package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer bundle is in its life cycle. */
enum class TrailerBundleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a trailer bundle and its current status. */
data class TrailerBundle(
    var code: String,
    var name: String,
    var active: Boolean,
    var stopCount: Int,
    var amount: Double,
    var status: TrailerBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerBundleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerBundleStatus): Boolean {
        when (status) {
            TrailerBundleStatus.CREATED -> return next == TrailerBundleStatus.VALIDATED || next == TrailerBundleStatus.PUBLISHED
            TrailerBundleStatus.VALIDATED -> return next == TrailerBundleStatus.PUBLISHED
            TrailerBundleStatus.PUBLISHED -> {
                when (next) {
                    TrailerBundleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerBundleStatus) {
        if (!canMoveTo(next)) {
            error("trailer bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netLoad(): Double = round((stopCount.toDouble() - amount) * 100) / 100

    fun snapshot(): TrailerBundle {
        return TrailerBundle(
            code = code,
            name = name,
            active = active,
            stopCount = stopCount,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a trailer bundle in status created. */
fun newTrailerBundle(code: String, name: String): TrailerBundle {
    return TrailerBundle(
        code = code,
        name = name,
        active = false,
        stopCount = 0,
        amount = 0.0,
        status = TrailerBundleStatus.CREATED,
    )
}
