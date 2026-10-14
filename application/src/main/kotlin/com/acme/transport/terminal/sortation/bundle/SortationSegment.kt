package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation segment is in its life cycle. */
enum class SortationSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A sortation segment as the sortation team keeps it. */
data class SortationSegment(
    var code: String,
    var label: String,
    var rate: Double,
    var margin: Double,
    var expiresAt: Int,
    var attributes: MutableMap<String, String>,
    var archived: Boolean,
    var trailerId: String,
    var approved: Boolean,
    var visible: Boolean,
    var phone: String,
    var status: SortationSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationSegmentStatus): Boolean {
        when (status) {
            SortationSegmentStatus.OPEN -> return next == SortationSegmentStatus.IN_REVIEW || next == SortationSegmentStatus.REOPENED
            SortationSegmentStatus.IN_REVIEW -> {
                when (next) {
                    SortationSegmentStatus.RESOLVED, SortationSegmentStatus.REOPENED -> return true
                }
                return false
            }
            SortationSegmentStatus.RESOLVED -> return next == SortationSegmentStatus.REOPENED || next == SortationSegmentStatus.ARCHIVED
            SortationSegmentStatus.REOPENED -> {
                when (next) {
                    SortationSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationSegmentStatus) {
        if (!canMoveTo(next)) {
            error("sortation segment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"sortation segment"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round(margin * expiresAt.toDouble() * 100) / 100

    fun snapshot(): SortationSegment {
        return SortationSegment(
            code = code,
            label = label,
            rate = rate,
            margin = margin,
            expiresAt = expiresAt,
            attributes = attributes,
            archived = archived,
            trailerId = trailerId,
            approved = approved,
            visible = visible,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a sortation segment in status open. */
fun newSortationSegment(code: String, label: String): SortationSegment {
    return SortationSegment(
        code = code,
        label = label,
        rate = 0.0,
        margin = 0.0,
        expiresAt = 0,
        attributes = mutableMapOf<String, String>(),
        archived = false,
        trailerId = "",
        approved = false,
        visible = false,
        phone = "",
        status = SortationSegmentStatus.OPEN,
    )
}
