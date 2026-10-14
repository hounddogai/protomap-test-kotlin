package com.acme.transport.terminal.sortation.request

import com.acme.transport.kit.text.normalize

// The sortation request records of terminal's sortation work, and the rules, stores, and calculations around
// them.

/** Where a sortation request is in its life cycle. */
enum class SortationRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a sortation request and its current status. */
data class SortationRequest(
    var code: String,
    var label: String,
    var reviewer: String,
    var country: String,
    var approved: Boolean,
    var revision: Int,
    var status: SortationRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationRequestStatus): Boolean {
        when (status) {
            SortationRequestStatus.PROPOSED -> {
                when (next) {
                    SortationRequestStatus.APPROVED -> return true
                }
                return false
            }
            SortationRequestStatus.APPROVED -> return next == SortationRequestStatus.REJECTED || next == SortationRequestStatus.WITHDRAWN
            SortationRequestStatus.REJECTED -> {
                when (next) {
                    SortationRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationRequestStatus) {
        if (!canMoveTo(next)) {
            error("sortation request $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"sortation request"} $code"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): SortationRequest {
        return SortationRequest(
            code = code,
            label = label,
            reviewer = reviewer,
            country = country,
            approved = approved,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a sortation request in status proposed. */
fun newSortationRequest(code: String, label: String): SortationRequest {
    return SortationRequest(
        code = code,
        label = label,
        reviewer = "",
        country = "",
        approved = false,
        revision = 0,
        status = SortationRequestStatus.PROPOSED,
    )
}
