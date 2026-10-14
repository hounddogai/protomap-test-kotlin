package com.acme.transport.lastmile.locker.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual locker preference is in its life cycle. */
enum class ManualLockerPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a manual locker preference and its current status. */
data class ManualLockerPreference(
    var code: String,
    var title: String,
    var archived: Boolean,
    var weight: Double,
    var pallets: Int,
    var status: ManualLockerPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualLockerPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualLockerPreferenceStatus): Boolean {
        when (status) {
            ManualLockerPreferenceStatus.PLANNED -> {
                when (next) {
                    ManualLockerPreferenceStatus.SCHEDULED, ManualLockerPreferenceStatus.COMPLETED -> return true
                }
                return false
            }
            ManualLockerPreferenceStatus.SCHEDULED -> {
                when (next) {
                    ManualLockerPreferenceStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ManualLockerPreferenceStatus.IN_PROGRESS -> {
                when (next) {
                    ManualLockerPreferenceStatus.COMPLETED -> return true
                }
                return false
            }
            ManualLockerPreferenceStatus.COMPLETED -> {
                when (next) {
                    ManualLockerPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualLockerPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("manual locker preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual locker preference"} $code"

    fun effectiveValue(): Double = round(pallets.toDouble() * weight * 100) / 100

    fun snapshot(): ManualLockerPreference {
        return ManualLockerPreference(
            code = code,
            title = title,
            archived = archived,
            weight = weight,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual locker preference in status planned. */
fun newManualLockerPreference(code: String, title: String): ManualLockerPreference {
    return ManualLockerPreference(
        code = code,
        title = title,
        archived = false,
        weight = 0.0,
        pallets = 0,
        status = ManualLockerPreferenceStatus.PLANNED,
    )
}
