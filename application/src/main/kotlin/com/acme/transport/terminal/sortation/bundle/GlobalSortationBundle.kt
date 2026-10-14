package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.text.normalize

// The global sortation bundle records of terminal's sortation work, and the rules, stores, and calculations
// around them.

/** Where a global sortation bundle is in its life cycle. */
enum class GlobalSortationBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global sortation bundle. Its code is unique within a tenant. */
data class GlobalSortationBundle(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var archived: Boolean,
    var minimum: Double,
    var status: GlobalSortationBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalSortationBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalSortationBundleStatus): Boolean {
        when (status) {
            GlobalSortationBundleStatus.IDLE -> return next == GlobalSortationBundleStatus.WARMING || next == GlobalSortationBundleStatus.DRAINING
            GlobalSortationBundleStatus.WARMING -> {
                when (next) {
                    GlobalSortationBundleStatus.READY -> return true
                }
                return false
            }
            GlobalSortationBundleStatus.READY -> {
                when (next) {
                    GlobalSortationBundleStatus.STOPPED -> return true
                }
                return false
            }
            GlobalSortationBundleStatus.DRAINING -> {
                when (next) {
                    GlobalSortationBundleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalSortationBundleStatus) {
        if (!canMoveTo(next)) {
            error("global sortation bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = minimum

    fun snapshot(): GlobalSortationBundle {
        return GlobalSortationBundle(
            code = code,
            title = title,
            attributes = attributes,
            archived = archived,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global sortation bundle in status idle. */
fun newGlobalSortationBundle(code: String, title: String): GlobalSortationBundle {
    return GlobalSortationBundle(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        archived = false,
        minimum = 0.0,
        status = GlobalSortationBundleStatus.IDLE,
    )
}
