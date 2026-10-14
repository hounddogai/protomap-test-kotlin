package com.acme.transport.network.transittime.mapping

import com.acme.transport.kit.text.normalize

// The transit time mapping records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time mapping is in its life cycle. */
enum class TransitTimeMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A transit time mapping. Its code is unique within a tenant. */
data class TransitTimeMapping(
    var code: String,
    var title: String,
    var summary: String,
    var pinned: Boolean,
    var cost: Double,
    var ownerId: String,
    var status: TransitTimeMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeMappingStatus): Boolean {
        when (status) {
            TransitTimeMappingStatus.PLANNED -> {
                when (next) {
                    TransitTimeMappingStatus.COMPLETED -> return true
                }
                return false
            }
            TransitTimeMappingStatus.SCHEDULED -> return next == TransitTimeMappingStatus.CANCELLED
            TransitTimeMappingStatus.IN_PROGRESS -> {
                when (next) {
                    TransitTimeMappingStatus.COMPLETED, TransitTimeMappingStatus.CANCELLED -> return true
                }
                return false
            }
            TransitTimeMappingStatus.COMPLETED -> return next == TransitTimeMappingStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TransitTimeMappingStatus) {
        if (!canMoveTo(next)) {
            error("transit time mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = cost

    fun snapshot(): TransitTimeMapping {
        return TransitTimeMapping(
            code = code,
            title = title,
            summary = summary,
            pinned = pinned,
            cost = cost,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a transit time mapping in status planned. */
fun newTransitTimeMapping(code: String, title: String): TransitTimeMapping {
    return TransitTimeMapping(
        code = code,
        title = title,
        summary = "",
        pinned = false,
        cost = 0.0,
        ownerId = "",
        status = TransitTimeMappingStatus.PLANNED,
    )
}
