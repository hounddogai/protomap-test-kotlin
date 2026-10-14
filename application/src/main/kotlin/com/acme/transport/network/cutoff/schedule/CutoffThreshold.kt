package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff threshold is in its life cycle. */
enum class CutoffThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One cutoff threshold, with what the planners need to act on it. */
data class CutoffThreshold(
    var code: String,
    var title: String,
    var contactEmail: String,
    var externalRef: String,
    var threshold: Double,
    var reviewer: String,
    var priority: Int,
    var active: Boolean,
    var rate: Double,
    var status: CutoffThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffThresholdStatus): Boolean {
        when (status) {
            CutoffThresholdStatus.PROPOSED -> return next == CutoffThresholdStatus.WITHDRAWN
            CutoffThresholdStatus.APPROVED -> {
                when (next) {
                    CutoffThresholdStatus.REJECTED, CutoffThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            CutoffThresholdStatus.REJECTED -> return next == CutoffThresholdStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CutoffThresholdStatus) {
        if (!canMoveTo(next)) {
            error("cutoff threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedVolume(): Double = round((rate + priority.toDouble()) * 100) / 100

    fun snapshot(): CutoffThreshold {
        return CutoffThreshold(
            code = code,
            title = title,
            contactEmail = contactEmail,
            externalRef = externalRef,
            threshold = threshold,
            reviewer = reviewer,
            priority = priority,
            active = active,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cutoff threshold in status proposed. */
fun newCutoffThreshold(code: String, title: String): CutoffThreshold {
    return CutoffThreshold(
        code = code,
        title = title,
        contactEmail = "",
        externalRef = "",
        threshold = 0.0,
        reviewer = "",
        priority = 0,
        active = false,
        rate = 0.0,
        status = CutoffThresholdStatus.PROPOSED,
    )
}
