package com.acme.transport.linehaul.relay.group

import com.acme.transport.kit.text.normalize

/** Where a capped relay notice is in its life cycle. */
enum class CappedRelayNoticeStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped relay notice as the relay team keeps it. */
data class CappedRelayNotice(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var priority: Int,
    var tags: MutableList<String>,
    var status: CappedRelayNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRelayNoticeStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRelayNoticeStatus): Boolean {
        when (status) {
            CappedRelayNoticeStatus.DRAFT -> return next == CappedRelayNoticeStatus.ACTIVE || next == CappedRelayNoticeStatus.SUSPENDED
            CappedRelayNoticeStatus.PENDING -> return next == CappedRelayNoticeStatus.ACTIVE
            CappedRelayNoticeStatus.ACTIVE -> return next == CappedRelayNoticeStatus.SUSPENDED
            CappedRelayNoticeStatus.SUSPENDED -> return next == CappedRelayNoticeStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedRelayNoticeStatus) {
        if (!canMoveTo(next)) {
            error("capped relay notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped relay notice"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): CappedRelayNotice {
        return CappedRelayNotice(
            code = code,
            title = title,
            attributes = attributes,
            priority = priority,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped relay notice in status draft. */
fun newCappedRelayNotice(code: String, title: String): CappedRelayNotice {
    return CappedRelayNotice(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        priority = 0,
        tags = mutableListOf<String>(),
        status = CappedRelayNoticeStatus.DRAFT,
    )
}
