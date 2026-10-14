package com.acme.transport.crewing.swap.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap draft is in its life cycle. */
enum class SwapDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One swap draft, with what the planners need to act on it. */
data class SwapDraft(
    var code: String,
    var name: String,
    var retries: Int,
    var floorValue: Double,
    var driverId: String,
    var cost: Double,
    var weight: Double,
    var externalRef: String,
    var durationMinutes: Int,
    var threshold: Double,
    var startsAt: Int,
    var status: SwapDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapDraftStatus): Boolean {
        when (status) {
            SwapDraftStatus.CREATED -> {
                when (next) {
                    SwapDraftStatus.VALIDATED, SwapDraftStatus.PUBLISHED -> return true
                }
                return false
            }
            SwapDraftStatus.VALIDATED -> {
                when (next) {
                    SwapDraftStatus.PUBLISHED, SwapDraftStatus.RETIRED -> return true
                }
                return false
            }
            SwapDraftStatus.PUBLISHED -> return next == SwapDraftStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SwapDraftStatus) {
        if (!canMoveTo(next)) {
            error("swap draft $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedExposure(): Double = round((durationMinutes.toDouble() + threshold) * 100) / 100

    fun snapshot(): SwapDraft {
        return SwapDraft(
            code = code,
            name = name,
            retries = retries,
            floorValue = floorValue,
            driverId = driverId,
            cost = cost,
            weight = weight,
            externalRef = externalRef,
            durationMinutes = durationMinutes,
            threshold = threshold,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a swap draft in status created. */
fun newSwapDraft(code: String, name: String): SwapDraft {
    return SwapDraft(
        code = code,
        name = name,
        retries = 0,
        floorValue = 0.0,
        driverId = "",
        cost = 0.0,
        weight = 0.0,
        externalRef = "",
        durationMinutes = 0,
        threshold = 0.0,
        startsAt = 0,
        status = SwapDraftStatus.CREATED,
    )
}
