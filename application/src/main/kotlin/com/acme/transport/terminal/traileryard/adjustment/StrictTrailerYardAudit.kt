package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict trailer yard audit is in its life cycle. */
enum class StrictTrailerYardAuditStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One strict trailer yard audit, with what the planners need to act on it. */
data class StrictTrailerYardAudit(
    var code: String,
    var title: String,
    var capacity: Int,
    var startsAt: Int,
    var expiresAt: Int,
    var budget: Double,
    var driverId: String,
    var tags: MutableList<String>,
    var dock: String,
    var externalRef: String,
    var status: StrictTrailerYardAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTrailerYardAuditStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTrailerYardAuditStatus): Boolean {
        when (status) {
            StrictTrailerYardAuditStatus.PROPOSED -> {
                when (next) {
                    StrictTrailerYardAuditStatus.APPROVED, StrictTrailerYardAuditStatus.REJECTED -> return true
                }
                return false
            }
            StrictTrailerYardAuditStatus.APPROVED -> {
                when (next) {
                    StrictTrailerYardAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
            StrictTrailerYardAuditStatus.REJECTED -> {
                when (next) {
                    StrictTrailerYardAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictTrailerYardAuditStatus) {
        if (!canMoveTo(next)) {
            error("strict trailer yard audit $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round(budget * capacity.toDouble() * 100) / 100

    fun snapshot(): StrictTrailerYardAudit {
        return StrictTrailerYardAudit(
            code = code,
            title = title,
            capacity = capacity,
            startsAt = startsAt,
            expiresAt = expiresAt,
            budget = budget,
            driverId = driverId,
            tags = tags,
            dock = dock,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict trailer yard audit in status proposed. */
fun newStrictTrailerYardAudit(code: String, title: String): StrictTrailerYardAudit {
    return StrictTrailerYardAudit(
        code = code,
        title = title,
        capacity = 0,
        startsAt = 0,
        expiresAt = 0,
        budget = 0.0,
        driverId = "",
        tags = mutableListOf<String>(),
        dock = "",
        externalRef = "",
        status = StrictTrailerYardAuditStatus.PROPOSED,
    )
}
