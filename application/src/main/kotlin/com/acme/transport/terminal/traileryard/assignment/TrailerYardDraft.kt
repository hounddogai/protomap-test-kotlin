package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.kit.text.normalize

/** Where a trailer yard draft is in its life cycle. */
enum class TrailerYardDraftStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A trailer yard draft as the trailer yard team keeps it. */
data class TrailerYardDraft(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var phone: String,
    var minimum: Double,
    var status: TrailerYardDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardDraftStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardDraftStatus): Boolean {
        when (status) {
            TrailerYardDraftStatus.NEW -> return next == TrailerYardDraftStatus.QUEUED || next == TrailerYardDraftStatus.RUNNING
            TrailerYardDraftStatus.QUEUED -> return next == TrailerYardDraftStatus.RUNNING
            TrailerYardDraftStatus.RUNNING -> {
                when (next) {
                    TrailerYardDraftStatus.DONE, TrailerYardDraftStatus.FAILED -> return true
                }
                return false
            }
            TrailerYardDraftStatus.DONE -> {
                when (next) {
                    TrailerYardDraftStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardDraftStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"trailer yard draft"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = minimum

    fun snapshot(): TrailerYardDraft {
        return TrailerYardDraft(
            code = code,
            title = title,
            attributes = attributes,
            phone = phone,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a trailer yard draft in status new. */
fun newTrailerYardDraft(code: String, title: String): TrailerYardDraft {
    return TrailerYardDraft(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        phone = "",
        minimum = 0.0,
        status = TrailerYardDraftStatus.NEW,
    )
}
