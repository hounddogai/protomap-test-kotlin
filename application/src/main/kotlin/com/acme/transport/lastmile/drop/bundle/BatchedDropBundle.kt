package com.acme.transport.lastmile.drop.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched drop bundle records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a batched drop bundle is in its life cycle. */
enum class BatchedDropBundleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One batched drop bundle, with what the planners need to act on it. */
data class BatchedDropBundle(
    var code: String,
    var title: String,
    var graceDays: Int,
    var etaMinutes: Int,
    var country: String,
    var depot: String,
    var channel: String,
    var dock: String,
    var priority: Int,
    var attributes: MutableMap<String, String>,
    var status: BatchedDropBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDropBundleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDropBundleStatus): Boolean {
        when (status) {
            BatchedDropBundleStatus.PROPOSED -> {
                when (next) {
                    BatchedDropBundleStatus.APPROVED, BatchedDropBundleStatus.REJECTED -> return true
                }
                return false
            }
            BatchedDropBundleStatus.APPROVED -> {
                when (next) {
                    BatchedDropBundleStatus.REJECTED, BatchedDropBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedDropBundleStatus.REJECTED -> return next == BatchedDropBundleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedDropBundleStatus) {
        if (!canMoveTo(next)) {
            error("batched drop bundle $code cannot move from $status to $next")
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

    fun weightedExposure(): Double = round(graceDays.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): BatchedDropBundle {
        return BatchedDropBundle(
            code = code,
            title = title,
            graceDays = graceDays,
            etaMinutes = etaMinutes,
            country = country,
            depot = depot,
            channel = channel,
            dock = dock,
            priority = priority,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched drop bundle in status proposed. */
fun newBatchedDropBundle(code: String, title: String): BatchedDropBundle {
    return BatchedDropBundle(
        code = code,
        title = title,
        graceDays = 0,
        etaMinutes = 0,
        country = "",
        depot = "",
        channel = "",
        dock = "",
        priority = 0,
        attributes = mutableMapOf<String, String>(),
        status = BatchedDropBundleStatus.PROPOSED,
    )
}
