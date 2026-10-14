package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global locker slot is in its life cycle. */
enum class GlobalLockerSlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One global locker slot, with what the planners need to act on it. */
data class GlobalLockerSlot(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var rate: Double,
    var description: String,
    var dock: String,
    var attempts: Int,
    var updatedAt: Int,
    var status: GlobalLockerSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalLockerSlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalLockerSlotStatus): Boolean {
        when (status) {
            GlobalLockerSlotStatus.PROPOSED -> return next == GlobalLockerSlotStatus.WITHDRAWN
            GlobalLockerSlotStatus.APPROVED -> return next == GlobalLockerSlotStatus.REJECTED || next == GlobalLockerSlotStatus.WITHDRAWN
            GlobalLockerSlotStatus.REJECTED -> return next == GlobalLockerSlotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalLockerSlotStatus) {
        if (!canMoveTo(next)) {
            error("global locker slot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round((updatedAt.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): GlobalLockerSlot {
        return GlobalLockerSlot(
            code = code,
            title = title,
            attributes = attributes,
            rate = rate,
            description = description,
            dock = dock,
            attempts = attempts,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global locker slot in status proposed. */
fun newGlobalLockerSlot(code: String, title: String): GlobalLockerSlot {
    return GlobalLockerSlot(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        rate = 0.0,
        description = "",
        dock = "",
        attempts = 0,
        updatedAt = 0,
        status = GlobalLockerSlotStatus.PROPOSED,
    )
}
