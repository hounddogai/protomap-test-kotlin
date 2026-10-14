package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.kit.text.normalize

/** Where a idling draft is in its life cycle. */
enum class IdlingDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a idling draft and its current status. */
data class IdlingDraft(
    var code: String,
    var label: String,
    var maximum: Double,
    var attributes: MutableMap<String, String>,
    var pinned: Boolean,
    var status: IdlingDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingDraftStatus): Boolean {
        when (status) {
            IdlingDraftStatus.NEW -> {
                when (next) {
                    IdlingDraftStatus.RUNNING, IdlingDraftStatus.FAILED -> return true
                }
                return false
            }
            IdlingDraftStatus.QUEUED -> return next == IdlingDraftStatus.FAILED
            IdlingDraftStatus.RUNNING -> {
                when (next) {
                    IdlingDraftStatus.FAILED -> return true
                }
                return false
            }
            IdlingDraftStatus.DONE -> {
                when (next) {
                    IdlingDraftStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingDraftStatus) {
        if (!canMoveTo(next)) {
            error("idling draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = maximum

    fun snapshot(): IdlingDraft {
        return IdlingDraft(
            code = code,
            label = label,
            maximum = maximum,
            attributes = attributes,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a idling draft in status new. */
fun newIdlingDraft(code: String, label: String): IdlingDraft {
    return IdlingDraft(
        code = code,
        label = label,
        maximum = 0.0,
        attributes = mutableMapOf<String, String>(),
        pinned = false,
        status = IdlingDraftStatus.NEW,
    )
}
