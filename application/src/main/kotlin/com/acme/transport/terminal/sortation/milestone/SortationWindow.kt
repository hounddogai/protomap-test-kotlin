package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation window is in its life cycle. */
enum class SortationWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A sortation window. Its code is unique within a tenant. */
data class SortationWindow(
    var code: String,
    var title: String,
    var rate: Double,
    var checksum: String,
    var externalRef: String,
    var phone: String,
    var updatedAt: Int,
    var label: String,
    var channel: String,
    var timezone: String,
    var pinned: Boolean,
    var status: SortationWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationWindowStatus): Boolean {
        when (status) {
            SortationWindowStatus.DRAFT -> return next == SortationWindowStatus.SUSPENDED
            SortationWindowStatus.PENDING -> {
                when (next) {
                    SortationWindowStatus.CLOSED -> return true
                }
                return false
            }
            SortationWindowStatus.ACTIVE -> return next == SortationWindowStatus.CLOSED
            SortationWindowStatus.SUSPENDED -> {
                when (next) {
                    SortationWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationWindowStatus) {
        if (!canMoveTo(next)) {
            error("sortation window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $title"

    fun weightedTotal(): Double = round((updatedAt.toDouble() - rate) * 100) / 100

    fun snapshot(): SortationWindow {
        return SortationWindow(
            code = code,
            title = title,
            rate = rate,
            checksum = checksum,
            externalRef = externalRef,
            phone = phone,
            updatedAt = updatedAt,
            label = label,
            channel = channel,
            timezone = timezone,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a sortation window in status draft. */
fun newSortationWindow(code: String, title: String): SortationWindow {
    return SortationWindow(
        code = code,
        title = title,
        rate = 0.0,
        checksum = "",
        externalRef = "",
        phone = "",
        updatedAt = 0,
        label = "",
        channel = "",
        timezone = "",
        pinned = false,
        status = SortationWindowStatus.DRAFT,
    )
}
