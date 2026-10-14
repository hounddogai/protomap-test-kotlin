package com.acme.transport.lastmile.attempt.segment

import com.acme.transport.kit.text.normalize

/** Where a manual attempt request is in its life cycle. */
enum class ManualAttemptRequestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One manual attempt request, with what the planners need to act on it. */
data class ManualAttemptRequest(
    var code: String,
    var name: String,
    var title: String,
    var country: String,
    var carrier: String,
    var category: String,
    var timezone: String,
    var score: Double,
    var status: ManualAttemptRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAttemptRequestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAttemptRequestStatus): Boolean {
        when (status) {
            ManualAttemptRequestStatus.NEW -> return next == ManualAttemptRequestStatus.QUEUED || next == ManualAttemptRequestStatus.RUNNING
            ManualAttemptRequestStatus.QUEUED -> {
                when (next) {
                    ManualAttemptRequestStatus.RUNNING, ManualAttemptRequestStatus.DONE -> return true
                }
                return false
            }
            ManualAttemptRequestStatus.RUNNING -> return next == ManualAttemptRequestStatus.FAILED
            ManualAttemptRequestStatus.DONE -> {
                when (next) {
                    ManualAttemptRequestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualAttemptRequestStatus) {
        if (!canMoveTo(next)) {
            error("manual attempt request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = score

    fun snapshot(): ManualAttemptRequest {
        return ManualAttemptRequest(
            code = code,
            name = name,
            title = title,
            country = country,
            carrier = carrier,
            category = category,
            timezone = timezone,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a manual attempt request in status new. */
fun newManualAttemptRequest(code: String, name: String): ManualAttemptRequest {
    return ManualAttemptRequest(
        code = code,
        name = name,
        title = "",
        country = "",
        carrier = "",
        category = "",
        timezone = "",
        score = 0.0,
        status = ManualAttemptRequestStatus.NEW,
    )
}
