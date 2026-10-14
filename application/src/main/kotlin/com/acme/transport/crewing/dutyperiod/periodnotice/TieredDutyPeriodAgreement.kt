package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered duty period agreement is in its life cycle. */
enum class TieredDutyPeriodAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tiered duty period agreement. Its code is unique within a tenant. */
data class TieredDutyPeriodAgreement(
    var code: String,
    var label: String,
    var region: String,
    var distanceKm: Double,
    var ownerId: String,
    var driverId: String,
    var score: Double,
    var status: TieredDutyPeriodAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDutyPeriodAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDutyPeriodAgreementStatus): Boolean {
        when (status) {
            TieredDutyPeriodAgreementStatus.NEW -> return next == TieredDutyPeriodAgreementStatus.DONE || next == TieredDutyPeriodAgreementStatus.FAILED
            TieredDutyPeriodAgreementStatus.QUEUED -> {
                when (next) {
                    TieredDutyPeriodAgreementStatus.RUNNING -> return true
                }
                return false
            }
            TieredDutyPeriodAgreementStatus.RUNNING -> return next == TieredDutyPeriodAgreementStatus.DONE || next == TieredDutyPeriodAgreementStatus.FAILED
            TieredDutyPeriodAgreementStatus.DONE -> {
                when (next) {
                    TieredDutyPeriodAgreementStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDutyPeriodAgreementStatus) {
        if (!canMoveTo(next)) {
            error("tiered duty period agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedExposure(): Double = round((distanceKm + score) * 100) / 100

    fun snapshot(): TieredDutyPeriodAgreement {
        return TieredDutyPeriodAgreement(
            code = code,
            label = label,
            region = region,
            distanceKm = distanceKm,
            ownerId = ownerId,
            driverId = driverId,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tiered duty period agreement in status new. */
fun newTieredDutyPeriodAgreement(code: String, label: String): TieredDutyPeriodAgreement {
    return TieredDutyPeriodAgreement(
        code = code,
        label = label,
        region = "",
        distanceKm = 0.0,
        ownerId = "",
        driverId = "",
        score = 0.0,
        status = TieredDutyPeriodAgreementStatus.NEW,
    )
}
