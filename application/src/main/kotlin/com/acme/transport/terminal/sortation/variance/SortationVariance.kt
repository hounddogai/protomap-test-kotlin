package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The sortation variance records of terminal's sortation work, and the rules, stores, and calculations around
// them.

/** Where a sortation variance is in its life cycle. */
enum class SortationVarianceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One sortation variance, with what the planners need to act on it. */
data class SortationVariance(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var description: String,
    var pinned: Boolean,
    var batchSize: Int,
    var ratio: Double,
    var version: Int,
    var priority: Int,
    var score: Double,
    var capacity: Int,
    var status: SortationVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationVarianceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationVarianceStatus): Boolean {
        when (status) {
            SortationVarianceStatus.REQUESTED -> {
                when (next) {
                    SortationVarianceStatus.GRANTED -> return true
                }
                return false
            }
            SortationVarianceStatus.GRANTED -> {
                when (next) {
                    SortationVarianceStatus.EXPIRED -> return true
                }
                return false
            }
            SortationVarianceStatus.REVOKED -> {
                when (next) {
                    SortationVarianceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationVarianceStatus) {
        if (!canMoveTo(next)) {
            error("sortation variance $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun effectiveLoad(): Double = round((priority.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): SortationVariance {
        return SortationVariance(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            description = description,
            pinned = pinned,
            batchSize = batchSize,
            ratio = ratio,
            version = version,
            priority = priority,
            score = score,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a sortation variance in status requested. */
fun newSortationVariance(code: String, name: String): SortationVariance {
    return SortationVariance(
        code = code,
        name = name,
        durationMinutes = 0,
        description = "",
        pinned = false,
        batchSize = 0,
        ratio = 0.0,
        version = 0,
        priority = 0,
        score = 0.0,
        capacity = 0,
        status = SortationVarianceStatus.REQUESTED,
    )
}
