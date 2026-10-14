package com.acme.transport.linehaul.relay.relaygroup

import com.acme.transport.kit.text.normalize

// The lenient relay group records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a lenient relay group is in its life cycle. */
enum class LenientRelayGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One lenient relay group, with what the planners need to act on it. */
data class LenientRelayGroup(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var reviewer: String,
    var expiresAt: Int,
    var status: LenientRelayGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRelayGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRelayGroupStatus): Boolean {
        when (status) {
            LenientRelayGroupStatus.DRAFT -> return next == LenientRelayGroupStatus.PENDING || next == LenientRelayGroupStatus.SUSPENDED
            LenientRelayGroupStatus.PENDING -> {
                when (next) {
                    LenientRelayGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientRelayGroupStatus.ACTIVE -> return next == LenientRelayGroupStatus.SUSPENDED || next == LenientRelayGroupStatus.CLOSED
            LenientRelayGroupStatus.SUSPENDED -> {
                when (next) {
                    LenientRelayGroupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientRelayGroupStatus) {
        if (!canMoveTo(next)) {
            error("lenient relay group $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): LenientRelayGroup {
        return LenientRelayGroup(
            code = code,
            title = title,
            attributes = attributes,
            reviewer = reviewer,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient relay group in status draft. */
fun newLenientRelayGroup(code: String, title: String): LenientRelayGroup {
    return LenientRelayGroup(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        reviewer = "",
        expiresAt = 0,
        status = LenientRelayGroupStatus.DRAFT,
    )
}
