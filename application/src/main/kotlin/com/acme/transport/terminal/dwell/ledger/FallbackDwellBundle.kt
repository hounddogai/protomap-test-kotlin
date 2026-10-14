package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback dwell bundle is in its life cycle. */
enum class FallbackDwellBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fallback dwell bundle, with what the planners need to act on it. */
data class FallbackDwellBundle(
    var code: String,
    var label: String,
    var segment: String,
    var score: Double,
    var graceDays: Int,
    var carrier: String,
    var timezone: String,
    var createdAt: Int,
    var status: FallbackDwellBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDwellBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDwellBundleStatus): Boolean {
        when (status) {
            FallbackDwellBundleStatus.OPEN -> {
                when (next) {
                    FallbackDwellBundleStatus.IN_REVIEW -> return true
                }
                return false
            }
            FallbackDwellBundleStatus.IN_REVIEW -> return next == FallbackDwellBundleStatus.ARCHIVED
            FallbackDwellBundleStatus.RESOLVED -> return next == FallbackDwellBundleStatus.REOPENED || next == FallbackDwellBundleStatus.ARCHIVED
            FallbackDwellBundleStatus.REOPENED -> {
                when (next) {
                    FallbackDwellBundleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDwellBundleStatus) {
        if (!canMoveTo(next)) {
            error("fallback dwell bundle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedLoad(): Double = round((createdAt.toDouble() - score) * 100) / 100

    fun snapshot(): FallbackDwellBundle {
        return FallbackDwellBundle(
            code = code,
            label = label,
            segment = segment,
            score = score,
            graceDays = graceDays,
            carrier = carrier,
            timezone = timezone,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fallback dwell bundle in status open. */
fun newFallbackDwellBundle(code: String, label: String): FallbackDwellBundle {
    return FallbackDwellBundle(
        code = code,
        label = label,
        segment = "",
        score = 0.0,
        graceDays = 0,
        carrier = "",
        timezone = "",
        createdAt = 0,
        status = FallbackDwellBundleStatus.OPEN,
    )
}
