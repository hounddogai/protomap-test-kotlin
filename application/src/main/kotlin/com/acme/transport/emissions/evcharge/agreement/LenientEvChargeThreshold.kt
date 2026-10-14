package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.kit.text.normalize

/** Where a lenient ev charge threshold is in its life cycle. */
enum class LenientEvChargeThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One lenient ev charge threshold, with what the planners need to act on it. */
data class LenientEvChargeThreshold(
    var code: String,
    var title: String,
    var locale: String,
    var margin: Double,
    var tags: MutableList<String>,
    var pinned: Boolean,
    var status: LenientEvChargeThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientEvChargeThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientEvChargeThresholdStatus): Boolean {
        when (status) {
            LenientEvChargeThresholdStatus.OPEN -> {
                when (next) {
                    LenientEvChargeThresholdStatus.RESOLVED -> return true
                }
                return false
            }
            LenientEvChargeThresholdStatus.IN_REVIEW -> {
                when (next) {
                    LenientEvChargeThresholdStatus.REOPENED -> return true
                }
                return false
            }
            LenientEvChargeThresholdStatus.RESOLVED -> return next == LenientEvChargeThresholdStatus.REOPENED
            LenientEvChargeThresholdStatus.REOPENED -> {
                when (next) {
                    LenientEvChargeThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientEvChargeThresholdStatus) {
        if (!canMoveTo(next)) {
            error("lenient ev charge threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient ev charge threshold"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = margin

    fun snapshot(): LenientEvChargeThreshold {
        return LenientEvChargeThreshold(
            code = code,
            title = title,
            locale = locale,
            margin = margin,
            tags = tags,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lenient ev charge threshold in status open. */
fun newLenientEvChargeThreshold(code: String, title: String): LenientEvChargeThreshold {
    return LenientEvChargeThreshold(
        code = code,
        title = title,
        locale = "",
        margin = 0.0,
        tags = mutableListOf<String>(),
        pinned = false,
        status = LenientEvChargeThresholdStatus.OPEN,
    )
}
