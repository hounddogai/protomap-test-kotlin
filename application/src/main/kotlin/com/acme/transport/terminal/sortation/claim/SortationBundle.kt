package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation bundle is in its life cycle. */
enum class SortationBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a sortation bundle and its current status. */
data class SortationBundle(
    var code: String,
    var title: String,
    var externalRef: String,
    var createdAt: Int,
    var priority: Int,
    var reviewer: String,
    var cost: Double,
    var carrier: String,
    var stopCount: Int,
    var status: SortationBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationBundleStatus): Boolean {
        when (status) {
            SortationBundleStatus.OPEN -> {
                when (next) {
                    SortationBundleStatus.RESOLVED -> return true
                }
                return false
            }
            SortationBundleStatus.IN_REVIEW -> return next == SortationBundleStatus.REOPENED
            SortationBundleStatus.RESOLVED -> return next == SortationBundleStatus.REOPENED || next == SortationBundleStatus.ARCHIVED
            SortationBundleStatus.REOPENED -> {
                when (next) {
                    SortationBundleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationBundleStatus) {
        if (!canMoveTo(next)) {
            error("sortation bundle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"sortation bundle"} $code"

    fun netVolume(): Double = round(cost * priority.toDouble() * 100) / 100

    fun snapshot(): SortationBundle {
        return SortationBundle(
            code = code,
            title = title,
            externalRef = externalRef,
            createdAt = createdAt,
            priority = priority,
            reviewer = reviewer,
            cost = cost,
            carrier = carrier,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a sortation bundle in status open. */
fun newSortationBundle(code: String, title: String): SortationBundle {
    return SortationBundle(
        code = code,
        title = title,
        externalRef = "",
        createdAt = 0,
        priority = 0,
        reviewer = "",
        cost = 0.0,
        carrier = "",
        stopCount = 0,
        status = SortationBundleStatus.OPEN,
    )
}
