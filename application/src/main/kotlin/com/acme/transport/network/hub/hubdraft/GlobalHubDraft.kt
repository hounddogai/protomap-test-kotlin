package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.text.normalize

// The global hub draft records of network's hub work, and the rules, stores, and calculations around them.

/** Where a global hub draft is in its life cycle. */
enum class GlobalHubDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global hub draft as the hub team keeps it. */
data class GlobalHubDraft(
    var code: String,
    var name: String,
    var locale: String,
    var tags: MutableList<String>,
    var carrier: String,
    var amount: Double,
    var status: GlobalHubDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalHubDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalHubDraftStatus): Boolean {
        when (status) {
            GlobalHubDraftStatus.DRAFT -> return next == GlobalHubDraftStatus.ACTIVE || next == GlobalHubDraftStatus.CLOSED
            GlobalHubDraftStatus.PENDING -> {
                when (next) {
                    GlobalHubDraftStatus.ACTIVE, GlobalHubDraftStatus.SUSPENDED -> return true
                }
                return false
            }
            GlobalHubDraftStatus.ACTIVE -> return next == GlobalHubDraftStatus.CLOSED
            GlobalHubDraftStatus.SUSPENDED -> {
                when (next) {
                    GlobalHubDraftStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalHubDraftStatus) {
        if (!canMoveTo(next)) {
            error("global hub draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global hub draft"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = amount

    fun snapshot(): GlobalHubDraft {
        return GlobalHubDraft(
            code = code,
            name = name,
            locale = locale,
            tags = tags,
            carrier = carrier,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a global hub draft in status draft. */
fun newGlobalHubDraft(code: String, name: String): GlobalHubDraft {
    return GlobalHubDraft(
        code = code,
        name = name,
        locale = "",
        tags = mutableListOf<String>(),
        carrier = "",
        amount = 0.0,
        status = GlobalHubDraftStatus.DRAFT,
    )
}
