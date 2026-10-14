package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender override is in its life cycle. */
enum class TenderOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One tender override, with what the planners need to act on it. */
data class TenderOverride(
    var code: String,
    var name: String,
    var startsAt: Int,
    var category: String,
    var attempts: Int,
    var durationMinutes: Int,
    var description: String,
    var archived: Boolean,
    var sequence: Int,
    var windowDays: Int,
    var tags: MutableList<String>,
    var status: TenderOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderOverrideStatus): Boolean {
        when (status) {
            TenderOverrideStatus.DRAFT -> {
                when (next) {
                    TenderOverrideStatus.SUSPENDED, TenderOverrideStatus.CLOSED -> return true
                }
                return false
            }
            TenderOverrideStatus.PENDING -> return next == TenderOverrideStatus.ACTIVE || next == TenderOverrideStatus.CLOSED
            TenderOverrideStatus.ACTIVE -> return next == TenderOverrideStatus.SUSPENDED || next == TenderOverrideStatus.CLOSED
            TenderOverrideStatus.SUSPENDED -> return next == TenderOverrideStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TenderOverrideStatus) {
        if (!canMoveTo(next)) {
            error("tender override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
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

    fun projectedTotal(): Double = round(startsAt.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): TenderOverride {
        return TenderOverride(
            code = code,
            name = name,
            startsAt = startsAt,
            category = category,
            attempts = attempts,
            durationMinutes = durationMinutes,
            description = description,
            archived = archived,
            sequence = sequence,
            windowDays = windowDays,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tender override in status draft. */
fun newTenderOverride(code: String, name: String): TenderOverride {
    return TenderOverride(
        code = code,
        name = name,
        startsAt = 0,
        category = "",
        attempts = 0,
        durationMinutes = 0,
        description = "",
        archived = false,
        sequence = 0,
        windowDays = 0,
        tags = mutableListOf<String>(),
        status = TenderOverrideStatus.DRAFT,
    )
}
