package com.acme.transport.linehaul.trailer.draft

import com.acme.transport.kit.text.normalize

// The trailer draft records of linehaul's trailer work, and the rules, stores, and calculations around them.

/** Where a trailer draft is in its life cycle. */
enum class TrailerDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A trailer draft as the trailer team keeps it. */
data class TrailerDraft(
    var code: String,
    var name: String,
    var segment: String,
    var tags: MutableList<String>,
    var title: String,
    var score: Double,
    var status: TrailerDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerDraftStatus): Boolean {
        when (status) {
            TrailerDraftStatus.DRAFT -> {
                when (next) {
                    TrailerDraftStatus.CLOSED -> return true
                }
                return false
            }
            TrailerDraftStatus.PENDING -> {
                when (next) {
                    TrailerDraftStatus.ACTIVE, TrailerDraftStatus.SUSPENDED -> return true
                }
                return false
            }
            TrailerDraftStatus.ACTIVE -> {
                when (next) {
                    TrailerDraftStatus.SUSPENDED, TrailerDraftStatus.CLOSED -> return true
                }
                return false
            }
            TrailerDraftStatus.SUSPENDED -> {
                when (next) {
                    TrailerDraftStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerDraftStatus) {
        if (!canMoveTo(next)) {
            error("trailer draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = score

    fun snapshot(): TrailerDraft {
        return TrailerDraft(
            code = code,
            name = name,
            segment = segment,
            tags = tags,
            title = title,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a trailer draft in status draft. */
fun newTrailerDraft(code: String, name: String): TrailerDraft {
    return TrailerDraft(
        code = code,
        name = name,
        segment = "",
        tags = mutableListOf<String>(),
        title = "",
        score = 0.0,
        status = TrailerDraftStatus.DRAFT,
    )
}
