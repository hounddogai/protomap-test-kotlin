package com.acme.transport.emissions.emissionfactor.factorschedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional emission factor schedule records of emissions's emission factor work, and the rules, stores,
// and calculations around them.

/** Where a regional emission factor schedule is in its life cycle. */
enum class RegionalEmissionFactorScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A regional emission factor schedule. Its code is unique within a tenant. */
data class RegionalEmissionFactorSchedule(
    var code: String,
    var title: String,
    var createdAt: Int,
    var margin: Double,
    var sequence: Int,
    var updatedAt: Int,
    var attempts: Int,
    var durationMinutes: Int,
    var threshold: Double,
    var dock: String,
    var approved: Boolean,
    var status: RegionalEmissionFactorScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalEmissionFactorScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalEmissionFactorScheduleStatus): Boolean {
        when (status) {
            RegionalEmissionFactorScheduleStatus.PROPOSED -> return next == RegionalEmissionFactorScheduleStatus.REJECTED
            RegionalEmissionFactorScheduleStatus.APPROVED -> {
                when (next) {
                    RegionalEmissionFactorScheduleStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalEmissionFactorScheduleStatus.REJECTED -> return next == RegionalEmissionFactorScheduleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RegionalEmissionFactorScheduleStatus) {
        if (!canMoveTo(next)) {
            error("regional emission factor schedule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun effectiveTotal(): Double = round(sequence.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): RegionalEmissionFactorSchedule {
        return RegionalEmissionFactorSchedule(
            code = code,
            title = title,
            createdAt = createdAt,
            margin = margin,
            sequence = sequence,
            updatedAt = updatedAt,
            attempts = attempts,
            durationMinutes = durationMinutes,
            threshold = threshold,
            dock = dock,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional emission factor schedule in status proposed. */
fun newRegionalEmissionFactorSchedule(code: String, title: String): RegionalEmissionFactorSchedule {
    return RegionalEmissionFactorSchedule(
        code = code,
        title = title,
        createdAt = 0,
        margin = 0.0,
        sequence = 0,
        updatedAt = 0,
        attempts = 0,
        durationMinutes = 0,
        threshold = 0.0,
        dock = "",
        approved = false,
        status = RegionalEmissionFactorScheduleStatus.PROPOSED,
    )
}
