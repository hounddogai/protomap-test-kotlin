package com.acme.transport.freightpay.ratecard.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default rate card entry records of freightpay's rate card work, and the rules, stores, and calculations
// around them.

/** Where a default rate card entry is in its life cycle. */
enum class DefaultRateCardEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a default rate card entry and its current status. */
data class DefaultRateCardEntry(
    var code: String,
    var title: String,
    var category: String,
    var createdAt: Int,
    var revision: Int,
    var routeCode: String,
    var version: Int,
    var updatedAt: Int,
    var durationMinutes: Int,
    var status: DefaultRateCardEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRateCardEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRateCardEntryStatus): Boolean {
        when (status) {
            DefaultRateCardEntryStatus.OPEN -> {
                when (next) {
                    DefaultRateCardEntryStatus.IN_REVIEW, DefaultRateCardEntryStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultRateCardEntryStatus.IN_REVIEW -> {
                when (next) {
                    DefaultRateCardEntryStatus.REOPENED -> return true
                }
                return false
            }
            DefaultRateCardEntryStatus.RESOLVED -> {
                when (next) {
                    DefaultRateCardEntryStatus.REOPENED -> return true
                }
                return false
            }
            DefaultRateCardEntryStatus.REOPENED -> {
                when (next) {
                    DefaultRateCardEntryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRateCardEntryStatus) {
        if (!canMoveTo(next)) {
            error("default rate card entry $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"default rate card entry"} $code"

    fun weightedVolume(): Double = round((durationMinutes.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): DefaultRateCardEntry {
        return DefaultRateCardEntry(
            code = code,
            title = title,
            category = category,
            createdAt = createdAt,
            revision = revision,
            routeCode = routeCode,
            version = version,
            updatedAt = updatedAt,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default rate card entry in status open. */
fun newDefaultRateCardEntry(code: String, title: String): DefaultRateCardEntry {
    return DefaultRateCardEntry(
        code = code,
        title = title,
        category = "",
        createdAt = 0,
        revision = 0,
        routeCode = "",
        version = 0,
        updatedAt = 0,
        durationMinutes = 0,
        status = DefaultRateCardEntryStatus.OPEN,
    )
}
