package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling profile is in its life cycle. */
enum class IdlingProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One idling profile, with what the planners need to act on it. */
data class IdlingProfile(
    var code: String,
    var label: String,
    var title: String,
    var distanceKm: Double,
    var ownerId: String,
    var ceiling: Double,
    var status: IdlingProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingProfileStatus): Boolean {
        when (status) {
            IdlingProfileStatus.CREATED -> {
                when (next) {
                    IdlingProfileStatus.VALIDATED, IdlingProfileStatus.RETIRED -> return true
                }
                return false
            }
            IdlingProfileStatus.VALIDATED -> {
                when (next) {
                    IdlingProfileStatus.RETIRED -> return true
                }
                return false
            }
            IdlingProfileStatus.PUBLISHED -> {
                when (next) {
                    IdlingProfileStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IdlingProfileStatus) {
        if (!canMoveTo(next)) {
            error("idling profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedValue(): Double = round(ceiling * distanceKm * 100) / 100

    fun snapshot(): IdlingProfile {
        return IdlingProfile(
            code = code,
            label = label,
            title = title,
            distanceKm = distanceKm,
            ownerId = ownerId,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a idling profile in status created. */
fun newIdlingProfile(code: String, label: String): IdlingProfile {
    return IdlingProfile(
        code = code,
        label = label,
        title = "",
        distanceKm = 0.0,
        ownerId = "",
        ceiling = 0.0,
        status = IdlingProfileStatus.CREATED,
    )
}
