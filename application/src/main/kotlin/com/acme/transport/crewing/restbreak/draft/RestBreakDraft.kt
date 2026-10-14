package com.acme.transport.crewing.restbreak.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rest break draft records of crewing's rest break work, and the rules, stores, and calculations around
// them.

/** Where a rest break draft is in its life cycle. */
enum class RestBreakDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rest break draft as the rest break team keeps it. */
data class RestBreakDraft(
    var code: String,
    var name: String,
    var windowDays: Int,
    var expiresAt: Int,
    var label: String,
    var weight: Double,
    var category: String,
    var locale: String,
    var attempts: Int,
    var margin: Double,
    var status: RestBreakDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakDraftStatus): Boolean {
        when (status) {
            RestBreakDraftStatus.DRAFT -> return next == RestBreakDraftStatus.PENDING
            RestBreakDraftStatus.PENDING -> return next == RestBreakDraftStatus.SUSPENDED || next == RestBreakDraftStatus.CLOSED
            RestBreakDraftStatus.ACTIVE -> {
                when (next) {
                    RestBreakDraftStatus.SUSPENDED, RestBreakDraftStatus.CLOSED -> return true
                }
                return false
            }
            RestBreakDraftStatus.SUSPENDED -> {
                when (next) {
                    RestBreakDraftStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakDraftStatus) {
        if (!canMoveTo(next)) {
            error("rest break draft $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($name)"

    fun adjustedTotal(): Double = round(windowDays.toDouble() * margin * 100) / 100

    fun snapshot(): RestBreakDraft {
        return RestBreakDraft(
            code = code,
            name = name,
            windowDays = windowDays,
            expiresAt = expiresAt,
            label = label,
            weight = weight,
            category = category,
            locale = locale,
            attempts = attempts,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rest break draft in status draft. */
fun newRestBreakDraft(code: String, name: String): RestBreakDraft {
    return RestBreakDraft(
        code = code,
        name = name,
        windowDays = 0,
        expiresAt = 0,
        label = "",
        weight = 0.0,
        category = "",
        locale = "",
        attempts = 0,
        margin = 0.0,
        status = RestBreakDraftStatus.DRAFT,
    )
}
