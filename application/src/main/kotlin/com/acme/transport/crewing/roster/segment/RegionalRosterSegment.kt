package com.acme.transport.crewing.roster.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional roster segment records of crewing's roster work, and the rules, stores, and calculations
// around them.

/** Where a regional roster segment is in its life cycle. */
enum class RegionalRosterSegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional roster segment. Its code is unique within a tenant. */
data class RegionalRosterSegment(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var archived: Boolean,
    var pallets: Int,
    var quantity: Int,
    var dock: String,
    var expiresAt: Int,
    var limit: Int,
    var summary: String,
    var status: RegionalRosterSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRosterSegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRosterSegmentStatus): Boolean {
        when (status) {
            RegionalRosterSegmentStatus.DRAFT -> {
                when (next) {
                    RegionalRosterSegmentStatus.ACTIVE, RegionalRosterSegmentStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalRosterSegmentStatus.PENDING -> {
                when (next) {
                    RegionalRosterSegmentStatus.ACTIVE, RegionalRosterSegmentStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalRosterSegmentStatus.ACTIVE -> return next == RegionalRosterSegmentStatus.CLOSED
            RegionalRosterSegmentStatus.SUSPENDED -> {
                when (next) {
                    RegionalRosterSegmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalRosterSegmentStatus) {
        if (!canMoveTo(next)) {
            error("regional roster segment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedLoad(): Double = round(expiresAt.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): RegionalRosterSegment {
        return RegionalRosterSegment(
            code = code,
            name = name,
            tags = tags,
            archived = archived,
            pallets = pallets,
            quantity = quantity,
            dock = dock,
            expiresAt = expiresAt,
            limit = limit,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional roster segment in status draft. */
fun newRegionalRosterSegment(code: String, name: String): RegionalRosterSegment {
    return RegionalRosterSegment(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        archived = false,
        pallets = 0,
        quantity = 0,
        dock = "",
        expiresAt = 0,
        limit = 0,
        summary = "",
        status = RegionalRosterSegmentStatus.DRAFT,
    )
}
