package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell rollup is in its life cycle. */
enum class DwellRollupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One dwell rollup, with what the planners need to act on it. */
data class DwellRollup(
    var code: String,
    var title: String,
    var timezone: String,
    var createdAt: Int,
    var updatedAt: Int,
    var summary: String,
    var threshold: Double,
    var dock: String,
    var tags: MutableList<String>,
    var batchSize: Int,
    var contactEmail: String,
    var status: DwellRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellRollupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellRollupStatus): Boolean {
        when (status) {
            DwellRollupStatus.DRAFT -> return next == DwellRollupStatus.PENDING || next == DwellRollupStatus.ACTIVE
            DwellRollupStatus.PENDING -> return next == DwellRollupStatus.CLOSED
            DwellRollupStatus.ACTIVE -> {
                when (next) {
                    DwellRollupStatus.SUSPENDED, DwellRollupStatus.CLOSED -> return true
                }
                return false
            }
            DwellRollupStatus.SUSPENDED -> return next == DwellRollupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DwellRollupStatus) {
        if (!canMoveTo(next)) {
            error("dwell rollup $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round((updatedAt.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): DwellRollup {
        return DwellRollup(
            code = code,
            title = title,
            timezone = timezone,
            createdAt = createdAt,
            updatedAt = updatedAt,
            summary = summary,
            threshold = threshold,
            dock = dock,
            tags = tags,
            batchSize = batchSize,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a dwell rollup in status draft. */
fun newDwellRollup(code: String, title: String): DwellRollup {
    return DwellRollup(
        code = code,
        title = title,
        timezone = "",
        createdAt = 0,
        updatedAt = 0,
        summary = "",
        threshold = 0.0,
        dock = "",
        tags = mutableListOf<String>(),
        batchSize = 0,
        contactEmail = "",
        status = DwellRollupStatus.DRAFT,
    )
}
