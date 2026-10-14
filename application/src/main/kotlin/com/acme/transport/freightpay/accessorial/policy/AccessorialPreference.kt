package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial preference is in its life cycle. */
enum class AccessorialPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One accessorial preference, with what the planners need to act on it. */
data class AccessorialPreference(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var locale: String,
    var shiftCode: String,
    var currency: String,
    var weight: Double,
    var maximum: Double,
    var status: AccessorialPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialPreferenceStatus): Boolean {
        when (status) {
            AccessorialPreferenceStatus.PLANNED -> return next == AccessorialPreferenceStatus.SCHEDULED
            AccessorialPreferenceStatus.SCHEDULED -> {
                when (next) {
                    AccessorialPreferenceStatus.IN_PROGRESS, AccessorialPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
            AccessorialPreferenceStatus.IN_PROGRESS -> return next == AccessorialPreferenceStatus.COMPLETED
            AccessorialPreferenceStatus.COMPLETED -> {
                when (next) {
                    AccessorialPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("accessorial preference $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round(maximum * updatedAt.toDouble() * 100) / 100

    fun snapshot(): AccessorialPreference {
        return AccessorialPreference(
            code = code,
            title = title,
            updatedAt = updatedAt,
            locale = locale,
            shiftCode = shiftCode,
            currency = currency,
            weight = weight,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a accessorial preference in status planned. */
fun newAccessorialPreference(code: String, title: String): AccessorialPreference {
    return AccessorialPreference(
        code = code,
        title = title,
        updatedAt = 0,
        locale = "",
        shiftCode = "",
        currency = "",
        weight = 0.0,
        maximum = 0.0,
        status = AccessorialPreferenceStatus.PLANNED,
    )
}
