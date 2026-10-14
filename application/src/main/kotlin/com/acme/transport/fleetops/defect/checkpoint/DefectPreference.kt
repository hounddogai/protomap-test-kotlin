package com.acme.transport.fleetops.defect.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect preference is in its life cycle. */
enum class DefectPreferenceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One defect preference, with what the planners need to act on it. */
data class DefectPreference(
    var code: String,
    var title: String,
    var startsAt: Int,
    var rate: Double,
    var limit: Int,
    var carrier: String,
    var depot: String,
    var contactEmail: String,
    var batchSize: Int,
    var locale: String,
    var status: DefectPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectPreferenceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectPreferenceStatus): Boolean {
        when (status) {
            DefectPreferenceStatus.DRAFT -> return next == DefectPreferenceStatus.ACTIVE
            DefectPreferenceStatus.PENDING -> {
                when (next) {
                    DefectPreferenceStatus.ACTIVE, DefectPreferenceStatus.SUSPENDED -> return true
                }
                return false
            }
            DefectPreferenceStatus.ACTIVE -> {
                when (next) {
                    DefectPreferenceStatus.SUSPENDED, DefectPreferenceStatus.CLOSED -> return true
                }
                return false
            }
            DefectPreferenceStatus.SUSPENDED -> {
                when (next) {
                    DefectPreferenceStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("defect preference $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"defect preference"} $code"

    fun adjustedLoad(): Double = round(batchSize.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): DefectPreference {
        return DefectPreference(
            code = code,
            title = title,
            startsAt = startsAt,
            rate = rate,
            limit = limit,
            carrier = carrier,
            depot = depot,
            contactEmail = contactEmail,
            batchSize = batchSize,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a defect preference in status draft. */
fun newDefectPreference(code: String, title: String): DefectPreference {
    return DefectPreference(
        code = code,
        title = title,
        startsAt = 0,
        rate = 0.0,
        limit = 0,
        carrier = "",
        depot = "",
        contactEmail = "",
        batchSize = 0,
        locale = "",
        status = DefectPreferenceStatus.DRAFT,
    )
}
