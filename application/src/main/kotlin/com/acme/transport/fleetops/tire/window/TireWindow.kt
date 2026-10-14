package com.acme.transport.fleetops.tire.window

import com.acme.transport.kit.text.normalize

// The tire window records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a tire window is in its life cycle. */
enum class TireWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a tire window and its current status. */
data class TireWindow(
    var code: String,
    var title: String,
    var summary: String,
    var updatedAt: Int,
    var archived: Boolean,
    var status: TireWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireWindowStatus): Boolean {
        when (status) {
            TireWindowStatus.CREATED -> return next == TireWindowStatus.VALIDATED
            TireWindowStatus.VALIDATED -> return next == TireWindowStatus.PUBLISHED || next == TireWindowStatus.RETIRED
            TireWindowStatus.PUBLISHED -> {
                when (next) {
                    TireWindowStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireWindowStatus) {
        if (!canMoveTo(next)) {
            error("tire window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): TireWindow {
        return TireWindow(
            code = code,
            title = title,
            summary = summary,
            updatedAt = updatedAt,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tire window in status created. */
fun newTireWindow(code: String, title: String): TireWindow {
    return TireWindow(
        code = code,
        title = title,
        summary = "",
        updatedAt = 0,
        archived = false,
        status = TireWindowStatus.CREATED,
    )
}
