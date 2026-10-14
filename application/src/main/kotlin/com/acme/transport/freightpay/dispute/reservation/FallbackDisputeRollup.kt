package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.text.normalize

/** Where a fallback dispute rollup is in its life cycle. */
enum class FallbackDisputeRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback dispute rollup as the dispute team keeps it. */
data class FallbackDisputeRollup(
    var code: String,
    var title: String,
    var locale: String,
    var tags: MutableList<String>,
    var externalRef: String,
    var reviewer: String,
    var pinned: Boolean,
    var etaMinutes: Int,
    var status: FallbackDisputeRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDisputeRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDisputeRollupStatus): Boolean {
        when (status) {
            FallbackDisputeRollupStatus.NEW -> return next == FallbackDisputeRollupStatus.DONE
            FallbackDisputeRollupStatus.QUEUED -> {
                when (next) {
                    FallbackDisputeRollupStatus.RUNNING, FallbackDisputeRollupStatus.FAILED -> return true
                }
                return false
            }
            FallbackDisputeRollupStatus.RUNNING -> {
                when (next) {
                    FallbackDisputeRollupStatus.DONE -> return true
                }
                return false
            }
            FallbackDisputeRollupStatus.DONE -> return next == FallbackDisputeRollupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackDisputeRollupStatus) {
        if (!canMoveTo(next)) {
            error("fallback dispute rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): FallbackDisputeRollup {
        return FallbackDisputeRollup(
            code = code,
            title = title,
            locale = locale,
            tags = tags,
            externalRef = externalRef,
            reviewer = reviewer,
            pinned = pinned,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback dispute rollup in status new. */
fun newFallbackDisputeRollup(code: String, title: String): FallbackDisputeRollup {
    return FallbackDisputeRollup(
        code = code,
        title = title,
        locale = "",
        tags = mutableListOf<String>(),
        externalRef = "",
        reviewer = "",
        pinned = false,
        etaMinutes = 0,
        status = FallbackDisputeRollupStatus.NEW,
    )
}
