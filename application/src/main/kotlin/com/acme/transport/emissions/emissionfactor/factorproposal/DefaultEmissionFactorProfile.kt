package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default emission factor profile is in its life cycle. */
enum class DefaultEmissionFactorProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One default emission factor profile, with what the planners need to act on it. */
data class DefaultEmissionFactorProfile(
    var code: String,
    var title: String,
    var createdAt: Int,
    var graceDays: Int,
    var carrier: String,
    var category: String,
    var weight: Double,
    var status: DefaultEmissionFactorProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultEmissionFactorProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultEmissionFactorProfileStatus): Boolean {
        when (status) {
            DefaultEmissionFactorProfileStatus.PLANNED -> {
                when (next) {
                    DefaultEmissionFactorProfileStatus.IN_PROGRESS, DefaultEmissionFactorProfileStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultEmissionFactorProfileStatus.SCHEDULED -> {
                when (next) {
                    DefaultEmissionFactorProfileStatus.IN_PROGRESS, DefaultEmissionFactorProfileStatus.COMPLETED -> return true
                }
                return false
            }
            DefaultEmissionFactorProfileStatus.IN_PROGRESS -> {
                when (next) {
                    DefaultEmissionFactorProfileStatus.COMPLETED -> return true
                }
                return false
            }
            DefaultEmissionFactorProfileStatus.COMPLETED -> return next == DefaultEmissionFactorProfileStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DefaultEmissionFactorProfileStatus) {
        if (!canMoveTo(next)) {
            error("default emission factor profile $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default emission factor profile"} $code"

    fun netVolume(): Double = round((weight - graceDays.toDouble()) * 100) / 100

    fun snapshot(): DefaultEmissionFactorProfile {
        return DefaultEmissionFactorProfile(
            code = code,
            title = title,
            createdAt = createdAt,
            graceDays = graceDays,
            carrier = carrier,
            category = category,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default emission factor profile in status planned. */
fun newDefaultEmissionFactorProfile(code: String, title: String): DefaultEmissionFactorProfile {
    return DefaultEmissionFactorProfile(
        code = code,
        title = title,
        createdAt = 0,
        graceDays = 0,
        carrier = "",
        category = "",
        weight = 0.0,
        status = DefaultEmissionFactorProfileStatus.PLANNED,
    )
}
