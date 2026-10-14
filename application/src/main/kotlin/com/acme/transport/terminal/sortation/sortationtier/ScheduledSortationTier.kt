package com.acme.transport.terminal.sortation.sortationtier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled sortation tier records of terminal's sortation work, and the rules, stores, and calculations
// around them.

/** Where a scheduled sortation tier is in its life cycle. */
enum class ScheduledSortationTierStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One scheduled sortation tier, with what the planners need to act on it. */
data class ScheduledSortationTier(
    var code: String,
    var name: String,
    var score: Double,
    var dock: String,
    var margin: Double,
    var batchSize: Int,
    var createdAt: Int,
    var quantity: Int,
    var loadKg: Double,
    var attributes: MutableMap<String, String>,
    var status: ScheduledSortationTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledSortationTierStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledSortationTierStatus): Boolean {
        when (status) {
            ScheduledSortationTierStatus.DRAFT -> {
                when (next) {
                    ScheduledSortationTierStatus.PENDING, ScheduledSortationTierStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledSortationTierStatus.PENDING -> return next == ScheduledSortationTierStatus.SUSPENDED || next == ScheduledSortationTierStatus.CLOSED
            ScheduledSortationTierStatus.ACTIVE -> {
                when (next) {
                    ScheduledSortationTierStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledSortationTierStatus.SUSPENDED -> {
                when (next) {
                    ScheduledSortationTierStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledSortationTierStatus) {
        if (!canMoveTo(next)) {
            error("scheduled sortation tier $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((createdAt.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): ScheduledSortationTier {
        return ScheduledSortationTier(
            code = code,
            name = name,
            score = score,
            dock = dock,
            margin = margin,
            batchSize = batchSize,
            createdAt = createdAt,
            quantity = quantity,
            loadKg = loadKg,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled sortation tier in status draft. */
fun newScheduledSortationTier(code: String, name: String): ScheduledSortationTier {
    return ScheduledSortationTier(
        code = code,
        name = name,
        score = 0.0,
        dock = "",
        margin = 0.0,
        batchSize = 0,
        createdAt = 0,
        quantity = 0,
        loadKg = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = ScheduledSortationTierStatus.DRAFT,
    )
}
