package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.text.normalize

// The pending rest break mapping records of crewing's rest break work, and the rules, stores, and
// calculations around them.

/** Where a pending rest break mapping is in its life cycle. */
enum class PendingRestBreakMappingStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending rest break mapping as the rest break team keeps it. */
data class PendingRestBreakMapping(
    var code: String,
    var title: String,
    var threshold: Double,
    var externalRef: String,
    var tags: MutableList<String>,
    var status: PendingRestBreakMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRestBreakMappingStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRestBreakMappingStatus): Boolean {
        when (status) {
            PendingRestBreakMappingStatus.DRAFT -> {
                when (next) {
                    PendingRestBreakMappingStatus.SUSPENDED -> return true
                }
                return false
            }
            PendingRestBreakMappingStatus.PENDING -> return next == PendingRestBreakMappingStatus.SUSPENDED
            PendingRestBreakMappingStatus.ACTIVE -> return next == PendingRestBreakMappingStatus.SUSPENDED || next == PendingRestBreakMappingStatus.CLOSED
            PendingRestBreakMappingStatus.SUSPENDED -> {
                when (next) {
                    PendingRestBreakMappingStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingRestBreakMappingStatus) {
        if (!canMoveTo(next)) {
            error("pending rest break mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = threshold

    fun snapshot(): PendingRestBreakMapping {
        return PendingRestBreakMapping(
            code = code,
            title = title,
            threshold = threshold,
            externalRef = externalRef,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending rest break mapping in status draft. */
fun newPendingRestBreakMapping(code: String, title: String): PendingRestBreakMapping {
    return PendingRestBreakMapping(
        code = code,
        title = title,
        threshold = 0.0,
        externalRef = "",
        tags = mutableListOf<String>(),
        status = PendingRestBreakMappingStatus.DRAFT,
    )
}
