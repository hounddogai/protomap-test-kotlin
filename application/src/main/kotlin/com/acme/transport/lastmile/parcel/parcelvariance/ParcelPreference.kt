package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel preference is in its life cycle. */
enum class ParcelPreferenceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One parcel preference, with what the planners need to act on it. */
data class ParcelPreference(
    var code: String,
    var label: String,
    var summary: String,
    var attempts: Int,
    var routeCode: String,
    var sequence: Int,
    var status: ParcelPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelPreferenceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelPreferenceStatus): Boolean {
        when (status) {
            ParcelPreferenceStatus.OPEN -> {
                when (next) {
                    ParcelPreferenceStatus.RESOLVED, ParcelPreferenceStatus.ARCHIVED -> return true
                }
                return false
            }
            ParcelPreferenceStatus.IN_REVIEW -> return next == ParcelPreferenceStatus.REOPENED || next == ParcelPreferenceStatus.ARCHIVED
            ParcelPreferenceStatus.RESOLVED -> return next == ParcelPreferenceStatus.REOPENED || next == ParcelPreferenceStatus.ARCHIVED
            ParcelPreferenceStatus.REOPENED -> return next == ParcelPreferenceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ParcelPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("parcel preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"parcel preference"} $code"

    fun weightedTotal(): Double = round((sequence.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): ParcelPreference {
        return ParcelPreference(
            code = code,
            label = label,
            summary = summary,
            attempts = attempts,
            routeCode = routeCode,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a parcel preference in status open. */
fun newParcelPreference(code: String, label: String): ParcelPreference {
    return ParcelPreference(
        code = code,
        label = label,
        summary = "",
        attempts = 0,
        routeCode = "",
        sequence = 0,
        status = ParcelPreferenceStatus.OPEN,
    )
}
