package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict trailer yard template is in its life cycle. */
enum class StrictTrailerYardTemplateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A strict trailer yard template. Its code is unique within a tenant. */
data class StrictTrailerYardTemplate(
    var code: String,
    var title: String,
    var approved: Boolean,
    var timezone: String,
    var archived: Boolean,
    var attempts: Int,
    var stopCount: Int,
    var source: String,
    var updatedAt: Int,
    var status: StrictTrailerYardTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTrailerYardTemplateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTrailerYardTemplateStatus): Boolean {
        when (status) {
            StrictTrailerYardTemplateStatus.PROPOSED -> {
                when (next) {
                    StrictTrailerYardTemplateStatus.APPROVED, StrictTrailerYardTemplateStatus.REJECTED -> return true
                }
                return false
            }
            StrictTrailerYardTemplateStatus.APPROVED -> return next == StrictTrailerYardTemplateStatus.REJECTED
            StrictTrailerYardTemplateStatus.REJECTED -> return next == StrictTrailerYardTemplateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictTrailerYardTemplateStatus) {
        if (!canMoveTo(next)) {
            error("strict trailer yard template $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedTotal(): Double = round(attempts.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): StrictTrailerYardTemplate {
        return StrictTrailerYardTemplate(
            code = code,
            title = title,
            approved = approved,
            timezone = timezone,
            archived = archived,
            attempts = attempts,
            stopCount = stopCount,
            source = source,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a strict trailer yard template in status proposed. */
fun newStrictTrailerYardTemplate(code: String, title: String): StrictTrailerYardTemplate {
    return StrictTrailerYardTemplate(
        code = code,
        title = title,
        approved = false,
        timezone = "",
        archived = false,
        attempts = 0,
        stopCount = 0,
        source = "",
        updatedAt = 0,
        status = StrictTrailerYardTemplateStatus.PROPOSED,
    )
}
