package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.text.normalize

/** Where a sortation rollup is in its life cycle. */
enum class SortationRollupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a sortation rollup and its current status. */
data class SortationRollup(
    var code: String,
    var title: String,
    var dock: String,
    var revision: Int,
    var attributes: MutableMap<String, String>,
    var phone: String,
    var status: SortationRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationRollupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationRollupStatus): Boolean {
        when (status) {
            SortationRollupStatus.DRAFT -> {
                when (next) {
                    SortationRollupStatus.ACTIVE, SortationRollupStatus.CLOSED -> return true
                }
                return false
            }
            SortationRollupStatus.PENDING -> return next == SortationRollupStatus.ACTIVE
            SortationRollupStatus.ACTIVE -> {
                when (next) {
                    SortationRollupStatus.SUSPENDED -> return true
                }
                return false
            }
            SortationRollupStatus.SUSPENDED -> return next == SortationRollupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SortationRollupStatus) {
        if (!canMoveTo(next)) {
            error("sortation rollup $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): SortationRollup {
        return SortationRollup(
            code = code,
            title = title,
            dock = dock,
            revision = revision,
            attributes = attributes,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a sortation rollup in status draft. */
fun newSortationRollup(code: String, title: String): SortationRollup {
    return SortationRollup(
        code = code,
        title = title,
        dock = "",
        revision = 0,
        attributes = mutableMapOf<String, String>(),
        phone = "",
        status = SortationRollupStatus.DRAFT,
    )
}
