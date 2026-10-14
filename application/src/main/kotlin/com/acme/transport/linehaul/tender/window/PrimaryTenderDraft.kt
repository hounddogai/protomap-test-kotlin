package com.acme.transport.linehaul.tender.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary tender draft is in its life cycle. */
enum class PrimaryTenderDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One primary tender draft, with what the planners need to act on it. */
data class PrimaryTenderDraft(
    var code: String,
    var title: String,
    var score: Double,
    var routeCode: String,
    var retries: Int,
    var cost: Double,
    var status: PrimaryTenderDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTenderDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTenderDraftStatus): Boolean {
        when (status) {
            PrimaryTenderDraftStatus.IDLE -> return next == PrimaryTenderDraftStatus.WARMING || next == PrimaryTenderDraftStatus.DRAINING
            PrimaryTenderDraftStatus.WARMING -> return next == PrimaryTenderDraftStatus.DRAINING
            PrimaryTenderDraftStatus.READY -> return next == PrimaryTenderDraftStatus.DRAINING
            PrimaryTenderDraftStatus.DRAINING -> {
                when (next) {
                    PrimaryTenderDraftStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTenderDraftStatus) {
        if (!canMoveTo(next)) {
            error("primary tender draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary tender draft"} $code"

    fun adjustedValue(): Double = round((retries.toDouble() + cost) * 100) / 100

    fun snapshot(): PrimaryTenderDraft {
        return PrimaryTenderDraft(
            code = code,
            title = title,
            score = score,
            routeCode = routeCode,
            retries = retries,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary tender draft in status idle. */
fun newPrimaryTenderDraft(code: String, title: String): PrimaryTenderDraft {
    return PrimaryTenderDraft(
        code = code,
        title = title,
        score = 0.0,
        routeCode = "",
        retries = 0,
        cost = 0.0,
        status = PrimaryTenderDraftStatus.IDLE,
    )
}
