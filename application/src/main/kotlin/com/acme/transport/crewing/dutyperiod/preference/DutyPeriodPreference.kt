package com.acme.transport.crewing.dutyperiod.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The duty period preference records of crewing's duty period work, and the rules, stores, and calculations
// around them.

/** Where a duty period preference is in its life cycle. */
enum class DutyPeriodPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a duty period preference and its current status. */
data class DutyPeriodPreference(
    var code: String,
    var title: String,
    var amount: Double,
    var currency: String,
    var visible: Boolean,
    var reviewer: String,
    var endsAt: Int,
    var distanceKm: Double,
    var floorValue: Double,
    var approved: Boolean,
    var status: DutyPeriodPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodPreferenceStatus): Boolean {
        when (status) {
            DutyPeriodPreferenceStatus.NEW -> {
                when (next) {
                    DutyPeriodPreferenceStatus.QUEUED, DutyPeriodPreferenceStatus.RUNNING -> return true
                }
                return false
            }
            DutyPeriodPreferenceStatus.QUEUED -> {
                when (next) {
                    DutyPeriodPreferenceStatus.RUNNING -> return true
                }
                return false
            }
            DutyPeriodPreferenceStatus.RUNNING -> {
                when (next) {
                    DutyPeriodPreferenceStatus.DONE, DutyPeriodPreferenceStatus.FAILED -> return true
                }
                return false
            }
            DutyPeriodPreferenceStatus.DONE -> return next == DutyPeriodPreferenceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DutyPeriodPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("duty period preference $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedValue(): Double = round((distanceKm - amount) * 100) / 100

    fun snapshot(): DutyPeriodPreference {
        return DutyPeriodPreference(
            code = code,
            title = title,
            amount = amount,
            currency = currency,
            visible = visible,
            reviewer = reviewer,
            endsAt = endsAt,
            distanceKm = distanceKm,
            floorValue = floorValue,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a duty period preference in status new. */
fun newDutyPeriodPreference(code: String, title: String): DutyPeriodPreference {
    return DutyPeriodPreference(
        code = code,
        title = title,
        amount = 0.0,
        currency = "",
        visible = false,
        reviewer = "",
        endsAt = 0,
        distanceKm = 0.0,
        floorValue = 0.0,
        approved = false,
        status = DutyPeriodPreferenceStatus.NEW,
    )
}
