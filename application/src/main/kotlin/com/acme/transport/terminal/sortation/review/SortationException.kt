package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.text.normalize

/** Where a sortation exception is in its life cycle. */
enum class SortationExceptionStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A sortation exception. Its code is unique within a tenant. */
data class SortationException(
    var code: String,
    var title: String,
    var graceDays: Int,
    var notes: String,
    var tags: MutableList<String>,
    var reviewer: String,
    var lane: String,
    var status: SortationExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationExceptionStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationExceptionStatus): Boolean {
        when (status) {
            SortationExceptionStatus.REQUESTED -> {
                when (next) {
                    SortationExceptionStatus.REVOKED, SortationExceptionStatus.EXPIRED -> return true
                }
                return false
            }
            SortationExceptionStatus.GRANTED -> {
                when (next) {
                    SortationExceptionStatus.REVOKED, SortationExceptionStatus.EXPIRED -> return true
                }
                return false
            }
            SortationExceptionStatus.REVOKED -> {
                when (next) {
                    SortationExceptionStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationExceptionStatus) {
        if (!canMoveTo(next)) {
            error("sortation exception $code cannot move from $status to $next")
        }
        status = next
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

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): SortationException {
        return SortationException(
            code = code,
            title = title,
            graceDays = graceDays,
            notes = notes,
            tags = tags,
            reviewer = reviewer,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a sortation exception in status requested. */
fun newSortationException(code: String, title: String): SortationException {
    return SortationException(
        code = code,
        title = title,
        graceDays = 0,
        notes = "",
        tags = mutableListOf<String>(),
        reviewer = "",
        lane = "",
        status = SortationExceptionStatus.REQUESTED,
    )
}
