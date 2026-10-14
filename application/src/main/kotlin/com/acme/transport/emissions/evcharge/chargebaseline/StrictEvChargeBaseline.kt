package com.acme.transport.emissions.evcharge.chargebaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict ev charge baseline records of emissions's ev charge work, and the rules, stores, and
// calculations around them.

/** Where a strict ev charge baseline is in its life cycle. */
enum class StrictEvChargeBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict ev charge baseline as the ev charge team keeps it. */
data class StrictEvChargeBaseline(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var dock: String,
    var revision: Int,
    var currency: String,
    var endsAt: Int,
    var status: StrictEvChargeBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictEvChargeBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictEvChargeBaselineStatus): Boolean {
        when (status) {
            StrictEvChargeBaselineStatus.OPEN -> {
                when (next) {
                    StrictEvChargeBaselineStatus.IN_REVIEW, StrictEvChargeBaselineStatus.RESOLVED -> return true
                }
                return false
            }
            StrictEvChargeBaselineStatus.IN_REVIEW -> {
                when (next) {
                    StrictEvChargeBaselineStatus.REOPENED, StrictEvChargeBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictEvChargeBaselineStatus.RESOLVED -> {
                when (next) {
                    StrictEvChargeBaselineStatus.REOPENED, StrictEvChargeBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictEvChargeBaselineStatus.REOPENED -> {
                when (next) {
                    StrictEvChargeBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictEvChargeBaselineStatus) {
        if (!canMoveTo(next)) {
            error("strict ev charge baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun effectiveTotal(): Double = round((revision.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): StrictEvChargeBaseline {
        return StrictEvChargeBaseline(
            code = code,
            title = title,
            pinned = pinned,
            dock = dock,
            revision = revision,
            currency = currency,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict ev charge baseline in status open. */
fun newStrictEvChargeBaseline(code: String, title: String): StrictEvChargeBaseline {
    return StrictEvChargeBaseline(
        code = code,
        title = title,
        pinned = false,
        dock = "",
        revision = 0,
        currency = "",
        endsAt = 0,
        status = StrictEvChargeBaselineStatus.OPEN,
    )
}
