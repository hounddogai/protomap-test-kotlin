package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard target is in its life cycle. */
enum class TrailerYardTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A trailer yard target. Its code is unique within a tenant. */
data class TrailerYardTarget(
    var code: String,
    var name: String,
    var contactEmail: String,
    var archived: Boolean,
    var limit: Int,
    var ownerId: String,
    var segment: String,
    var distanceKm: Double,
    var status: TrailerYardTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardTargetStatus): Boolean {
        when (status) {
            TrailerYardTargetStatus.OPEN -> return next == TrailerYardTargetStatus.RESOLVED || next == TrailerYardTargetStatus.ARCHIVED
            TrailerYardTargetStatus.IN_REVIEW -> {
                when (next) {
                    TrailerYardTargetStatus.RESOLVED, TrailerYardTargetStatus.REOPENED -> return true
                }
                return false
            }
            TrailerYardTargetStatus.RESOLVED -> return next == TrailerYardTargetStatus.REOPENED
            TrailerYardTargetStatus.REOPENED -> return next == TrailerYardTargetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TrailerYardTargetStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netValue(): Double = round((limit.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): TrailerYardTarget {
        return TrailerYardTarget(
            code = code,
            name = name,
            contactEmail = contactEmail,
            archived = archived,
            limit = limit,
            ownerId = ownerId,
            segment = segment,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a trailer yard target in status open. */
fun newTrailerYardTarget(code: String, name: String): TrailerYardTarget {
    return TrailerYardTarget(
        code = code,
        name = name,
        contactEmail = "",
        archived = false,
        limit = 0,
        ownerId = "",
        segment = "",
        distanceKm = 0.0,
        status = TrailerYardTargetStatus.OPEN,
    )
}
