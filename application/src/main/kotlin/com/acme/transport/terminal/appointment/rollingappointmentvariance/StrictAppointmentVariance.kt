package com.acme.transport.terminal.appointment.rollingappointmentvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict appointment variance records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a strict appointment variance is in its life cycle. */
enum class StrictAppointmentVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One strict appointment variance, with what the planners need to act on it. */
data class StrictAppointmentVariance(
    var code: String,
    var name: String,
    var batchSize: Int,
    var score: Double,
    var ceiling: Double,
    var country: String,
    var budget: Double,
    var rate: Double,
    var unit: String,
    var region: String,
    var expiresAt: Int,
    var status: StrictAppointmentVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAppointmentVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAppointmentVarianceStatus): Boolean {
        when (status) {
            StrictAppointmentVarianceStatus.NEW -> {
                when (next) {
                    StrictAppointmentVarianceStatus.RUNNING, StrictAppointmentVarianceStatus.FAILED -> return true
                }
                return false
            }
            StrictAppointmentVarianceStatus.QUEUED -> {
                when (next) {
                    StrictAppointmentVarianceStatus.FAILED -> return true
                }
                return false
            }
            StrictAppointmentVarianceStatus.RUNNING -> return next == StrictAppointmentVarianceStatus.DONE
            StrictAppointmentVarianceStatus.DONE -> {
                when (next) {
                    StrictAppointmentVarianceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictAppointmentVarianceStatus) {
        if (!canMoveTo(next)) {
            error("strict appointment variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict appointment variance"} $code"

    fun netVolume(): Double = round((budget + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): StrictAppointmentVariance {
        return StrictAppointmentVariance(
            code = code,
            name = name,
            batchSize = batchSize,
            score = score,
            ceiling = ceiling,
            country = country,
            budget = budget,
            rate = rate,
            unit = unit,
            region = region,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict appointment variance in status new. */
fun newStrictAppointmentVariance(code: String, name: String): StrictAppointmentVariance {
    return StrictAppointmentVariance(
        code = code,
        name = name,
        batchSize = 0,
        score = 0.0,
        ceiling = 0.0,
        country = "",
        budget = 0.0,
        rate = 0.0,
        unit = "",
        region = "",
        expiresAt = 0,
        status = StrictAppointmentVarianceStatus.NEW,
    )
}
