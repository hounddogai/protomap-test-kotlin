package com.acme.transport.lastmile.parcel.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The parcel plan records of lastmile's parcel work, and the rules, stores, and calculations around them.

/** Where a parcel plan is in its life cycle. */
enum class ParcelPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One parcel plan, with what the planners need to act on it. */
data class ParcelPlan(
    var code: String,
    var name: String,
    var notes: String,
    var revision: Int,
    var shiftCode: String,
    var capacity: Int,
    var discount: Double,
    var priority: Int,
    var sequence: Int,
    var country: String,
    var startsAt: Int,
    var status: ParcelPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelPlanStatus): Boolean {
        when (status) {
            ParcelPlanStatus.CREATED -> return next == ParcelPlanStatus.PUBLISHED
            ParcelPlanStatus.VALIDATED -> {
                when (next) {
                    ParcelPlanStatus.PUBLISHED, ParcelPlanStatus.RETIRED -> return true
                }
                return false
            }
            ParcelPlanStatus.PUBLISHED -> {
                when (next) {
                    ParcelPlanStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelPlanStatus) {
        if (!canMoveTo(next)) {
            error("parcel plan $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun netExposure(): Double = round((startsAt.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): ParcelPlan {
        return ParcelPlan(
            code = code,
            name = name,
            notes = notes,
            revision = revision,
            shiftCode = shiftCode,
            capacity = capacity,
            discount = discount,
            priority = priority,
            sequence = sequence,
            country = country,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a parcel plan in status created. */
fun newParcelPlan(code: String, name: String): ParcelPlan {
    return ParcelPlan(
        code = code,
        name = name,
        notes = "",
        revision = 0,
        shiftCode = "",
        capacity = 0,
        discount = 0.0,
        priority = 0,
        sequence = 0,
        country = "",
        startsAt = 0,
        status = ParcelPlanStatus.CREATED,
    )
}
