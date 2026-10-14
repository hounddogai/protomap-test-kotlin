package com.acme.transport.crewing.swap.draft

import com.acme.transport.kit.text.normalize

// The rolling swap draft records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a rolling swap draft is in its life cycle. */
enum class RollingSwapDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One rolling swap draft, with what the planners need to act on it. */
data class RollingSwapDraft(
    var code: String,
    var name: String,
    var description: String,
    var title: String,
    var category: String,
    var driverId: String,
    var retries: Int,
    var channel: String,
    var status: RollingSwapDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingSwapDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingSwapDraftStatus): Boolean {
        when (status) {
            RollingSwapDraftStatus.CREATED -> {
                when (next) {
                    RollingSwapDraftStatus.VALIDATED, RollingSwapDraftStatus.PUBLISHED -> return true
                }
                return false
            }
            RollingSwapDraftStatus.VALIDATED -> return next == RollingSwapDraftStatus.PUBLISHED || next == RollingSwapDraftStatus.RETIRED
            RollingSwapDraftStatus.PUBLISHED -> return next == RollingSwapDraftStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingSwapDraftStatus) {
        if (!canMoveTo(next)) {
            error("rolling swap draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): RollingSwapDraft {
        return RollingSwapDraft(
            code = code,
            name = name,
            description = description,
            title = title,
            category = category,
            driverId = driverId,
            retries = retries,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rolling swap draft in status created. */
fun newRollingSwapDraft(code: String, name: String): RollingSwapDraft {
    return RollingSwapDraft(
        code = code,
        name = name,
        description = "",
        title = "",
        category = "",
        driverId = "",
        retries = 0,
        channel = "",
        status = RollingSwapDraftStatus.CREATED,
    )
}
