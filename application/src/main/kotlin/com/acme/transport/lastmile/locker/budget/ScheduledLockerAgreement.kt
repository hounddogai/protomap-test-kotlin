package com.acme.transport.lastmile.locker.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled locker agreement is in its life cycle. */
enum class ScheduledLockerAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a scheduled locker agreement and its current status. */
data class ScheduledLockerAgreement(
    var code: String,
    var title: String,
    var limit: Int,
    var summary: String,
    var etaMinutes: Int,
    var fuelLiters: Double,
    var attempts: Int,
    var country: String,
    var status: ScheduledLockerAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLockerAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLockerAgreementStatus): Boolean {
        when (status) {
            ScheduledLockerAgreementStatus.NEW -> return next == ScheduledLockerAgreementStatus.FAILED
            ScheduledLockerAgreementStatus.QUEUED -> {
                when (next) {
                    ScheduledLockerAgreementStatus.DONE -> return true
                }
                return false
            }
            ScheduledLockerAgreementStatus.RUNNING -> {
                when (next) {
                    ScheduledLockerAgreementStatus.FAILED -> return true
                }
                return false
            }
            ScheduledLockerAgreementStatus.DONE -> {
                when (next) {
                    ScheduledLockerAgreementStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledLockerAgreementStatus) {
        if (!canMoveTo(next)) {
            error("scheduled locker agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netLoad(): Double = round((attempts.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): ScheduledLockerAgreement {
        return ScheduledLockerAgreement(
            code = code,
            title = title,
            limit = limit,
            summary = summary,
            etaMinutes = etaMinutes,
            fuelLiters = fuelLiters,
            attempts = attempts,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scheduled locker agreement in status new. */
fun newScheduledLockerAgreement(code: String, title: String): ScheduledLockerAgreement {
    return ScheduledLockerAgreement(
        code = code,
        title = title,
        limit = 0,
        summary = "",
        etaMinutes = 0,
        fuelLiters = 0.0,
        attempts = 0,
        country = "",
        status = ScheduledLockerAgreementStatus.NEW,
    )
}
