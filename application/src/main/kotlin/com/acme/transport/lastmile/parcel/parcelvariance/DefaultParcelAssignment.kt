package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default parcel assignment is in its life cycle. */
enum class DefaultParcelAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a default parcel assignment and its current status. */
data class DefaultParcelAssignment(
    var code: String,
    var name: String,
    var cost: Double,
    var country: String,
    var routeCode: String,
    var driverId: String,
    var channel: String,
    var tags: MutableList<String>,
    var ownerId: String,
    var priority: Int,
    var minimum: Double,
    var status: DefaultParcelAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultParcelAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultParcelAssignmentStatus): Boolean {
        when (status) {
            DefaultParcelAssignmentStatus.CREATED -> return next == DefaultParcelAssignmentStatus.PUBLISHED
            DefaultParcelAssignmentStatus.VALIDATED -> {
                when (next) {
                    DefaultParcelAssignmentStatus.RETIRED -> return true
                }
                return false
            }
            DefaultParcelAssignmentStatus.PUBLISHED -> return next == DefaultParcelAssignmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultParcelAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("default parcel assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round(minimum * cost * 100) / 100

    fun snapshot(): DefaultParcelAssignment {
        return DefaultParcelAssignment(
            code = code,
            name = name,
            cost = cost,
            country = country,
            routeCode = routeCode,
            driverId = driverId,
            channel = channel,
            tags = tags,
            ownerId = ownerId,
            priority = priority,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a default parcel assignment in status created. */
fun newDefaultParcelAssignment(code: String, name: String): DefaultParcelAssignment {
    return DefaultParcelAssignment(
        code = code,
        name = name,
        cost = 0.0,
        country = "",
        routeCode = "",
        driverId = "",
        channel = "",
        tags = mutableListOf<String>(),
        ownerId = "",
        priority = 0,
        minimum = 0.0,
        status = DefaultParcelAssignmentStatus.CREATED,
    )
}
